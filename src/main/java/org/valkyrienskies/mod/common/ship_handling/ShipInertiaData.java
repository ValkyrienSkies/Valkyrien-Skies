package org.valkyrienskies.mod.common.ship_handling;

import lombok.Data;
import org.joml.Matrix3d;
import org.joml.Matrix3dc;
import org.valkyrienskies.mod.common.math.Vector;

import javax.annotation.Nonnull;

/**
 * Stores the data of the ship mas and inertia matrix.
 */
@Data
public class ShipInertiaData {

    double gameTickMass = 0;
    @Nonnull
    Matrix3dc gameMoITensor = new Matrix3d();
    @Nonnull
    Vector gameTickCenterOfMass = new Vector();
}
