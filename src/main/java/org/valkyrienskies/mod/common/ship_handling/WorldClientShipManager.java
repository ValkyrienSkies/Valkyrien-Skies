package org.valkyrienskies.mod.common.ship_handling;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class WorldClientShipManager implements IPhysObjectWorld {

    private final World world;
    private final Map<ShipData, PhysicsObject> loadedShips;
    private final ConcurrentLinkedQueue<ShipData> loadQueue, unloadQueue;

    public WorldClientShipManager(World world) {
        this.world = world;
        this.loadedShips = new HashMap<>();
        this.loadQueue = new ConcurrentLinkedQueue<>();
        this.unloadQueue = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void tick() {
        loadAndUnloadShips();

        for (PhysicsObject physicsObject : getAllLoadedPhysObj()) {
            physicsObject.getShipTransformationManager()
                .updateAllTransforms(physicsObject.getShipData().getShipTransform(), false, false);
        }
    }

    private void loadAndUnloadShips() {
        while (!loadQueue.isEmpty()) {
            ShipData toLoad = loadQueue.remove();
            if (loadedShips.containsKey(toLoad)) {
                throw new IllegalStateException("Tried loading a ShipData that was already loaded?\n" + toLoad);
            }
            PhysicsObject physicsObject = new PhysicsObject(world, toLoad, false);
            loadedShips.put(toLoad, physicsObject);
        }

        while (!unloadQueue.isEmpty()) {
            ShipData toUnload = unloadQueue.remove();
            if (!loadedShips.containsKey(toUnload)) {
                throw new IllegalStateException("Tried unloading a ShipData that isn't loaded?\n" + toUnload);
            }
            loadedShips.remove(toUnload);
        }
    }

    @Override
    public void onWorldUnload() {
        loadedShips.clear();
    }

    @Nullable
    @Override
    public PhysicsObject getPhysObjectFromData(ShipData data) {
        return loadedShips.get(data);
    }

    @Nonnull
    @Override
    public List<PhysicsObject> getNearbyPhysObjects(AxisAlignedBB toCheck) {
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
    public Iterable<PhysicsObject> getAllLoadedPhysObj() {
        return loadedShips.values();
    }

    @Override
    public void queueShipLoad(@Nonnull ShipData data) {
        loadQueue.add(data);
    }

    @Override
    public void queueShipUnload(@Nonnull ShipData data) {
        unloadQueue.add(data);
    }

    @Nonnull
    @Override
    public World getWorld() {
        return world;
    }
}
