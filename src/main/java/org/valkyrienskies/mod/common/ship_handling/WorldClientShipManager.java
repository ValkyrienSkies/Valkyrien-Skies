package org.valkyrienskies.mod.common.ship_handling;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;
import org.valkyrienskies.mod.common.physics.management.physo.ShipData;

public class WorldClientShipManager implements IPhysObjectWorld {

    private final World world;

    public WorldClientShipManager(World world) {
        this.world = world;
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
    public List<PhysicsObject> getNearbyPhysObjects(AxisAlignedBB toCheck) {
        return new ArrayList<>();
        // throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public World getWorld() {
        return world;
    }
}
