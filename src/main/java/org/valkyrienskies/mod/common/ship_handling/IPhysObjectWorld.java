package org.valkyrienskies.mod.common.ship_handling;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;
import org.valkyrienskies.mod.common.physmanagement.shipdata.ShipData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * This maps ShipData to their respective PhysicsObjects. Everything in here is designed to be transient. Anything
 * permanent should be stored in ShipData.
 */
public interface IPhysObjectWorld {

    /**
     * @return The world this is handling PhysicsObjects for.
     */
    @Nonnull
    World getWorld();

    void tick();

    void onWorldUnload();

    /**
     * Creates a new PhysicsObject for the given ShipData. Throws an exception if a PhysicsObject already exists for
     * that ShipData.
     *
     * @param data
     * @return
     */
    @Nonnull
    PhysicsObject createPhysObjectFromData(ShipData data) throws IllegalArgumentException;

    /**
     * Removes the PhysicsObject with the ShipData.
     *
     * @param data
     * @return True if there was a PhysicsObject with ShipData of data, false if there wasn't.
     */
    boolean removePhysObject(ShipData data);

    /**
     * @param data
     * @return Null if there wasn't a PhysicsObject for the given data.
     */
    @Nullable
    PhysicsObject getPhysObjectFromData(ShipData data);

    /**
     * @param toCheck
     * @return
     */
    @Nonnull
    List<PhysicsObject> getNearbyPhysObjects(AxisAlignedBB toCheck);

}
