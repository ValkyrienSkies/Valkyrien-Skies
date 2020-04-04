package org.valkyrienskies.mod.common.ship_handling;

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
import org.valkyrienskies.addon.control.nodenetwork.INodeController;
import org.valkyrienskies.mod.client.render.PhysObjectRenderManager;
import org.valkyrienskies.mod.common.coordinates.ShipTransform;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.BlockPhysicsDetails;
import org.valkyrienskies.mod.common.physics.PhysicsCalculations;
import org.valkyrienskies.mod.common.physics.collision.meshing.IVoxelFieldAABBMaker;
import org.valkyrienskies.mod.common.physics.collision.meshing.NaiveVoxelFieldAABBMaker;
import org.valkyrienskies.mod.common.physics.management.BasicCenterOfMassProvider;
import org.valkyrienskies.mod.common.physics.management.IPhysicsObjectCenterOfMassProvider;
import org.valkyrienskies.mod.common.physics.management.ShipTransformationManager;
import org.valkyrienskies.mod.common.physics.management.chunkcache.ClaimedChunkCacheController;
import org.valkyrienskies.mod.common.physics.management.chunkcache.SurroundingChunkCacheController;
import org.valkyrienskies.mod.common.physmanagement.relocation.MoveBlocks;
import org.valkyrienskies.mod.common.tileentity.TileEntityPhysicsInfuser;
import valkyrienwarfare.api.IPhysicsEntity;
import valkyrienwarfare.api.TransformType;

import javax.annotation.Nonnull;
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
    /**
     * Used to quickly make AABBs
     */
    @Getter
    private final IVoxelFieldAABBMaker voxelFieldAABBMaker;
    private final IPhysicsObjectCenterOfMassProvider centerOfMassProvider;
    @Getter
    private final World world;

    /**
     * Please never manually update this
     */
    @Delegate
    @Getter
    private final ShipData shipData;

    // endregion

    // region Methods

    /**
     * Creates a new PhysicsObject.
     *
     * @param world            The world this object exists in.
     * @param initial          The ShipData this PhysicsObject will be created with
     * @param firstTimeCreated True if this ship was just created through a physics infuser, false
     *                         if it was loaded in from the world save.
     */
    PhysicsObject(World world, ShipData initial, boolean firstTimeCreated) {
        // QueryableShipData.get(world).registerUpdateListener(this::shipDataUpdateListener);
        this.world = world;
        this.shipData = initial;
        this.referenceBlockPos = getShipData().getChunkClaim().getRegionCenter();
        this.watchingPlayers = new ArrayList<>();
        this.physicsControllers = ConcurrentHashMap.newKeySet();
        this.physicsControllersImmutable = Collections.unmodifiableSet(this.physicsControllers);
        this.claimedChunkCache = new ClaimedChunkCacheController(this, !firstTimeCreated);
        this.cachedSurroundingChunks = new SurroundingChunkCacheController(this);
        this.voxelFieldAABBMaker = new NaiveVoxelFieldAABBMaker(referenceBlockPos.getX(),
            referenceBlockPos.getZ());
        this.centerOfMassProvider = new BasicCenterOfMassProvider();
        this.shipTransformationManager = new ShipTransformationManager(this,
            getShipData().getShipTransform());
        this.physicsCalculations = new PhysicsCalculations(this);
        this.shipAligningToGrid = false;
        this.needsCollisionCacheUpdate = true;
        // Note how this is last.
        if (world.isRemote) {
            this.shipRenderer = new PhysObjectRenderManager(this, referenceBlockPos);
        } else {
            this.shipRenderer = null;
            if (!firstTimeCreated) {
                this.getShipTransformationManager()
                    .updateAllTransforms(this.getShipData().getShipTransform(), true, true);
                Objects.requireNonNull(shipData.getBlockPositions())
                    .forEach(voxelFieldAABBMaker::addVoxel);
            }
        }
    }

    public void onSetBlockState(IBlockState oldState, IBlockState newState, BlockPos posAt) {
        // If the world is remote, or the block is not within the claimed chunks, ignore it!
        if (getWorld().isRemote || !getShipData().getChunkClaim().containsBlock(posAt)) {
            return;
        }

        // If the block here is not to be made with physics, just treat it like you'd
        // treat AIR blocks.
        if (oldState != null && BlockPhysicsDetails.blocksToNotPhysicsInfuse
            .contains(oldState.getBlock())) {
            oldState = Blocks.AIR.getDefaultState();
        }
        if (newState != null && BlockPhysicsDetails.blocksToNotPhysicsInfuse
            .contains(newState.getBlock())) {
            newState = Blocks.AIR.getDefaultState();
        }

        boolean isOldAir = oldState == null || oldState.getBlock().equals(Blocks.AIR);
        boolean isNewAir = newState == null || newState.getBlock().equals(Blocks.AIR);

        if (isNewAir) {
            getBlockPositions().remove(posAt);
            voxelFieldAABBMaker.removeVoxel(posAt.getX(), posAt.getY(), posAt.getZ());
        }

        if (isOldAir && !isNewAir) {
            getBlockPositions().add(posAt);
            voxelFieldAABBMaker.addVoxel(posAt.getX(), posAt.getY(), posAt.getZ());
        }

        if (getBlockPositions().isEmpty()) {
            // TODO: Maybe?
            //destroy();
        }

        if (getPhysicsCalculations() != null) {
            getPhysicsCalculations().onSetBlockState(oldState, newState, posAt);
        }

        centerOfMassProvider.onSetBlockState(shipData.getInertiaData(), posAt, oldState, newState);
    }

    void onTick() {
        cachedSurroundingChunks.updateChunkCache();

        this.setNeedsCollisionCacheUpdate(true);

        if (!world.isRemote) {
            ShipTransform physicsTransform = getShipTransformationManager()
                .getCurrentPhysicsTransform();
            getShipTransformationManager().updateAllTransforms(physicsTransform, false, true);
            getShipData().setShipTransform(getShipTransformationManager().getCurrentTickTransform());

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
            Vector position = new Vector(currentTransform.getPosX(), currentTransform.getPosY(),
                    currentTransform.getPosZ());

            BlockPos centerDifference = new BlockPos(
                    Math.round(getCenterCoord().x - position.x),
                    Math.round(getCenterCoord().y - position.y),
                    Math.round(getCenterCoord().z - position.z));

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

    public Vector getCenterCoord() {
        return new Vector(this.getShipData().getShipTransform().getCenterCoord());
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
}
