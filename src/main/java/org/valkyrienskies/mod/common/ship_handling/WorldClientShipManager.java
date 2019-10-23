package org.valkyrienskies.mod.common.ship_handling;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;
import org.valkyrienskies.mod.common.physics.management.physo.ShipData;
import org.valkyrienskies.mod.common.physmanagement.shipdata.QueryableShipData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class WorldClientShipManager implements IPhysObjectWorld {

    private final World world;
    private final Map<UUID, ShipData> ships;

    public WorldClientShipManager(World world) {
        this.world = world;
        this.ships = new HashMap<>();
    }

    @Override
    public void tick() {
        // Do ship stuff
        
    }

    @Override
    public void onWorldUnload() {
        // Don't do anything
    }

    @Nonnull
    @Override
    public PhysicsObject createPhysObjectFromData(ShipData data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removePhysObject(ShipData data) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public PhysicsObject getPhysObjectFromData(ShipData data) {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public List<PhysicsObject> getNearbyPhysObjects(AxisAlignedBB toCheck) {
        List<PhysicsObject> nearby = new ArrayList<>();
        for (ShipData data : QueryableShipData.get(world)) {
            if (data.getPhyso() != null) {
                if (toCheck.intersects(data.getShipBB())) {
                    nearby.add(data.getPhyso());
                }
            }
        }
        return nearby;
    }

    @Nonnull
    @Override
    public World getWorld() {
        return world;
    }
}
