package org.valkyrienskies.mod.common.ship_handling;

import gnu.trove.iterator.TIntIterator;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.network.play.server.SPacketUnloadChunk;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.ChunkProviderServer;
import org.joml.Quaterniondc;
import org.valkyrienskies.addon.control.nodenetwork.INodeController;
import org.valkyrienskies.mod.client.render.PhysObjectRenderManager;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.coordinates.ShipTransform;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.network.SpawnPhysObjMessage;
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
import org.valkyrienskies.mod.common.physmanagement.relocation.SpatialDetector;
import org.valkyrienskies.mod.common.tileentity.TileEntityPhysicsInfuser;
import valkyrienwarfare.api.IPhysicsEntity;
import valkyrienwarfare.api.TransformType;

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
        this.centerOfMassProvider = new BasicCenterOfMassProvider(initial.getInertiaData());
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

        centerOfMassProvider.onSetBlockState(this, posAt, oldState, newState);
    }

    void assembleShip(EntityPlayer player, SpatialDetector detector,
                      BlockPos centerInWorld) {

        MutableBlockPos pos = new MutableBlockPos();

        BlockPos centerDifference = getReferenceBlockPos().subtract(centerInWorld);

        TIntIterator iter = detector.foundSet.iterator();

        MutableBlockPos oldPos = new MutableBlockPos();
        MutableBlockPos newPos = new MutableBlockPos();

        // First copy all the blocks from ship to world.
        while (iter.hasNext()) {
            int i = iter.next();
            SpatialDetector.setPosWithRespectTo(i, centerInWorld, oldPos);
            SpatialDetector.setPosWithRespectTo(i, centerInWorld, newPos);
            newPos.setPos(newPos.getX() + centerDifference.getX(),
                newPos.getY() + centerDifference.getY(),
                newPos.getZ() + centerDifference.getZ());

            MoveBlocks.copyBlockToPos(getWorld(), oldPos, newPos, Optional.of(this));
            voxelFieldAABBMaker.addVoxel(newPos.getX(), newPos.getY(), newPos.getZ());
        }
        // Update the physics infuser pos.
        getShipData().setPhysInfuserPos(getShipData().getPhysInfuserPos().add(centerDifference));

        // First we destroy all the tile entities we copied.
        iter = detector.foundSet.iterator();
        while (iter.hasNext()) {
            int i = iter.next();
            SpatialDetector.setPosWithRespectTo(i, centerInWorld, pos);
            TileEntity tile = getWorld().getTileEntity(pos);
            if (tile != null && !tile.isInvalid()) {
                try {
                    tile.invalidate();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    getWorld().removeTileEntity(pos);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // We NEED this to fix ship lighting. If this code was removed then ships would have lighting artifacts all
        // over them.
        for (ChunkPos chunkPos : getChunkClaim()) {
            Chunk chunkAt = claimedChunkCache.getChunkAt(chunkPos.x, chunkPos.z);
            chunkAt.setTerrainPopulated(true);
            chunkAt.setLightPopulated(true);
        }

        // Then we destroy all the blocks we copied
        iter = detector.foundSet.iterator();
        while (iter.hasNext()) {
            int i = iter.next();
            SpatialDetector.setPosWithRespectTo(i, centerInWorld, pos);
            getWorld().setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
        }

        // Some extra ship crap at the end.
        detectBlockPositions();

        // This puts the updated ShipData transform into the transformation manager. It also creates the ship bounding
        // box (Which is stored into ShipData).
        this.getShipTransformationManager()
            .updateAllTransforms(this.getShipData().getShipTransform(), true, true);
    }

    private void preloadNewPlayers() {
        Set<EntityPlayerMP> newWatchers = getPlayersThatJustWatched();
        for (Chunk chunk : claimedChunkCache) {
            SPacketChunkData data = new SPacketChunkData(chunk, 65535);
            for (EntityPlayerMP player : newWatchers) {
                player.connection.sendPacket(data);
                ((WorldServer) getWorld()).getEntityTracker()
                        .sendLeashedEntitiesInChunk(player, chunk);
            }
        }

        SpawnPhysObjMessage physObjMessage = new SpawnPhysObjMessage();
        physObjMessage.initializeData(getShipData());
        for (EntityPlayerMP player : newWatchers) {
            ValkyrienSkiesMod.physWrapperNetwork.sendTo(physObjMessage, player);
        }
    }

    /**
     * TODO: Make this further get the player to stop all further tracking of those physObject
     *
     * @param untracking EntityPlayer that stopped tracking
     */
    public void onPlayerUntracking(EntityPlayer untracking) {
        getWatchingPlayers().remove(untracking);
        for (ChunkPos chunkPos : getChunkClaim()) {
            SPacketUnloadChunk unloadPacket = new SPacketUnloadChunk(chunkPos.x, chunkPos.z);
            ((EntityPlayerMP) untracking).connection.sendPacket(unloadPacket);
        }
    }

    /**
     * Called when this entity has been unloaded from the world
     */
    private void onThisUnload() {
        if (!getWorld().isRemote) {
            unloadShipChunksFromWorld();
        } else {
            getShipRenderer().killRenderers();
        }
    }

    private void unloadShipChunksFromWorld() {
        ChunkProviderServer provider = (ChunkProviderServer) getWorld().getChunkProvider();
        for (ChunkPos chunkPos : getChunkClaim()) {
            provider.queueUnload(claimedChunkCache.getChunkAt(chunkPos.x, chunkPos.z));
        }
    }

    private Set<EntityPlayerMP> getPlayersThatJustWatched() {
        Set<EntityPlayerMP> newPlayers = new HashSet<>();
        // for (Object o : ((WorldServer) getWorld()).getEntityTracker()
        //  .getTrackingPlayers(getWrapperEntity())) {
        for (EntityPlayer playerLol : getWorld().playerEntities) {
            EntityPlayerMP player = (EntityPlayerMP) playerLol;
            if (!getWatchingPlayers().contains(player)) {
                newPlayers.add(player);
                getWatchingPlayers().add(player);
            }
        }
        return newPlayers;
    }

    void onTick() {
        updateChunkCache();
        preloadNewPlayers();

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

    private void updateChunkCache() {
        cachedSurroundingChunks.updateChunkCache();
    }

    // Generates the blockPos array; must be loaded DIRECTLY after the chunks are
    // setup
    private void detectBlockPositions() {
        final int airStateIndex = Block.getStateId(Blocks.AIR.getDefaultState());

        for (Chunk chunk : claimedChunkCache) {
            for (int index = 0; index < 16; index++) {
                ExtendedBlockStorage storage = chunk.getBlockStorageArray()[index];
                if (storage != null) {
                    for (int y = 0; y < 16; y++) {
                        for (int x = 0; x < 16; x++) {
                            for (int z = 0; z < 16; z++) {
                                if (storage.data.storage
                                    .getAt(y << 8 | z << 4 | x)
                                    != airStateIndex) {
                                    BlockPos pos = new BlockPos(chunk.x * 16 + x,
                                        index * 16 + y,
                                        chunk.z * 16 + z);
                                    getBlockPositions().add(pos);
                                    voxelFieldAABBMaker
                                        .addVoxel(pos.getX(), pos.getY(), pos.getZ());
                                    if (BlockPhysicsDetails.isBlockProvidingForce(
                                        getWorld().getBlockState(pos), pos, getWorld())) {
                                        getPhysicsCalculations()
                                            .addPotentialActiveForcePos(pos);
                                    }
                                }
                            }
                        }
                    }
                }
            }
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

}
