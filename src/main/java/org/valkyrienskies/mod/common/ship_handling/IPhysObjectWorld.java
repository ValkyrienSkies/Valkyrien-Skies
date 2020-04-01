package org.valkyrienskies.mod.common.ship_handling;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

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

    @Nonnull
    Iterable<PhysicsObject> getAllLoadedPhysObj();

    /**
     * Thread safe way to queue a ship load.
     */
    void queueShipLoad(@Nonnull UUID shipID);

    /**
     * Thread safe way to queue a ship unload.
     */
    void queueShipUnload(@Nonnull UUID shipID);

}
