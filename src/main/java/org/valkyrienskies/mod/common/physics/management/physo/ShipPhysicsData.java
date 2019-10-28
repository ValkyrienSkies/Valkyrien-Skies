package org.valkyrienskies.mod.common.physics.management.physo;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.joml.Matrix3d;
import org.joml.Matrix3dc;
import org.valkyrienskies.mod.common.math.Vector;

/**
 * Stores data used by {@link org.valkyrienskies.mod.common.physics.PhysicsCalculations}
 */
@Setter
@Getter
@Accessors(fluent = false)
public class ShipPhysicsData {

    Vector linearMomentum = new Vector();
    Vector angularVelocity = new Vector();
    double gameTickMass = 0;
    Matrix3dc gameMoITensor = new Matrix3d();
    Vector gameTickCenterOfMass = new Vector();

}
