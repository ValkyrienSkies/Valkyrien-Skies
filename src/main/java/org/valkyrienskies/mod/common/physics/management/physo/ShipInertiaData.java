package org.valkyrienskies.mod.common.physics.management.physo;

import lombok.Getter;
import lombok.Setter;
import org.joml.Matrix3dc;
import org.valkyrienskies.mod.common.math.Vector;

import javax.annotation.Nullable;

/**
 * Stores the data of the ship mas and inertia matrix.
 */
@Setter
@Getter
public class ShipInertiaData {

    double gameTickMass = 0;
    @Nullable
    Matrix3dc gameMoITensor = null;
    Vector gameTickCenterOfMass = new Vector();
}
