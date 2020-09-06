package org.valkyrienskies.mod.common.ships.ship_world;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.util.multithreaded.CalledFromWrongThreadException;

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
     * Only allowed to be called by the game thread.
     * @return Null if there doesn't exist a PhysicsObject for the given shipID.
     */
    @Nullable
    PhysicsObject getPhysObjectFromUUID(@Nonnull UUID shipID) throws CalledFromWrongThreadException;

    /**
     * @return A list of all the physics objects whose AABB intersect with toCheck.
     */
    @Nonnull
    List<PhysicsObject> getNearbyPhysObjects(@Nonnull AxisAlignedBB toCheck) throws CalledFromWrongThreadException;

    @Nonnull
    Iterable<PhysicsObject> getAllLoadedPhysObj() throws CalledFromWrongThreadException;

    /**
     * Can be called from any thread. Although the list is immutable the PhysicsObjects are not, so please do not modify
     * them on other threads; otherwise you risk breaking the ships.
     */
    @Nonnull
    ImmutableList<PhysicsObject> getAllLoadedThreadSafe();

    /**
     * Queues a ship load, must be called on the game thread.
     */
    void queueShipLoad(@Nonnull UUID shipID);

    /**
     * Queue a ship unload, must be called on the game thread.
     */
    void queueShipUnload(@Nonnull UUID shipID);

}
