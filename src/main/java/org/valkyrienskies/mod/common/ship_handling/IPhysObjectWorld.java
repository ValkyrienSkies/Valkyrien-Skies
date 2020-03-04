package org.valkyrienskies.mod.common.ship_handling;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;
import org.valkyrienskies.mod.common.physics.management.physo.ShipData;

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
     * Thread safe way to queue a ship spawn. (Not the same as {@link #queueShipLoad(ShipData)}.
     */
    void queueShipSpawn(@Nonnull ShipData data);

    /**
     * Thread safe way to queue a ship load.
     */
    void queueShipLoad(@Nonnull ShipData data);

    /**
     * Thread safe way to queue a ship unload.
     */
    void queueShipUnload(@Nonnull ShipData data);

}
