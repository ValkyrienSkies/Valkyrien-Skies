package org.valkyrienskies.mod.common.physics.management;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.entity.PhysicsWrapperEntity;

public class DimensionPhysObjectManager {

    private final Map<World, WorldPhysObjectManager> managerPerWorld;

    public DimensionPhysObjectManager() {
        managerPerWorld = new HashMap<>();
    }

    /**
     * Kinda like a preorder, order one now!
     */
    void onShipPreload(PhysicsWrapperEntity toPreload) {
        getManagerForWorld(toPreload.world).preloadPhysicsWrapperEntityMappings(toPreload);
    }

    // Put the ship in the manager queues
    public void onShipLoad(PhysicsWrapperEntity justLoaded) {
        getManagerForWorld(justLoaded.world).onLoad(justLoaded);
    }

    // Remove the ship from the damn queues
    public void onShipUnload(PhysicsWrapperEntity justUnloaded) {
        getManagerForWorld(justUnloaded.world).onUnload(justUnloaded);
    }

    public WorldPhysObjectManager getManagerForWorld(World world) {
        if (!managerPerWorld.containsKey(world)) {
            managerPerWorld.put(world, new WorldPhysObjectManager(world));
        }
        return managerPerWorld.get(world);
    }

    public void removeWorld(World world) {
        managerPerWorld.remove(world);
    }

}
