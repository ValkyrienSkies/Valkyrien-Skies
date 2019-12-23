package org.valkyrienskies.mod.common.physics.management.physo;

import gnu.trove.iterator.TIntIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
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
import org.valkyrienskies.mod.common.physmanagement.chunk.VSChunkClaim;
import org.valkyrienskies.mod.common.physmanagement.relocation.MoveBlocks;
import org.valkyrienskies.mod.common.physmanagement.relocation.SpatialDetector;
import org.valkyrienskies.mod.common.tileentity.TileEntityPhysicsInfuser;
import valkyrienwarfare.api.IPhysicsEntity;
import valkyrienwarfare.api.TransformType;

/**
 * The heart and soul of this mod, and now its broken lol.
 */

public class PhysicsObject implements IPhysicsEntity {

    // region Fields

    @Getter
    private final List<EntityPlayerMP> watchingPlayers = new ArrayList<>();
    private final Set<INodeController> physicsControllers = ConcurrentHashMap.newKeySet();
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
    private boolean needsCollisionCacheUpdate = true;

    private boolean shipAligningToGrid = false;
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
    private ShipData shipData;

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
    public PhysicsObject(World world, ShipData initial, boolean firstTimeCreated) {
        // QueryableShipData.get(world).registerUpdateListener(this::shipDataUpdateListener);
        this.world = world;
        this.shipData = initial;
        this.referenceBlockPos = getData().getChunkClaim().getRegionCenter();
        this.physicsControllersImmutable = Collections.unmodifiableSet(this.physicsControllers);
        this.claimedChunkCache = new ClaimedChunkCacheController(this, !firstTimeCreated);
        this.cachedSurroundingChunks = new SurroundingChunkCacheController(this);
        this.voxelFieldAABBMaker = new NaiveVoxelFieldAABBMaker(referenceBlockPos.getX(),
            referenceBlockPos.getZ());
        this.centerOfMassProvider = new BasicCenterOfMassProvider(initial.getInertiaData());
        this.shipTransformationManager = new ShipTransformationManager(this,
            getData().getShipTransform());
        this.physicsCalculations = new PhysicsCalculations(this);
        // Note how this is last.
        if (world.isRemote) {
            this.shipRenderer = new PhysObjectRenderManager(this, referenceBlockPos);
        } else {
            this.shipRenderer = null;
            if (!firstTimeCreated) {
                this.getShipTransformationManager()
                    .updateAllTransforms(this.getData().getShipTransform(), true, true);
                Objects.requireNonNull(shipData.getBlockPositions())
                    .forEach(voxelFieldAABBMaker::addVoxel);
            }
        }
    }

    public ShipData getData() {
        return shipData;
    }

    public void onSetBlockState(IBlockState oldState, IBlockState newState, BlockPos posAt) {
        // If the world is remote, or the block is not within the claimed chunks, ignore it!
        if (getWorld().isRemote || !getData().getChunkClaim().containsBlock(posAt)) {
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

    public void assembleShip(EntityPlayer player, SpatialDetector detector,
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
        getData().setPhysInfuserPos(getData().getPhysInfuserPos().add(centerDifference));

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

        // Then we destroy all the blocks we copied
        iter = detector.foundSet.iterator();
        while (iter.hasNext()) {
            int i = iter.next();
            SpatialDetector.setPosWithRespectTo(i, centerInWorld, pos);
            getWorld().setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
        }

        // We NEED this to fix ship lighting. If this code was removed then ships would have lighting artifacts all
        // over them.
        for (int x = getOwnedChunks().minX(); x <= getOwnedChunks().maxX(); x++) {
            for (int z = getOwnedChunks().minZ(); z <= getOwnedChunks().maxZ(); z++) {
                claimedChunkCache.getChunkAt(x, z).checkLight();
            }
        }

        // lol
        // getWrapperEntity().posX += .5;
        // getWrapperEntity().posY += .5;
        // getWrapperEntity().posZ += .5;

        // Some extra ship crap at the end.
        detectBlockPositions();

        // Note that this updates the ShipData transform.
        getPhysicsCalculations().updateParentCenterOfMass();
        // This puts the updated ShipData transform into the transformation manager. It also creates the ship bounding
        // box (Which is stored into ShipData).
        this.getShipTransformationManager()
            .updateAllTransforms(this.getData().getShipTransform(), true, true);
        /*
        Polygon polygon = new Polygon(bbInShipSpace,
            getShipTransformationManager().getCurrentTickTransform(),
            TransformType.SUBSPACE_TO_GLOBAL);
        getData().setShipBB(polygon.getEnclosedAABB());
        */
    }

    public void preloadNewPlayers() {
        Set<EntityPlayerMP> newWatchers = getPlayersThatJustWatched();
        for (Chunk[] chunkArray : claimedChunkCache.getCacheArray()) {
            for (Chunk chunk : chunkArray) {
                SPacketChunkData data = new SPacketChunkData(chunk, 65535);
                for (EntityPlayerMP player : newWatchers) {
                    player.connection.sendPacket(data);
                    ((WorldServer) getWorld()).getEntityTracker()
                        .sendLeashedEntitiesInChunk(player, chunk);
                }
            }
        }
        SpawnPhysObjMessage physObjMessage = new SpawnPhysObjMessage();
        physObjMessage.initializeData(getData());
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
        for (int x = getOwnedChunks().minX(); x <= getOwnedChunks().maxX(); x++) {
            for (int z = getOwnedChunks().minZ(); z <= getOwnedChunks().maxZ(); z++) {
                SPacketUnloadChunk unloadPacket = new SPacketUnloadChunk(x, z);
                ((EntityPlayerMP) untracking).connection.sendPacket(unloadPacket);
            }
        }
    }

    public VSChunkClaim getOwnedChunks() {
        return getData().getChunkClaim();
    }

    /**
     * Called when this entity has been unloaded from the world
     */
    public void onThisUnload() {
        if (!getWorld().isRemote) {
            unloadShipChunksFromWorld();
        } else {
            getShipRenderer().killRenderers();
        }
    }

    public void unloadShipChunksFromWorld() {
        ChunkProviderServer provider = (ChunkProviderServer) getWorld().getChunkProvider();
        for (int x = getOwnedChunks().minX(); x <= getOwnedChunks().maxX(); x++) {
            for (int z = getOwnedChunks().minZ(); z <= getOwnedChunks().maxZ(); z++) {
                provider.queueUnload(claimedChunkCache.getChunkAt(x, z));
            }
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

    public void onTick() {
        updateChunkCache();
        if (!getWorld().isRemote) {
            TileEntity te = getWorld().getTileEntity(getData().getPhysInfuserPos());
            boolean shouldDeconstructShip;

            if (te instanceof TileEntityPhysicsInfuser) {
                TileEntityPhysicsInfuser physicsCore = (TileEntityPhysicsInfuser) te;
                // Mark for deconstruction
                shouldDeconstructShip =
                    !physicsCore.canMaintainShip() || physicsCore.isTryingToDisassembleShip();
                shipAligningToGrid =
                    !physicsCore.canMaintainShip() || physicsCore.isTryingToAlignShip();
                getData().setPhysicsEnabled(!physicsCore.canMaintainShip() ||
                    physicsCore.isPhysicsEnabled());
            } else {
                // Mark for deconstruction
                shipAligningToGrid = true;
                shouldDeconstructShip = true;
                getData().setPhysicsEnabled(false);
            }

            // getData().setPhysicsEnabled(false);

            if (shouldDeconstructShip) {
                this.tryToDeconstructShip();
            }
        }

        this.setNeedsCollisionCacheUpdate(true);

        if (!world.isRemote) {
            ShipTransform physicsTransform = getShipTransformationManager()
                .getCurrentPhysicsTransform();
            getShipTransformationManager().updateAllTransforms(physicsTransform, false, true);
            getData().setShipTransform(getShipTransformationManager().getCurrentTickTransform());
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

    public void updateChunkCache() {
        cachedSurroundingChunks.updateChunkCache();
    }

    // Generates the blockPos array; must be loaded DIRECTLY after the chunks are
    // setup
    public void detectBlockPositions() {
        // int minChunkX = claimedChunks[0][0].x;
        // int minChunkZ = claimedChunks[0][0].z;
        int chunkX, chunkZ, index, x, y, z;
        Chunk chunk;
        ExtendedBlockStorage storage;
        Chunk[][] claimedChunks = claimedChunkCache.getCacheArray();
        int airStateIndex = Block.getStateId(Blocks.AIR.getDefaultState());

        for (chunkX = claimedChunks.length - 1; chunkX > -1; chunkX--) {
            for (chunkZ = claimedChunks[0].length - 1; chunkZ > -1; chunkZ--) {
                chunk = claimedChunks[chunkX][chunkZ];
                if (chunk != null) {
                    for (index = 0; index < 16; index++) {
                        storage = chunk.getBlockStorageArray()[index];
                        if (storage != null) {
                            for (y = 0; y < 16; y++) {
                                for (x = 0; x < 16; x++) {
                                    for (z = 0; z < 16; z++) {
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
    public boolean canShipBeDeconstructed() {
        // The quaternion with the ship's orientation
        Quaterniondc shipQuat = getShipTransformationManager().getCurrentTickTransform()
            .rotationQuaternion(TransformType.SUBSPACE_TO_GLOBAL);
        // Only allow a ship to be deconstructed if the angle between the grid and its orientation is less than half a degree.
        return Math.toDegrees(shipQuat.angle()) < .5;
    }

    public void tryToDeconstructShip() {
        // First check if the ship orientation is close to that of the grid; if it isn't then don't let this ship deconstruct.
        if (!canShipBeDeconstructed()) {
            return;
        }

        // We're pretty close to the grid; time 2 go.
        MutableBlockPos newPos = new MutableBlockPos();

        ShipTransform currentTransform = getShipTransformationManager().getCurrentTickTransform();
        Vector centerCoord = new Vector(currentTransform.getCenterCoord());
        Vector position = new Vector(currentTransform.getPosX(), currentTransform.getPosY(),
            currentTransform.getPosZ());

        BlockPos centerDifference = new BlockPos(
            Math.round(getCenterCoord().x - position.x),
            Math.round(getCenterCoord().y - position.y),
            Math.round(getCenterCoord().z - position.z));
        // First copy all the blocks from ship to world.

        for (BlockPos oldPos : this.getBlockPositions()) {
            newPos.setPos(oldPos.getX() - centerDifference.getX(),
                oldPos.getY() - centerDifference.getY(), oldPos.getZ() - centerDifference.getZ());
            MoveBlocks.copyBlockToPos(getWorld(), oldPos, newPos, Optional.empty());
        }

        // Just delete the tile entities in ship to prevent any dupe bugs.
        for (BlockPos oldPos : this.getBlockPositions()) {
            getWorld().removeTileEntity(oldPos);
        }

        // Delete old blocks. TODO: Used to use EMPTYCHUNK to do this but that causes crashes?
        getOwnedChunks().forEach((x, z) -> {
            Chunk chunk = new Chunk(getWorld(), x, z);
            chunk.setTerrainPopulated(true);
            chunk.setLightPopulated(true);
            claimedChunkCache.injectChunkIntoWorldServer(chunk, x, z, true);
            claimedChunkCache.setChunkAt(x, z, chunk);
        });
        // TODO:
        this.destroy();
    }

    @Deprecated
    public void destroy() {
        List<EntityPlayerMP> watchersCopy = new ArrayList<EntityPlayerMP>(getWatchingPlayers());
        for (int x = getOwnedChunks().minX(); x <= getOwnedChunks().maxX(); x++) {
            for (int z = getOwnedChunks().minZ(); z <= getOwnedChunks().maxZ(); z++) {
                SPacketUnloadChunk unloadPacket = new SPacketUnloadChunk(x, z);
                for (EntityPlayerMP wachingPlayer : watchersCopy) {
                    wachingPlayer.connection.sendPacket(unloadPacket);
                }
            }
            // NOTICE: This method isnt being called to avoid the
            // watchingPlayers.remove(player) call, which is a waste of CPU time
            // onPlayerUntracking(wachingPlayer);
        }
        getWatchingPlayers().clear();
    }

    public Vector getCenterCoord() {
        return new Vector(this.getData().getShipTransform().getCenterCoord());
    }

    public boolean isShipAligningToGrid() {
        return this.shipAligningToGrid;
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

    // region Encapsulation Code

    public ShipTransform getTransform() {
        return this.getData().getShipTransform();
    }

    public void updateTransform(ShipTransform transform) {
        this.getData().setShipTransform(transform);
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
        return getData().getShipBB();
    }

    public void setShipBoundingBox(AxisAlignedBB shipBoundingBox) {
        getData().setShipBB(shipBoundingBox);
    }

    @Nullable
    public Chunk getNearbyChunk(int x, int z) {
        int minChunkX = cachedSurroundingChunks.getCachedChunks().chunkX;
        int minChunkZ = cachedSurroundingChunks.getCachedChunks().chunkZ;
        Chunk[][] chunks = cachedSurroundingChunks.getCachedChunks().chunkArray;
        if (x < minChunkX || x >= minChunkX + chunks.length || z < minChunkZ
            || z >= minChunkZ + chunks[0].length) {
            return null;
        }
        return chunks[x - minChunkX][z - minChunkZ];
    }
}
