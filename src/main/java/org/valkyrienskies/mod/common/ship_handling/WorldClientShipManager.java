package org.valkyrienskies.mod.common.ship_handling;

import javax.annotation.Nonnull;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;
import org.valkyrienskies.mod.common.physmanagement.shipdata.QueryableShipData;

public class WorldClientShipManager implements IWorldShipManager {

    private final World world;
    private final QueryableShipData queryableShipData;


    public WorldClientShipManager(World world) {
        this.world = world;
        this.queryableShipData = QueryableShipData.get(world);
    }

    @Override
    public void tick() {
        for (PhysicsObject physicsObject : queryableShipData.getLoadedPhysos()) {
            physicsObject.getShipTransformationManager()
                .updateAllTransforms(physicsObject.getData().getShipTransform(), false, false);
        }
    }

    @Override
    public void onWorldUnload() {

    }

    @Nonnull
    @Override
    public World getWorld() {
        return world;
    }
}
