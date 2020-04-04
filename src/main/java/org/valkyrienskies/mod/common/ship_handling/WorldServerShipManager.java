package org.valkyrienskies.mod.common.ship_handling;

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
import org.valkyrienskies.mod.common.config.VSConfig;
import org.valkyrienskies.mod.common.multithreaded.VSThread;
import org.valkyrienskies.mod.common.physics.management.BasicCenterOfMassProvider;
import org.valkyrienskies.mod.common.physics.management.IPhysicsObjectCenterOfMassProvider;
import org.valkyrienskies.mod.common.physmanagement.relocation.DetectorManager;
import org.valkyrienskies.mod.common.physmanagement.relocation.SpatialDetector;
import org.valkyrienskies.mod.common.physmanagement.shipdata.QueryableShipData;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class WorldServerShipManager implements IPhysObjectWorld {

    @Getter
    private final WorldServer world;
    @Getter
    private final VSThread physicsThread;
    private final WorldShipLoadingController loadingController;
    private final Map<UUID, PhysicsObject> loadedShips;
    private final ConcurrentLinkedQueue<ShipData> spawnQueue;
    private final ConcurrentLinkedQueue<UUID> loadQueue, unloadQueue, backgroundLoadQueue;
    private final Set<UUID> loadingInBackground;

    public WorldServerShipManager(World world) {
        this.world = (WorldServer) world;
        this.physicsThread = new VSThread(world);
        this.loadingController = new WorldShipLoadingController(this);
        this.loadedShips = new HashMap<>();
        this.spawnQueue = new ConcurrentLinkedQueue<>();
        this.loadQueue = new ConcurrentLinkedQueue<>();
        this.unloadQueue = new ConcurrentLinkedQueue<>();
        this.backgroundLoadQueue = new ConcurrentLinkedQueue<>();
        this.loadingInBackground = new HashSet<>();
        this.physicsThread.start();
    }

    @Override
    public void onWorldUnload() {
        this.physicsThread.kill();
    }

    @Override
    public PhysicsObject getPhysObjectFromUUID(UUID shipID) {
        return loadedShips.get(shipID);
    }

    @Nonnull
    @Override
    public List<PhysicsObject> getNearbyPhysObjects(AxisAlignedBB toCheck) {
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
    }

    private void spawnNewShips() {
        while (!spawnQueue.isEmpty()) {
            ShipData toSpawn = spawnQueue.remove();

            if (loadedShips.containsKey(toSpawn.getUuid())) {
                throw new IllegalStateException("Tried spawning a ShipData that was already loaded?\n" + toSpawn);
            }

            BlockPos physicsInfuserPos = toSpawn.getPhysInfuserPos();
            SpatialDetector detector =  DetectorManager.getDetectorFor(
                    DetectorManager.DetectorIDs.ShipSpawnerGeneral, physicsInfuserPos, world,
                    VSConfig.maxShipSize + 1, true);

            System.out.println("Hello! " + Thread.currentThread().getName());
            if (detector.foundSet.size() > VSConfig.maxShipSize || detector.cleanHouse) {
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
            TIntIterator blocksIterator = detector.foundSet.iterator();
            MutableBlockPos srcLocationPos = new MutableBlockPos();
            BlockPos centerDifference = toSpawn.getChunkClaim().getRegionCenter().subtract(physicsInfuserPos);

            MutableBlockPos pasteLocationPos = new MutableBlockPos();

            Map<Long, Chunk> copiedChunksMap = new HashMap<>();
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
                // Also update the center of mass and inertia provider
                centerOfMassProvider.onSetBlockState(toSpawn.getInertiaData(), pasteLocationPos, Blocks.AIR.getDefaultState(), srcState);

                // Then copy the TileEntity (if there is one)
                TileEntity srcTile = world.getTileEntity(srcLocationPos);
                if (srcTile != null) {
                    NBTTagCompound tileEntNBT = srcTile.writeToNBT(new NBTTagCompound());
                    // Change the block position to be inside of the Ship
                    tileEntNBT.setInteger("x", pasteLocationPos.getX());
                    tileEntNBT.setInteger("y", pasteLocationPos.getY());
                    tileEntNBT.setInteger("z", pasteLocationPos.getZ());
                    TileEntity pasteTile = TileEntity.create(world, tileEntNBT);

                    newChunk.addTileEntity(pasteTile);
                }

                // Finally, delete the old IBlockState and TileEntity from the old Chunk
                chunkToSet.storageArrays[storageIndex]
                        .set(srcLocationPos.getX() & 15, srcLocationPos.getY() & 15, srcLocationPos.getZ() & 15, Blocks.AIR.getDefaultState());

                chunkToSet.markDirty();
                if (srcTile != null) {
                    world.removeTileEntity(srcLocationPos);
                }

                // THIS IS TEMP because its extremely inefficient.
                // Come up with a clever way to let clients figure this out in the future.
                world.notifyBlockUpdate(srcLocationPos, srcState, Blocks.AIR.getDefaultState(), 3);

                // Not sure about this yet
                /*
                int chunkX = (tempPos.getX() + centerDifference.getX()) >> 4;
                int chunkZ = (tempPos.getZ() + centerDifference.getZ()) >> 4;
                toSpawn.getChunkClaim().addChunkClaim(chunkX, chunkZ);
                 */
            }

            toSpawn.getChunkClaim().forEach((x, z) -> {
                long chunkLong = ChunkPos.asLong(x, z);
                if (copiedChunksMap.containsKey(chunkLong)) {
                    injectChunkIntoWorldServer(copiedChunksMap.get(chunkLong), x, z);
                } else {
                    injectChunkIntoWorldServer(new Chunk(world, x, z), x, z);
                }
            });

            toSpawn.setPhysInfuserPos(toSpawn.getChunkClaim().getRegionCenter());

            PhysicsObject physicsObject = new PhysicsObject(world, toSpawn);

            // physicsObject.assembleShip(null, detector, physicsInfuserPos);

            // Add shipData to the ShipData storage
            QueryableShipData.get(world).addShip(toSpawn);
            loadedShips.put(toSpawn.getUuid(), physicsObject);
        }
    }

    private void injectChunkIntoWorldServer(Chunk chunk, int x, int z) {
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
        while (!loadQueue.isEmpty()) {
            UUID toLoadID = loadQueue.remove();

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
            System.out.println("Attempting to load ship " + toLoad);
            PhysicsObject physicsObject = new PhysicsObject(world, toLoad);
            PhysicsObject old = loadedShips.put(toLoad.getUuid(), physicsObject);
            if (old != null) {
                throw new IllegalStateException("How did we already have a ship loaded for " + toLoad);
            }
        }

        // Load ships that aren't required immediately in the background.
        while (!backgroundLoadQueue.isEmpty()) {
            UUID toLoadID = backgroundLoadQueue.remove();
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

            System.out.println("Attempting to load " + toLoad + " in the background.");
            ChunkProviderServer chunkProviderServer = world.getChunkProvider();
            for (ChunkPos chunkPos : toLoad.getChunkClaim()) {
                @Nonnull Runnable returnTask = () -> {
                    System.out.println("Loaded ship chunk " + chunkPos);
                };
                chunkProviderServer.loadChunk(chunkPos.x, chunkPos.z, returnTask);
            }
        }

        // Unload far away ships immediately.
        while (!unloadQueue.isEmpty()) {
            UUID toUnloadID = unloadQueue.remove();
            // Make sure we have a ship with this ID that can be unloaded
            if (!loadedShips.containsKey(toUnloadID)) {
                throw new IllegalStateException("Tried unloading a ShipData that isn't loaded? Ship ID is\n"
                        + toUnloadID);
            }

            PhysicsObject physicsObject = getPhysObjectFromUUID(toUnloadID);

            System.out.println("Attempting to unload " + physicsObject);
            physicsObject.unload();
            boolean success = loadedShips.remove(toUnloadID, physicsObject);

            if (!success) {
                throw new IllegalStateException("How did we fail to unload " + physicsObject.getShipData());
            }
        }
    }

    @Nonnull
    @Override
    public Iterable<PhysicsObject> getAllLoadedPhysObj() {
        return loadedShips.values();
    }

    /**
     * Thread safe way to queue a ship spawn. (Not the same as {@link #queueShipLoad(UUID)}.
     */
    public void queueShipSpawn(@Nonnull ShipData data) {
        this.spawnQueue.add(data);
    }

    @Override
    public void queueShipLoad(@Nonnull UUID shipID) {
        this.loadQueue.add(shipID);
    }

    @Override
    public void queueShipUnload(@Nonnull UUID shipID) {
        this.unloadQueue.add(shipID);
    }

    /**
     * Thread safe way to queue a ship to be loaded in the background.
     */
    public void queueShipLoadBackground(@Nonnull UUID shipID) {
        backgroundLoadQueue.add(shipID);
    }

    /**
     * Used to prevent the world from unloading the chunks of ships loading in background.
     */
    public Iterable<Long> getBackgroundShipChunks() {
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
