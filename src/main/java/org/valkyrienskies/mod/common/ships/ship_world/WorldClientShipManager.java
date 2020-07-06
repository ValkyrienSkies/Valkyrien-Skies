package org.valkyrienskies.mod.common.ships.ship_world;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.config.VSConfig;
import org.valkyrienskies.mod.common.ships.QueryableShipData;
import org.valkyrienskies.mod.common.ships.ShipData;
import org.valkyrienskies.mod.common.util.multithreaded.CalledFromWrongThreadException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class WorldClientShipManager implements IPhysObjectWorld {

    private final World world;
    private final Map<UUID, PhysicsObject> loadedShips;
    private final ConcurrentLinkedQueue<UUID> loadQueue, unloadQueue;
    private ImmutableList<PhysicsObject> threadSafeLoadedShips;

    public WorldClientShipManager(World world) {
        this.world = world;
        this.loadedShips = new HashMap<>();
        this.loadQueue = new ConcurrentLinkedQueue<>();
        this.unloadQueue = new ConcurrentLinkedQueue<>();
        this.threadSafeLoadedShips = ImmutableList.of();
    }

    private void enforceGameThread() throws CalledFromWrongThreadException {
        if (!Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
            throw new CalledFromWrongThreadException("Wrong thread calling code: " + Thread.currentThread());
        }
    }

    @Override
    public void tick() {
        loadAndUnloadShips();

        for (PhysicsObject physicsObject : getAllLoadedPhysObj()) {
            physicsObject.getShipTransformationManager()
                .updateAllTransforms(physicsObject.getShipData().getShipTransform(), false, false);
        }

        // Update the thread safe ship list.
        this.threadSafeLoadedShips = ImmutableList.copyOf(loadedShips.values());
    }

    private void loadAndUnloadShips() {
        QueryableShipData queryableShipData = QueryableShipData.get(world);
        // Load ships queued for loading
        while (!loadQueue.isEmpty()) {
            UUID toLoadID = loadQueue.remove();
            if (loadedShips.containsKey(toLoadID)) {
                throw new IllegalStateException("Tried loading a for ship that was already loaded? UUID is\n" + toLoadID);
            }
            Optional<ShipData> toLoadOptional = queryableShipData.getShip(toLoadID);
            if (!toLoadOptional.isPresent()) {
                throw new IllegalStateException("No ship found for UUID:\n" + toLoadID);
            }
            ShipData shipData = toLoadOptional.get();
            PhysicsObject physicsObject = new PhysicsObject(world, shipData);
            loadedShips.put(toLoadID, physicsObject);
            if (VSConfig.showAnnoyingDebugOutput) {
                System.out.println("Successfully loaded " + shipData);
            }
        }

        // Unload ships queued for unloading
        while (!unloadQueue.isEmpty()) {
            UUID toUnloadID = unloadQueue.remove();

            if (!loadedShips.containsKey(toUnloadID)) {
                throw new IllegalStateException("Tried unloading that isn't loaded? ID is\n" + toUnloadID);
            }
            PhysicsObject removedShip = loadedShips.get(toUnloadID);
            removedShip.unload();
            loadedShips.remove(toUnloadID);
            if (VSConfig.showAnnoyingDebugOutput) {
                System.out.println("Successfully unloaded " + removedShip.getShipData());
            }
        }
    }

    @Override
    public void onWorldUnload() {
        loadedShips.clear();
    }

    @Nullable
    @Override
    public PhysicsObject getPhysObjectFromUUID(@Nonnull UUID shipID) throws CalledFromWrongThreadException {
        enforceGameThread();
        return loadedShips.get(shipID);
    }

    @Nonnull
    @Override
    public List<PhysicsObject> getNearbyPhysObjects(@Nonnull AxisAlignedBB toCheck) throws CalledFromWrongThreadException {
        enforceGameThread();
        List<PhysicsObject> nearby = new ArrayList<>();
        for (PhysicsObject physicsObject : getAllLoadedPhysObj()) {
            if (toCheck.intersects(physicsObject.getShipBB())) {
                nearby.add(physicsObject);
            }
        }
        return nearby;
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

    @Override
    public void queueShipLoad(@Nonnull UUID shipID) {
        loadQueue.add(shipID);
    }

    @Override
    public void queueShipUnload(@Nonnull UUID shipID) {
        unloadQueue.add(shipID);
    }

    @Nonnull
    @Override
    public World getWorld() {
        return world;
    }
}
