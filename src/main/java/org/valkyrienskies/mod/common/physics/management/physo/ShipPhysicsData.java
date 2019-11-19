package org.valkyrienskies.mod.common.physics.management.physo;

import lombok.Getter;
import lombok.Setter;
import org.valkyrienskies.mod.common.math.Vector;

/**
 * Stores data used by {@link org.valkyrienskies.mod.common.physics.PhysicsCalculations}
 */
@Setter
@Getter

public class ShipPhysicsData {

    Vector linearMomentum = new Vector();
    Vector angularVelocity = new Vector();

}
