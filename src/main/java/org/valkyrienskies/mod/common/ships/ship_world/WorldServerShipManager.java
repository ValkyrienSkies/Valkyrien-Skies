package org.valkyrienskies.mod.common.ships.ship_world;

import com.google.common.collect.ImmutableList;
import gnu.trove.iterator.TIntIterator;
import lombok.Getter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.ChunkProviderServer;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.valkyrienskies.mod.common.config.VSConfig;
import org.valkyrienskies.mod.common.physics.BlockPhysicsDetails;
import org.valkyrienskies.mod.common.ships.QueryableShipData;
import org.valkyrienskies.mod.common.ships.ShipData;
import org.valkyrienskies.mod.common.ships.block_relocation.BlockFinder;
import org.valkyrienskies.mod.common.ships.block_relocation.IRelocationAwareTile;
import org.valkyrienskies.mod.common.ships.block_relocation.SpatialDetector;
import org.valkyrienskies.mod.common.ships.physics_data.BasicCenterOfMassProvider;
import org.valkyrienskies.mod.common.ships.physics_data.IPhysicsObjectCenterOfMassProvider;
import org.valkyrienskies.mod.common.util.multithreaded.CalledFromWrongThreadException;
import org.valkyrienskies.mod.common.util.multithreaded.VSThread;

import javax.annotation.Nonnull;
import java.util.*;

public class WorldServerShipManager implements IPhysObjectWorld {

    @Getter
    private final WorldServer world;
    @Getter
    private final VSThread physicsThread;
    private final WorldShipLoadingController loadingController;
    private final Map<UUID, PhysicsObject> loadedShips;
    // Use LinkedHashSet as a queue because it preserves order and doesn't allow duplicates
    private final LinkedHashSet<ImmutableTriple<BlockPos, ShipData, BlockFinder.BlockFinderType>> spawnQueue;
    private final LinkedHashSet<UUID> loadQueue, unloadQueue, backgroundLoadQueue;
    private final Set<UUID> loadingInBackground;
    private ImmutableList<PhysicsObject> threadSafeLoadedShips;

    public WorldServerShipManager(World world) {
        this.world = (WorldServer) world;
        this.physicsThread = new VSThread(world);
        this.loadingController = new WorldShipLoadingController(this);
        this.loadedShips = new HashMap<>();
        this.spawnQueue = new LinkedHashSet<>();
        this.loadQueue = new LinkedHashSet<>();
        this.unloadQueue = new LinkedHashSet<>();
        this.backgroundLoadQueue = new LinkedHashSet<>();
        this.loadingInBackground = new HashSet<>();
        this.threadSafeLoadedShips = ImmutableList.of();
        this.physicsThread.start();
    }

    private void enforceGameThread() {
        if (!world.isCallingFromMinecraftThread()) {
            throw new CalledFromWrongThreadException("Wrong thread calling code: " + Thread.currentThread());
        }
    }

    @Override
    public void onWorldUnload() {
        this.physicsThread.kill();
    }

    @Override
    public PhysicsObject getPhysObjectFromUUID(@Nonnull UUID shipID) throws CalledFromWrongThreadException {
        enforceGameThread();
        return loadedShips.get(shipID);
    }

    @Nonnull
    @Override
    public List<PhysicsObject> getPhysObjectsInAABB(@Nonnull AxisAlignedBB toCheck) throws CalledFromWrongThreadException {
        enforceGameThread();
        List<PhysicsObject> nearby = new ArrayList<>();
        for (PhysicsObject ship : getAllLoadedPhysObj()) {
            if (toCheck.intersects(ship.getShipBB())) {
                nearby.add(ship);
            }
        }
        return nearby;
    }

    public void tick() {
        // First destroy any ships that want to be destroyed (copy blocks from ship to world, and then unload)
        Iterator<Map.Entry<UUID, PhysicsObject>> iterator = loadedShips.entrySet().iterator();
        while (iterator.hasNext()) {
            PhysicsObject physicsObject = iterator.next().getValue();
            if (physicsObject.shouldShipBeDestroyed()) {
                // Copy ship blocks to the world
                physicsObject.destroyShip();
                // Then remove the ship from the world, and the ship map.
                QueryableShipData.get(world).removeShip(physicsObject.getShipData());
                iterator.remove();
            }
        }

        // Then execute queued ship spawn operations
        spawnNewShips();

        // Then determine which ships to load and unload
        loadingController.determineLoadAndUnload();

        // Then execute queued ship load and unload operations
        loadAndUnloadShips();

        // Then tick all the loaded ships
        for (PhysicsObject ship : getAllLoadedPhysObj()) {
            ship.onTick();
        }

        // Finally, send the players updates about the ships.
        loadingController.sendUpdatesToPlayers();

        // And then update the thread safe ship list.
        this.threadSafeLoadedShips = ImmutableList.copyOf(loadedShips.values());
    }

    private void spawnNewShips() {
        for (final ImmutableTriple<BlockPos, ShipData, BlockFinder.BlockFinderType> spawnData : spawnQueue) {
            final BlockPos physicsInfuserPos = spawnData.getLeft();
            final ShipData toSpawn = spawnData.getMiddle();
            final BlockFinder.BlockFinderType blockBlockFinderType = spawnData.getRight();

            if (loadedShips.containsKey(toSpawn.getUuid())) {
                throw new IllegalStateException("Tried spawning a ShipData that was already loaded?\n" + toSpawn);
            }

            final SpatialDetector detector = BlockFinder.getBlockFinderFor(
                    blockBlockFinderType,
                    physicsInfuserPos,
                    world,
                    VSConfig.maxDetectedShipSize + 1,
                    true
            );

            if (VSConfig.showAnnoyingDebugOutput) {
                System.out.println("Attempting to spawn " + toSpawn + " on the thread " + Thread.currentThread().getName());
            }
            if (detector.foundSet.size() > VSConfig.maxDetectedShipSize || detector.cleanHouse) {
                System.err.println("Ship too big or bedrock detected!");

                /*
                if (creator != null) {
                    creator.sendMessage(new TextComponentString(
                            "Ship construction canceled because its exceeding the ship size limit; "
                                    +
                                    "or because it's attached to bedrock. " +
                                    "Raise it with /physsettings maxshipsize [number]"));
                }

                 */
                continue; // Skip ship construction
            }

            // Fill the chunk claims
            int radius = 7;

            // TEMP CODE
            // Eventually want to create mechanisms that control how many chunks are allocated to a ship
            // But for now, lets just give them a bunch of chunks.
            ChunkPos centerPos = toSpawn.getChunkClaim().getCenterPos();
            for (int chunkX = -radius; chunkX <= radius; chunkX++) {
                for (int chunkZ = -radius; chunkZ <= radius; chunkZ++) {
                    toSpawn.getChunkClaim().addChunkClaim(centerPos.x + chunkX, centerPos.z + chunkZ);
                }
            }

            // When copying the ship chunks we want to keep track of the inertia and center of mass.
            IPhysicsObjectCenterOfMassProvider centerOfMassProvider = new BasicCenterOfMassProvider();

            // Then create the ship chunks
            MutableBlockPos srcLocationPos = new MutableBlockPos();
            BlockPos centerDifference = toSpawn.getChunkClaim().getRegionCenter().subtract(physicsInfuserPos);

            MutableBlockPos pasteLocationPos = new MutableBlockPos();

            Map<Long, Chunk> copiedChunksMap = new HashMap<>();

            // First, copy the blocks and tiles to the new chunks
            TIntIterator blocksIterator = detector.foundSet.iterator();
            while (blocksIterator.hasNext()) {
                int hashedPos = blocksIterator.next();
                SpatialDetector.setPosWithRespectTo(hashedPos, detector.firstBlock, srcLocationPos);
                // Get the BlockPos from the hashedPos
                pasteLocationPos.setPos(srcLocationPos.getX() + centerDifference.getX(), srcLocationPos.getY() + centerDifference.getY(), srcLocationPos.getZ() + centerDifference.getZ());

                // Then add it to the ShipData block positions set
                toSpawn.blockPositions.add(pasteLocationPos.getX(), pasteLocationPos.getY(), pasteLocationPos.getZ());

                // Then create a chunk to accommodate this block (if one does not already exist).
                int newChunkX = pasteLocationPos.getX() >> 4;
                int newChunkZ = pasteLocationPos.getZ() >> 4;

                long newChunkPosLong = ChunkPos.asLong(newChunkX, newChunkZ);

                if (!copiedChunksMap.containsKey(newChunkPosLong)) {
                    Chunk chunk = new Chunk(world, newChunkX, newChunkZ);
                    copiedChunksMap.put(newChunkPosLong, chunk);
                }

                // Then copy the IBlockState & TileEntity to the new Chunk
                // Get the old world Chunk
                Chunk chunkToSet = world.getChunk(srcLocationPos);

                // Get the new Chunk
                Chunk newChunk = copiedChunksMap.get(newChunkPosLong);

                // Then get the old IBlockState, as efficiently as possible
                int storageIndex = srcLocationPos.getY() >> 4;
                // Check that we're placing the block in a valid position
                if (storageIndex < 0 || storageIndex >= chunkToSet.storageArrays.length) {
                    // Invalid position, abort!
                    throw new IllegalStateException("Incorrect block copy!\n" + srcLocationPos);
                }

                IBlockState srcState = chunkToSet.storageArrays[storageIndex]
                        .get(srcLocationPos.getX() & 15, srcLocationPos.getY() & 15, srcLocationPos.getZ() & 15);

                // Then paste that IBlockState into the new ship chunk
                int newChunkStorageIndex = pasteLocationPos.getY() >> 4;

                if (newChunk.storageArrays[newChunkStorageIndex] == Chunk.NULL_BLOCK_STORAGE) {
                    newChunk.storageArrays[newChunkStorageIndex] = new ExtendedBlockStorage(newChunkStorageIndex << 4,
                            true);
                }
                newChunk.storageArrays[newChunkStorageIndex]
                        .set(pasteLocationPos.getX() & 15, pasteLocationPos.getY() & 15, pasteLocationPos.getZ() & 15, srcState);

                // If this block is force block, then add it to the activeForcePositions list of the ship.
                if (BlockPhysicsDetails.isBlockProvidingForce(srcState)) {
                    toSpawn.activeForcePositions.add(pasteLocationPos);
                }

                // Also update the center of mass and inertia provider
                centerOfMassProvider.onSetBlockState(toSpawn.getInertiaData(), pasteLocationPos, Blocks.AIR.getDefaultState(), srcState);

                // Then copy the TileEntity (if there is one)
                TileEntity srcTile = world.getTileEntity(srcLocationPos);
                if (srcTile != null) {
                    TileEntity pasteTile;
                    if (srcTile instanceof IRelocationAwareTile) {
                        pasteTile = ((IRelocationAwareTile) srcTile).createRelocatedTile(pasteLocationPos, toSpawn);
                    } else {
                        NBTTagCompound tileEntNBT = srcTile.writeToNBT(new NBTTagCompound());
                        // Change the block position to be inside of the Ship
                        tileEntNBT.setInteger("x", pasteLocationPos.getX());
                        tileEntNBT.setInteger("y", pasteLocationPos.getY());
                        tileEntNBT.setInteger("z", pasteLocationPos.getZ());
                        pasteTile = TileEntity.create(world, tileEntNBT);
                    }

                    // Finally, add the new TileEntity to the new ship chunk.
                    newChunk.addTileEntity(pasteTile);
                }
            }

            // Then delete the copied blocks from the old chunks
            blocksIterator = detector.foundSet.iterator();
            while (blocksIterator.hasNext()) {
                int hashedPos = blocksIterator.next();
                SpatialDetector.setPosWithRespectTo(hashedPos, detector.firstBlock, srcLocationPos);

                Chunk chunkToSet = world.getChunk(srcLocationPos);

                // Then get the old IBlockState, as efficiently as possible
                int storageIndex = srcLocationPos.getY() >> 4;
                // Check that we're placing the block in a valid position
                if (storageIndex < 0 || storageIndex >= chunkToSet.storageArrays.length) {
                    // Invalid position, abort!
                    throw new IllegalStateException("Incorrect block copy!\n" + srcLocationPos);
                }

                IBlockState srcState = chunkToSet.storageArrays[storageIndex]
                        .get(srcLocationPos.getX() & 15, srcLocationPos.getY() & 15, srcLocationPos.getZ() & 15);

                // THIS IS TEMP because its extremely inefficient.
                // Come up with a clever way to let clients figure this out in the future.
                world.notifyBlockUpdate(srcLocationPos, srcState, Blocks.AIR.getDefaultState(), 3);

                // Finally, delete the old IBlockState and TileEntity from the old Chunk
                chunkToSet.storageArrays[storageIndex]
                        .set(srcLocationPos.getX() & 15, srcLocationPos.getY() & 15, srcLocationPos.getZ() & 15, Blocks.AIR.getDefaultState());

                // Delete the TileEntity at this pos (if there is one)
                world.removeTileEntity(srcLocationPos);

                chunkToSet.markDirty();
            }

            // Then inject the ship chunks into the world
            toSpawn.getChunkClaim().forEach((x, z) -> {
                long chunkLong = ChunkPos.asLong(x, z);
                if (copiedChunksMap.containsKey(chunkLong)) {
                    injectChunkIntoWorldServer(copiedChunksMap.get(chunkLong), x, z);
                } else {
                    injectChunkIntoWorldServer(new Chunk(world, x, z), x, z);
                }
            });

            // Add shipData to the ShipData storage
            QueryableShipData.get(world).addShip(toSpawn);

            // Finally, instantiate the PhysicsObject representation of this ShipData
            PhysicsObject physicsObject = new PhysicsObject(world, toSpawn);
            loadedShips.put(toSpawn.getUuid(), physicsObject);
        }
        spawnQueue.clear();
    }

    private void injectChunkIntoWorldServer(@Nonnull Chunk chunk, int x, int z) {
        ChunkProviderServer provider = world.getChunkProvider();
        chunk.dirty = true;
        chunk.setTerrainPopulated(true);
        chunk.setLightPopulated(true);
        chunk.onLoad();

        provider.loadedChunks.put(ChunkPos.asLong(x, z), chunk);
    }

    private void loadAndUnloadShips() {
        QueryableShipData queryableShipData = QueryableShipData.get(world);
        // Load the ships that are required immediately.
        for (final UUID toLoadID : loadQueue) {
            Optional<ShipData> toLoadOptional = queryableShipData.getShip(toLoadID);
            if (!toLoadOptional.isPresent()) {
                throw new IllegalStateException("No ship found for ID:\n" + toLoadID);
            }
            ShipData toLoad = toLoadOptional.get();
            if (loadedShips.containsKey(toLoadID)) {
                throw new IllegalStateException("Tried loading a ShipData that was already loaded?\n" + toLoad);
            }

            // Remove this ship from the background loading set, if it is in it.
            loadingInBackground.remove(toLoadID);
            // Finally, load the ship.
            if (VSConfig.showAnnoyingDebugOutput) {
                System.out.println("Attempting to load ship " + toLoad);
            }
            PhysicsObject physicsObject = new PhysicsObject(world, toLoad);
            PhysicsObject old = loadedShips.put(toLoad.getUuid(), physicsObject);
            if (old != null) {
                throw new IllegalStateException("How did we already have a ship loaded for " + toLoad);
            }
        }
        loadQueue.clear();

        // Load ships that aren't required immediately in the background.
        for (final UUID toLoadID : backgroundLoadQueue) {
            // Skip if this ship is already being loaded in the background,.
            if (loadingInBackground.contains(toLoadID)) {
                continue; // Already loading this ship in the background
            }

            // Make sure there isn't an already loaded ship with this UUID.
            if (loadedShips.containsKey(toLoadID)) {
                // continue; // temp, need to fix WorldShipLoadingController.determineLoadAndUnload()
                throw new IllegalStateException("Tried loading a ShipData that was already loaded? Ship ID is\n"
                        + toLoadID);
            }
            // Then try getting the ShipData for this UUID.
            Optional<ShipData> toLoadOptional = queryableShipData.getShip(toLoadID);
            if (!toLoadOptional.isPresent()) {
                throw new IllegalStateException("No ship found for ID:\n" + toLoadID);
            }

            ShipData toLoad = toLoadOptional.get();
            loadingInBackground.add(toLoadID);

            if (VSConfig.showAnnoyingDebugOutput) {
                System.out.println("Attempting to load " + toLoad + " in the background.");
            }
            ChunkProviderServer chunkProviderServer = world.getChunkProvider();
            for (ChunkPos chunkPos : toLoad.getChunkClaim()) {
                @Nonnull Runnable returnTask = () -> {
                    if (VSConfig.showAnnoyingDebugOutput) {
                        System.out.println("Loaded ship chunk " + chunkPos);
                    }
                };
                chunkProviderServer.loadChunk(chunkPos.x, chunkPos.z, returnTask);
            }
        }
        backgroundLoadQueue.clear();

        // Unload far away ships immediately.
        for (final UUID toUnloadID : unloadQueue) {
            // Make sure we have a ship with this ID that can be unloaded
            if (!loadedShips.containsKey(toUnloadID)) {
                throw new IllegalStateException("Tried unloading a ShipData that isn't loaded? Ship ID is\n"
                        + toUnloadID);
            }

            PhysicsObject physicsObject = getPhysObjectFromUUID(toUnloadID);

            if (VSConfig.showAnnoyingDebugOutput) {
                System.out.println("Attempting to unload " + physicsObject);
            }
            physicsObject.unload();
            boolean success = loadedShips.remove(toUnloadID, physicsObject);

            if (!success) {
                throw new IllegalStateException("How did we fail to unload " + physicsObject.getShipData());
            }
        }
        unloadQueue.clear();
    }

    @Nonnull
    @Override
    public Iterable<PhysicsObject> getAllLoadedPhysObj() throws CalledFromWrongThreadException {
        enforceGameThread();
        return loadedShips.values();
    }

    @Nonnull
    @Override
    public ImmutableList<PhysicsObject> getAllLoadedThreadSafe() {
        return threadSafeLoadedShips;
    }

    /**
     * Thread safe way to queue a ship spawn. (Not the same as {@link #queueShipLoad(UUID)}.
     */
    public void queueShipSpawn(@Nonnull ShipData data, @Nonnull BlockPos spawnPos, @Nonnull BlockFinder.BlockFinderType blockFinderType) {
        enforceGameThread();
        this.spawnQueue.add(ImmutableTriple.of(spawnPos, data, blockFinderType));
    }

    @Override
    public void queueShipLoad(@Nonnull UUID shipID) {
        enforceGameThread();
        this.loadQueue.add(shipID);
    }

    @Override
    public void queueShipUnload(@Nonnull UUID shipID) {
        enforceGameThread();
        this.unloadQueue.add(shipID);
    }

    /**
     * Thread safe way to queue a ship to be loaded in the background.
     */
    public void queueShipLoadBackground(@Nonnull UUID shipID) {
        enforceGameThread();
        backgroundLoadQueue.add(shipID);
    }

    /**
     * Used to prevent the world from unloading the chunks of ships loading in background.
     */
    public Iterable<Long> getBackgroundShipChunks() throws CalledFromWrongThreadException {
        enforceGameThread();
        List<Long> backgroundChunks = new ArrayList<>();
        QueryableShipData queryableShipData = QueryableShipData.get(world);
        for (UUID shipID : loadingInBackground) {
            Optional<ShipData> shipDataOptional = queryableShipData.getShip(shipID);
            if (!shipDataOptional.isPresent()) {
                throw new IllegalStateException("Ship data not present for:\n" + shipID);
            }
            backgroundChunks.addAll(shipDataOptional.get().getChunkClaim().getClaimedChunks());
        }
        return backgroundChunks;
    }
}
