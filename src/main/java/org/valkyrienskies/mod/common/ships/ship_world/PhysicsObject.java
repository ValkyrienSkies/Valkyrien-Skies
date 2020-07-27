package org.valkyrienskies.mod.common.ships.ship_world;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
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
import org.valkyrienskies.addon.control.nodenetwork.INodeController;
import org.valkyrienskies.mod.client.render.PhysObjectRenderManager;
import org.valkyrienskies.mod.common.ships.ShipData;
import org.valkyrienskies.mod.common.ships.interpolation.ITransformInterpolator;
import org.valkyrienskies.mod.common.ships.interpolation.SimpleEMATransformInterpolator;
import org.valkyrienskies.mod.common.ships.ship_transform.ShipTransform;
import org.valkyrienskies.mod.common.physics.PhysicsCalculations;
import org.valkyrienskies.mod.common.ships.chunk_claims.ClaimedChunkCacheController;
import org.valkyrienskies.mod.common.ships.chunk_claims.SurroundingChunkCacheController;
import org.valkyrienskies.mod.common.ships.block_relocation.MoveBlocks;
import org.valkyrienskies.mod.common.ships.physics_data.BasicCenterOfMassProvider;
import org.valkyrienskies.mod.common.ships.physics_data.IPhysicsObjectCenterOfMassProvider;
import org.valkyrienskies.mod.common.ships.ship_transform.ShipTransformationManager;
import org.valkyrienskies.mod.common.tileentity.TileEntityPhysicsInfuser;
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

    // region Fields

    @Getter
    private final List<EntityPlayerMP> watchingPlayers;
    private final Set<INodeController> physicsControllers;
    private final Set<INodeController> physicsControllersImmutable;
    // Used to iterate over the ship blocks extremely quickly by taking advantage of the cache
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
    private SurroundingChunkCacheController cachedSurroundingChunks;

    /**
     * Used for faster memory access to the Chunks this object 'owns'
     */
    @Getter
    private final ClaimedChunkCacheController claimedChunkCache;
    /**
     * If this PhysicsObject needs to update the collision cache immediately
     */
    @Setter
    @Getter
    private boolean needsCollisionCacheUpdate;

    @Getter
    private boolean shipAligningToGrid;

    @Getter
    private final World world;

    /**
     * Please never manually update this
     */
    @Delegate
    @Getter
    private final ShipData shipData;

    @Getter
    private final ITransformInterpolator transformInterpolator;

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
        this.needsCollisionCacheUpdate = true;
        // Note how this is last.
        if (world.isRemote) {
            this.shipRenderer = new PhysObjectRenderManager(this, referenceBlockPos);
            this.transformInterpolator = new SimpleEMATransformInterpolator(initial.getShipTransform(), initial.getShipBB(), .75);
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
            this.setNeedsCollisionCacheUpdate(true);

            ShipTransform physicsTransform = getShipTransformationManager()
                .getCurrentPhysicsTransform();
            getShipTransformationManager().updateAllTransforms(physicsTransform, false, true);
            // Copy the current and prev transforms into ShipData
            getShipData().setShipTransform(getShipTransformationManager().getCurrentTickTransform());
            getShipData().setPrevTickShipTransform(getShipTransformationManager().getPrevTickTransform());

            TileEntity te = getWorld().getTileEntity(getShipData().getPhysInfuserPos());
            boolean shouldDeconstructShip;

            // Only want to update the status of whether this ship is queued for unload AFTER we've updated from the
            // physics transform.
            if (te instanceof TileEntityPhysicsInfuser) {
                TileEntityPhysicsInfuser physicsCore = (TileEntityPhysicsInfuser) te;
                // Mark for deconstruction
                shouldDeconstructShip =
                        !physicsCore.canMaintainShip() || physicsCore.isTryingToDisassembleShip();
                shipAligningToGrid =
                        !physicsCore.canMaintainShip() || physicsCore.isTryingToAlignShip();
                getShipData().setPhysicsEnabled(!physicsCore.canMaintainShip() ||
                        physicsCore.isPhysicsEnabled());
            } else {
                // Mark for deconstruction
                shipAligningToGrid = true;
                shouldDeconstructShip = true;
                getShipData().setPhysicsEnabled(true);
            }
        } else {
            transformInterpolator.tickTransformInterpolator();
            ShipTransform newTransform = transformInterpolator.getCurrentTickTransform();
            AxisAlignedBB newAABB = transformInterpolator.getCurrentAABB();

            shipData.setPrevTickShipTransform(shipData.getShipTransform());
            shipData.setShipTransform(newTransform);
            shipData.setShipBB(newAABB);

            shipTransformationManager.updateAllTransforms(newTransform, false, false);
            /*
            WrapperPositionMessage toUse = getShipTransformationManager().serverBuffer
                .pollForClientTransform();
            if (toUse != null) {
                toUse.applySmoothLerp(this, .6D);
            }
             */
        }
    }

    // endregion

    // region More Methods
    /*
     * Encapsulation code past here.
     */

    /**
     * Sets the consecutive tick counter to 0.
     */
    public void resetConsecutiveProperTicks() {
        this.setNeedsCollisionCacheUpdate(true);
    }

    /**
     * @return the cachedSurroundingChunks
     */
    public ChunkCache getCachedSurroundingChunks() {
        return cachedSurroundingChunks.getCachedChunks();
    }

    // ===== Keep track of all Node Processors in a concurrent Set =====
    public void onSetTileEntity(BlockPos pos, TileEntity tileentity) {
        if (tileentity instanceof INodeController) {
            physicsControllers.add((INodeController) tileentity);
        }
    }

    public void onRemoveTileEntity(BlockPos pos) {
        physicsControllers.removeIf(next -> next.getNodePos().equals(pos));
    }

    // Do not allow anything external to modify the physics controllers Set.
    public Set<INodeController> getPhysicsControllersInShip() {
        return physicsControllersImmutable;
    }

    /**
     * Returns true if this ship is aligned close enough to the grid that it is allowed to
     * deconstruct back to the world.
     */
    boolean shouldShipBeDestroyed() {
        TileEntity te = getWorld().getTileEntity(getShipData().getPhysInfuserPos());
        boolean shouldDeconstructShip;
        if (te instanceof TileEntityPhysicsInfuser) {
            TileEntityPhysicsInfuser physicsCore = (TileEntityPhysicsInfuser) te;
            // Mark for deconstruction
            shouldDeconstructShip =
                    !physicsCore.canMaintainShip() || physicsCore.isTryingToDisassembleShip();
        } else {
            shouldDeconstructShip = true;
        }

        if (!shouldDeconstructShip) {
            return false;
        }

        return isShipAlignedToWorld();
    }

    public boolean isShipAlignedToWorld() {
        // The quaternion with the ship's orientation
        Quaterniondc shipQuat = getShipTransformationManager().getCurrentTickTransform()
                .rotationQuaternion(TransformType.SUBSPACE_TO_GLOBAL);
        // Only allow a ship to be deconstructed if the angle between the grid and its orientation is less than half a degree.
        return Math.toDegrees(shipQuat.angle()) < .5;
    }

    void destroyShip() {
        // Then tell the game to stop tracking/loading the chunks
        List<EntityPlayerMP> watchersCopy = new ArrayList<EntityPlayerMP>(getWatchingPlayers());
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
                MoveBlocks.copyBlockToPos(getWorld(), oldPos, newPos, Optional.empty());
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

    /**
     * A thread safe way of accessing tile entities within a ship. Not guaranteed to provide the most up to do tile.
     */
    @Nullable
    public TileEntity getShipTile(@Nonnull BlockPos pos) {
        Chunk chunk = claimedChunkCache.getChunkAt(pos.getX() >> 4, pos.getZ() >> 4);
        if (chunk != null) {
            return chunk.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
        } else {
            return null;
        }
    }
}
