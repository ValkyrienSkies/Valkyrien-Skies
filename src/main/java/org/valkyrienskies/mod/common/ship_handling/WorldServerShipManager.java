package org.valkyrienskies.mod.common.ship_handling;

import gnu.trove.iterator.TIntIterator;
import lombok.Getter;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.ChunkProviderServer;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.config.VSConfig;
import org.valkyrienskies.mod.common.multithreaded.VSThread;
import org.valkyrienskies.mod.common.network.ShipIndexDataMessage;
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
    private final Map<ShipData, PhysicsObject> loadedShips;
    private final ConcurrentLinkedQueue<ShipData> spawnQueue, loadQueue, unloadQueue, backgroundLoadQueue;
    private final Set<ShipData> loadingInBackground;

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
    public PhysicsObject getPhysObjectFromData(ShipData data) {
        return loadedShips.get(data);
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
        for (PhysicsObject physicsObject : getAllLoadedPhysObj()) {
            if (physicsObject.shouldShipBeDestroyed()) {
                // Copy ship blocks to the world
                physicsObject.destroyShip();
                // Then remove the ship from the world
                QueryableShipData.get(world).removeShip(physicsObject.getShipData());
                boolean success = this.loadedShips.remove(physicsObject.getShipData(), physicsObject);
                if (!success) {
                    throw new IllegalStateException("Ship destruction failed!\n" + physicsObject.getShipData());
                }
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

        // Finally, send updates to nearby players.
        ShipIndexDataMessage indexDataMessage = new ShipIndexDataMessage();
        indexDataMessage.addDataToMessage(QueryableShipData.get(world));
        ValkyrienSkiesMod.physWrapperNetwork.sendToDimension(indexDataMessage, world.provider.getDimension());
    }

    private void spawnNewShips() {
        while (!spawnQueue.isEmpty()) {
            ShipData toSpawn = spawnQueue.remove();

            if (loadedShips.containsKey(toSpawn)) {
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
            TIntIterator blocksIterator = detector.foundSet.iterator();
            BlockPos.MutableBlockPos tempPos = new BlockPos.MutableBlockPos();
            BlockPos centerDifference = toSpawn.getChunkClaim().getRegionCenter().subtract(physicsInfuserPos);
            while (blocksIterator.hasNext()) {
                int hashedPos = blocksIterator.next();
                SpatialDetector.setPosWithRespectTo(hashedPos, detector.firstBlock, tempPos);

                int chunkX = (tempPos.getX() + centerDifference.getX()) >> 4;
                int chunkZ = (tempPos.getZ() + centerDifference.getZ()) >> 4;

                toSpawn.getChunkClaim().addChunkClaim(chunkX, chunkZ);
            }

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


            PhysicsObject physicsObject = new PhysicsObject(world, toSpawn, true);

            physicsObject.assembleShip(null, detector, physicsInfuserPos);

            loadedShips.put(toSpawn, physicsObject);
        }
    }

    private void loadAndUnloadShips() {
        // Load the ships that are required immediately.
        while (!loadQueue.isEmpty()) {
            ShipData toLoad = loadQueue.remove();
            if (loadedShips.containsKey(toLoad)) {
                throw new IllegalStateException("Tried loading a ShipData that was already loaded?\n" + toLoad);
            }
            if (loadingInBackground.contains(toLoad)) {
                // Ship was being loaded in the background, finish loading here
                // For now background loading just means loading the chunks in the background.
                loadingInBackground.remove(toLoad);
            }
            System.out.println("Attempting to load " + toLoad);
            PhysicsObject physicsObject = new PhysicsObject(world, toLoad, false);
            PhysicsObject old = loadedShips.put(toLoad, physicsObject);
            if (old != null) {
                throw new IllegalStateException("How did we already have a ship loaded for " + toLoad);
            }
        }

        // Create background tasks to load ships that aren't required immediately.
        while (!backgroundLoadQueue.isEmpty()) {
            ShipData toLoad = backgroundLoadQueue.remove();
            if (loadedShips.containsKey(toLoad)) {
                // continue; // temp, need to fix WorldShipLoadingController.determineLoadAndUnload()
                throw new IllegalStateException("Tried loading a ShipData that was already loaded?\n" + toLoad);
            }
            if (loadingInBackground.contains(toLoad)) {
                continue; // Already loading this ship in the background
            }
            loadingInBackground.add(toLoad);
            System.out.println("Attempting to load " + toLoad + " in the background.");

            ChunkProviderServer chunkProviderServer = world.getChunkProvider();

            for (ChunkPos chunkPos : toLoad.getChunkClaim()) {
                Runnable returnTask = () -> {
                    System.out.println("Loaded ship chunk " + chunkPos);
                }; // Just anything that's not null.
                chunkProviderServer.loadChunk(chunkPos.x, chunkPos.z, returnTask);
            }
            // PhysicsObject physicsObject = new PhysicsObject(world, toLoad, false);
        }

        // Unload far away ships immediately.
        while (!unloadQueue.isEmpty()) {
            ShipData toUnload = unloadQueue.remove();
            if (!loadedShips.containsKey(toUnload)) {
                throw new IllegalStateException("Tried unloading a ShipData that isn't loaded?\n" + toUnload);
            }
            PhysicsObject physicsObject = getPhysObjectFromData(toUnload);
            if (physicsObject == null) {
                throw new IllegalStateException("Tried unloading a ShipData that has a null PhysicsObject?\n" + toUnload);
            }
            System.out.println("Attempting to unload " + toUnload);
            boolean success = loadedShips.remove(toUnload, physicsObject);
            if (!success) {
                throw new IllegalStateException("How did we fail to unload " + toUnload);
            }
        }
    }

    @Nonnull
    @Override
    public Iterable<PhysicsObject> getAllLoadedPhysObj() {
        return loadedShips.values();
    }

    /**
     * Thread safe way to queue a ship spawn. (Not the same as {@link #queueShipLoad(ShipData)}.
     */
    public void queueShipSpawn(@Nonnull ShipData data) {
        this.spawnQueue.add(data);
    }

    @Override
    public void queueShipLoad(@Nonnull ShipData data) {
        this.loadQueue.add(data);
    }

    @Override
    public void queueShipUnload(@Nonnull ShipData data) {
        this.unloadQueue.add(data);
    }

    /**
     * Thread safe way to queue a ship to be loaded in the background.
     */
    public void queueShipLoadBackground(@Nonnull ShipData data) {
        backgroundLoadQueue.add(data);
    }

    /**
     * Used to prevent the world from unloading the chunks of ships loading in background.
     */
    public Iterable<Long> getBackgroundShipChunks() {
        List<Long> backgroundChunks = new ArrayList<>();
        for (ShipData data : loadingInBackground) {
            backgroundChunks.addAll(data.getChunkClaim().getClaimedChunks());
        }
        return backgroundChunks;
    }
}
