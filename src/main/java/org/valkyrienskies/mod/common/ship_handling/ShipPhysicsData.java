package org.valkyrienskies.mod.common.ship_handling;

import lombok.Data;
import org.valkyrienskies.mod.common.math.Vector;

import javax.annotation.Nonnull;

/**
 * Stores data used by {@link org.valkyrienskies.mod.common.physics.PhysicsCalculations}
 */
@Data
public class ShipPhysicsData {
    @Nonnull
    Vector linearMomentum = new Vector();
    @Nonnull
    Vector angularVelocity = new Vector();
}
