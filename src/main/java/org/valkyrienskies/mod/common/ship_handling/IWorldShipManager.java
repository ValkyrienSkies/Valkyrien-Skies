package org.valkyrienskies.mod.common.ship_handling;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.physics.IPhysicsEngine;

/**
 * This maps ShipData to their respective PhysicsObjects. Everything in here is designed to be transient. Anything
 * permanent should be stored in ShipData.
 */
public interface IWorldShipManager {

    /**
     * @return The world this is handling PhysicsObjects for.
     */
    @Nonnull
    World getWorld();

    @Nullable
    default IPhysicsEngine getPhysicsEngine() {
        return null;
    }

    void tick();

    void onWorldUnload();



}
