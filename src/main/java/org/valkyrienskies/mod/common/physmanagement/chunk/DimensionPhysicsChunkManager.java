package org.valkyrienskies.mod.common.physmanagement.chunk;

import net.minecraft.world.World;
import org.valkyrienskies.mod.common.entity.PhysicsWrapperEntity;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;

import java.util.HashMap;
import java.util.Map;

public class DimensionPhysicsChunkManager {

    private final Map<World, PhysicsChunkManager> managerPerWorld;

    public DimensionPhysicsChunkManager() {
        managerPerWorld = new HashMap<>();
    }

    public void initWorld(World world) {
        if (!managerPerWorld.containsKey(world)) {
            System.out.println("Physics Chunk Manager Initialized");
            managerPerWorld.put(world, new PhysicsChunkManager(world));
        }
    }

    public PhysicsChunkManager getManagerForWorld(World world) {
        return managerPerWorld.get(world);
    }

    public void removeWorld(World world) {
        managerPerWorld.remove(world);
    }

    public void registerChunksForShip(PhysicsWrapperEntity wrapper) {
        ValkyrienUtils.getQueryableData(wrapper.world).addShip(wrapper);
    }

    public void removeRegisteredChunksForShip(PhysicsWrapperEntity wrapper) {
        ValkyrienUtils.getQueryableData(wrapper.world).removeShip(wrapper);
    }

    public void updateShipPosition(PhysicsWrapperEntity wrapper) {
        ValkyrienUtils.getQueryableData(wrapper.world).updateShipPosition(wrapper);
    }

    public void removeShipPosition(PhysicsWrapperEntity wrapper) {
        ValkyrienUtils.getQueryableData(wrapper.world).removeShip(wrapper);
    }

    public void removeShipNameRegistry(PhysicsWrapperEntity wrapper) {
        ValkyrienUtils.getQueryableData(wrapper.world).removeShip(wrapper);
    }
}
