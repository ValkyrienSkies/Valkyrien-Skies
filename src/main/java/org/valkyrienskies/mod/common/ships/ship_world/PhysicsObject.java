package org.valkyrienskies.mod.common.ships.ship_world;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketUnloadChunk;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.mod.client.render.PhysObjectRenderManager;
import org.valkyrienskies.mod.common.collision.Polygon;
import org.valkyrienskies.mod.common.physics.IPhysicsBlockController;
import org.valkyrienskies.mod.common.physics.PhysicsCalculations;
import org.valkyrienskies.mod.common.ships.ShipData;
import org.valkyrienskies.mod.common.ships.block_relocation.MoveBlocks;
import org.valkyrienskies.mod.common.ships.block_relocation.SpatialDetector;
import org.valkyrienskies.mod.common.ships.chunk_claims.ClaimedChunkCacheController;
import org.valkyrienskies.mod.common.ships.chunk_claims.SurroundingChunkCacheController;
import org.valkyrienskies.mod.common.ships.entity_interaction.IDraggable;
import org.valkyrienskies.mod.common.ships.interpolation.ITransformInterpolator;
import org.valkyrienskies.mod.common.ships.interpolation.SimpleEMATransformInterpolator;
import org.valkyrienskies.mod.common.ships.ship_transform.ShipTransform;
import org.valkyrienskies.mod.common.ships.ship_transform.ShipTransformationManager;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
import valkyrienwarfare.api.IPhysicsEntity;
import valkyrienwarfare.api.TransformType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The heart and soul of this mod, and now its broken lol.
 */

public class PhysicsObject implements IPhysicsEntity {

    // The number of ticks we wait before enabling physics. I use 20 because I'm very paranoid of ships falling through the ground.
    private static final int DISABLE_PHYSICS_FOR_X_INITIAL_TICKS = 20;
    // Before we start dragging entities with the ship, Wait this number of ticks after a ship has been teleported using "/vs tp-ship-to" commands.
    public static int TICKS_SINCE_TELEPORT_TO_START_DRAGGING = 50;

    // region Fields
    @Getter
    private final List<EntityPlayerMP> watchingPlayers;
    private final Set<IPhysicsBlockController> physicsControllers;
    private final Set<IPhysicsBlockController> physicsControllersImmutable;
    @Getter
    private final PhysObjectRenderManager shipRenderer;
    /**
     * Just a random block position in the ship. Used to correct floating point errors and keep
     * track of the ship.
     */
    @Getter
    private final BlockPos referenceBlockPos;
    @Getter
    private final ShipTransformationManager shipTransformationManager;
    @Getter
    private final PhysicsCalculations physicsCalculations;

    // The closest Chunks to the Ship cached in here
    private final SurroundingChunkCacheController cachedSurroundingChunks;

    /**
     * Used for faster memory access to the Chunks this object 'owns'
     */
    @Getter
    private final ClaimedChunkCacheController claimedChunkCache;

    @Getter
    private final World world;

    /**
     * Please never manually update this
     */
    @Delegate
    @Getter
    private final ShipData shipData;

    /**
     * Used by the client to smoothly interpolate the ShipTransform sent by the server, so that clients see smooth ship
     * movement.
     */
    @Getter
    private final ITransformInterpolator transformInterpolator;

    /**
     * If true, this ship will slowly realign itself with the world, ignoring the normal rules of physics
     */
    @Setter
    @Getter
    private boolean shipAligningToGrid;

    /**
     * This determines if a ship is trying to deconstruct, and how it will deconstruct.
     */
    @Setter
    @Getter
    @Nonnull
    private DeconstructState deconstructState;

    // If (forceToUseShipDataTransform == true) then reset the physics transform to the ShipData transform.
    @Setter
    private boolean forceToUseShipDataTransform;

    // Used to prevent players from thinking they're on a ship if this ship just got teleported.
    @Setter @Getter
    private int ticksSinceShipTeleport;

    // Counts the number of ticks this PhysicsObject (not ShipData) has existed. Used to disable physics for the first DISABLE_PHYSICS_FOR_X_INITIAL_TICKS ticks.
    private int ticksExisted;

    // endregion

    // region Methods

    /**
     * Creates a new PhysicsObject.
     *
     * @param world            The world this object exists in.
     * @param initial          The ShipData this PhysicsObject will be created with
     */
    PhysicsObject(World world, ShipData initial) {
        // QueryableShipData.get(world).registerUpdateListener(this::shipDataUpdateListener);
        this.world = world;
        this.shipData = initial;
        this.referenceBlockPos = getShipData().getChunkClaim().getRegionCenter();
        this.watchingPlayers = new ArrayList<>();
        this.physicsControllers = ConcurrentHashMap.newKeySet();
        this.physicsControllersImmutable = Collections.unmodifiableSet(this.physicsControllers);
        this.claimedChunkCache = new ClaimedChunkCacheController(this);
        this.cachedSurroundingChunks = new SurroundingChunkCacheController(this);
        this.shipTransformationManager = new ShipTransformationManager(this,
            getShipData().getShipTransform());
        this.physicsCalculations = new PhysicsCalculations(this);
        this.shipAligningToGrid = false;
        this.deconstructState = DeconstructState.NOT_DECONSTRUCTING;
        this.forceToUseShipDataTransform = false;
        this.ticksSinceShipTeleport = TICKS_SINCE_TELEPORT_TO_START_DRAGGING + 1; // Anything larger than TICKS_SINCE_TELEPORT_TO_START_DRAGGING works
        this.ticksExisted = 0;
        // Note how this is last.
        if (world.isRemote) {
            this.shipRenderer = new PhysObjectRenderManager(this, referenceBlockPos);
            this.transformInterpolator = new SimpleEMATransformInterpolator(initial.getShipTransform(), initial.getShipBB(), .5);
        } else {
            this.shipRenderer = null;
            this.getShipTransformationManager()
                .updateAllTransforms(this.getShipData().getShipTransform(), true, true);
            this.transformInterpolator = null;
        }
    }

    void onTick() {
        if (!world.isRemote) {
            cachedSurroundingChunks.updateChunkCache();

            final boolean forceToUseShipDataTransformLocalCopy = forceToUseShipDataTransform;
            forceToUseShipDataTransform = false;
            if (forceToUseShipDataTransformLocalCopy) {
                final ShipTransform forcedTransform = shipData.getShipTransform();

                // This is BAD! Race condition! But I don't think this will cause any problems (I hope).
                getShipTransformationManager().setPrevPhysicsTransform(forcedTransform);
                getShipTransformationManager().setCurrentPhysicsTransform(forcedTransform);
                getShipTransformationManager().setPrevTickTransform(forcedTransform);
                getShipTransformationManager().setCurrentTickTransform(forcedTransform);
            }

            ticksSinceShipTeleport++;

            ShipTransform physicsTransform = getShipTransformationManager()
                .getCurrentPhysicsTransform();
            getShipTransformationManager().updateAllTransforms(physicsTransform, false, true);
            // Copy the current and prev transforms into ShipData
            getShipData().setShipTransform(getShipTransformationManager().getCurrentTickTransform());
            getShipData().setPrevTickShipTransform(getShipTransformationManager().getPrevTickTransform());
        } else {
            transformInterpolator.tickTransformInterpolator();
            ShipTransform newTransform = transformInterpolator.getCurrentTickTransform();
            AxisAlignedBB newAABB = transformInterpolator.getCurrentAABB();

            shipData.setPrevTickShipTransform(shipData.getShipTransform());
            shipData.setShipTransform(newTransform);
            shipData.setShipBB(newAABB);

            shipTransformationManager.updateAllTransforms(newTransform, false, false);
        }
        this.ticksExisted++;
    }

    // endregion

    // region More Methods
    /*
     * Encapsulation code past here.
     */

    /**
     * @return the cachedSurroundingChunks
     */
    public ChunkCache getCachedSurroundingChunks() {
        return cachedSurroundingChunks.getCachedChunks();
    }

    // ===== Keep track of all Node Processors in a concurrent Set =====
    public void onSetTileEntity(BlockPos pos, TileEntity tileentity) {
        if (tileentity instanceof IPhysicsBlockController) {
            physicsControllers.add((IPhysicsBlockController) tileentity);
        }
    }

    public void onRemoveTileEntity(BlockPos pos) {
        physicsControllers.removeIf(next -> next.getNodePos().equals(pos));
    }

    // Do not allow anything external to modify the physics controllers Set.
    public Set<IPhysicsBlockController> getPhysicsControllersInShip() {
        return physicsControllersImmutable;
    }

    /**
     * Returns true if this ship is aligned close enough to the grid that it is allowed to
     * deconstruct back to the world.
     */
    boolean shouldShipBeDestroyed() {
        if (getBlockPositions().isEmpty()) {
            return true;
        }
        if (deconstructState.deconstructShip) {
            if (deconstructState.mustBeAlignedBeforeDeconstruct) {
                return isShipAlignedToWorld();
            } else {
                return true;
            }
        }
        return false;
    }

    public boolean isShipAlignedToWorld() {
        // The quaternion with the ship's orientation
        Quaterniondc shipQuat = getShipTransformationManager().getCurrentTickTransform()
                .rotationQuaternion(TransformType.SUBSPACE_TO_GLOBAL);
        // Only allow a ship to be deconstructed if the angle between the grid and its orientation is less than half a degree.
        return Math.toDegrees(shipQuat.angle()) < 2;
    }

    void destroyShip() {
        // Then tell the game to stop tracking/loading the chunks
        List<EntityPlayerMP> watchersCopy = new ArrayList<>(getWatchingPlayers());
        for (ChunkPos chunkPos : getChunkClaim()) {
            SPacketUnloadChunk unloadPacket = new SPacketUnloadChunk(chunkPos.x, chunkPos.z);
            for (EntityPlayerMP wachingPlayer : watchersCopy) {
                wachingPlayer.connection.sendPacket(unloadPacket);
            }
            // NOTICE: This method isnt being called to avoid the
            // watchingPlayers.remove(player) call, which is a waste of CPU time
            // onPlayerUntracking(wachingPlayer);
        }
        getWatchingPlayers().clear();

        // Finally, copy all the blocks from the ship to the world
        if (!getBlockPositions().isEmpty()) {
            if (deconstructState.copyBlocks) {
                MutableBlockPos newPos = new MutableBlockPos();

                ShipTransform currentTransform = getShipTransformationManager().getCurrentTickTransform();
                Vector3dc position = new Vector3d(currentTransform.getPosX(), currentTransform.getPosY(),
                        currentTransform.getPosZ());

                BlockPos centerDifference = new BlockPos(
                        Math.round(getCenterCoord().x() - position.x()),
                        Math.round(getCenterCoord().y() - position.y()),
                        Math.round(getCenterCoord().z() - position.z()));

                for (BlockPos oldPos : this.getBlockPositions()) {
                    newPos.setPos(oldPos.getX() - centerDifference.getX(),
                            oldPos.getY() - centerDifference.getY(), oldPos.getZ() - centerDifference.getZ());
                    MoveBlocks.copyBlockToPos(getWorld(), oldPos, newPos, null);
                }

                // Then relight the chunks we just copied the blocks to
                {
                    Set<Long> chunksRelit = new HashSet<>();
                    for (BlockPos changedPos : this.getBlockPositions()) {
                        int changedChunkX = (changedPos.getX() - centerDifference.getX()) >> 4;
                        int changedChunkZ = (changedPos.getZ() - centerDifference.getZ()) >> 4;
                        long changedChunkPos = ChunkPos.asLong(changedChunkX, changedChunkZ);

                        if (chunksRelit.contains(changedChunkPos)) {
                            continue;
                        }
                        final Chunk chunk = world.getChunk(changedChunkX, changedChunkZ);
                        chunk.generateSkylightMap();
                        chunk.checkLight();
                        chunk.markDirty();
                        chunksRelit.add(changedChunkPos);
                    }
                }
            }

            // Just delete the tile entities in ship to prevent any dupe bugs.
            for (BlockPos oldPos : this.getBlockPositions()) {
                getWorld().removeTileEntity(oldPos);
            }
        }

        // Delete all the old ship chunks
        getClaimedChunkCache().deleteShipChunksFromWorld();
    }

    public Vector3dc getCenterCoord() {
        return getShipData().getShipTransform().getCenterCoord();
    }

    // region VS API Functions
    @Override
    public Vec3d rotateVector(Vec3d vector, TransformType transformType) {
        return this.getShipTransformationManager()
            .getCurrentTickTransform()
            .rotate(vector, transformType);
    }

    @Override
    public Vec3d transformVector(Vec3d vector, TransformType transformType) {
        return this.getShipTransformationManager()
            .getCurrentTickTransform()
            .transform(vector, transformType);
    }
    // endregion

    /**
     * Gets the chunk at chunkX and chunkZ.
     *
     * @see ClaimedChunkCacheController#getChunkAt(int, int)
     */
    public Chunk getChunkAt(int chunkX, int chunkZ) {
        return claimedChunkCache.getChunkAt(chunkX, chunkZ);
    }

    public AxisAlignedBB getShipBoundingBox() {
        return getShipData().getShipBB();
    }

    public void setShipBoundingBox(AxisAlignedBB shipBoundingBox) {
        getShipData().setShipBB(shipBoundingBox);
    }

    void unload() {
        watchingPlayers.clear();
        if (!getWorld().isRemote) {
            ChunkProviderServer provider = (ChunkProviderServer) getWorld().getChunkProvider();
            for (ChunkPos chunkPos : getChunkClaim()) {
                provider.queueUnload(claimedChunkCache.getChunkAt(chunkPos.x, chunkPos.z));
            }
        } else {
            ChunkProviderClient provider = (ChunkProviderClient) getWorld().getChunkProvider();
            for (ChunkPos chunkPos : getChunkClaim()) {
                provider.unloadChunk(chunkPos.x, chunkPos.z);
            }
            getShipRenderer().killRenderers();
        }
    }

    /**
     * We allow a ship to be loaded by the client before all the chunks have arrived. So this handles behavior
     * when a ship chunk arrives for an already loaded ship.
     */
    @SideOnly(Side.CLIENT)
    public void updateChunk(@Nonnull Chunk chunk) {
        if (!getChunkClaim().containsChunk(chunk.x, chunk.z)) {
            throw new IllegalStateException("Ship " + getShipData() + " does not contain chunk " + chunk);
        }
        if (claimedChunkCache == null) {
            throw new IllegalStateException("Claimed chunk cache was null for ship " + getShipData());
        }
        if (shipRenderer == null) {
            throw new IllegalStateException("Ship renderer was null for ship " + getShipData());
        }
        claimedChunkCache.updateChunk(chunk);
        shipRenderer.updateChunk(chunk);
    }

    @Getter
    public enum DeconstructState {
        NOT_DECONSTRUCTING(false, false, false),
        DECONSTRUCT_NORMAL(true, true, true),
        DECONSTRUCT_IMMEDIATE_NO_COPY(true, false, false);

        private final boolean deconstructShip;
        private final boolean copyBlocks;
        private final boolean mustBeAlignedBeforeDeconstruct;

        DeconstructState(final boolean deconstructShip, final boolean copyBlocks, final boolean mustBeAlignedBeforeDeconstruct) {
            this.deconstructShip = deconstructShip;
            this.copyBlocks = copyBlocks;
            this.mustBeAlignedBeforeDeconstruct = mustBeAlignedBeforeDeconstruct;
        }
    }

    public AxisAlignedBB getPhysicsTransformAABB() {
        AxisAlignedBB subspaceBB = getBlockPositions().makeAABB();
        if (subspaceBB == null) {
            // The aabbMaker didn't know what the aabb was, just don't update the aabb for now.
            return null;
        }
        // Expand subspaceBB by 1 to fit the block grid.
        subspaceBB = subspaceBB.expand(1, 1, 1);
        // Now transform the subspaceBB to world coordinates
        Polygon largerPoly = new Polygon(subspaceBB, getShipTransformationManager().getCurrentPhysicsTransform(),
                TransformType.SUBSPACE_TO_GLOBAL);
        // Set the ship AABB to that of the polygon.
        AxisAlignedBB worldBB = largerPoly.getEnclosedAABB();
        return worldBB;
    }

    /**
     * Not the same as "is physics enabled?", the idea here is to disable physics for the first few ticks this ship got
     * loaded to prevent the ship from falling though the floor.
     */
    public boolean isPhysicsReady() {
        return ticksExisted >= DISABLE_PHYSICS_FOR_X_INITIAL_TICKS;
    }
}
