package org.valkyrienskies.mod.common.ship_handling;

import lombok.Data;
import org.joml.Matrix3d;
import org.joml.Matrix3dc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

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
    Vector3dc gameTickCenterOfMass = new Vector3d();
}
