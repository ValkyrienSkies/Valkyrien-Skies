package org.valkyrienskies.addon.control.tileentity;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.mod.common.physics.PhysicsCalculations;
import valkyrienwarfare.api.TransformType;

public class TileEntityGyroscopeStabilizer extends TileEntity {

    // Up to 15,000,000 newton-meters of torque generated.
    public static final double MAXIMUM_TORQUE = 15000000;
    // The direction we are want to align to.
    private static final Vector3dc GRAVITY_UP = new Vector3d(0, 1, 0);

    public Vector3dc getTorqueInGlobal(PhysicsCalculations physicsCalculations, BlockPos pos) {
        Vector3d shipLevelNormal = new Vector3d(GRAVITY_UP);
        physicsCalculations.getParent().getShipTransformationManager().getCurrentPhysicsTransform()
            .transformDirection(shipLevelNormal, TransformType.SUBSPACE_TO_GLOBAL);
        Vector3d torqueDir = GRAVITY_UP.cross(shipLevelNormal, new Vector3d());

        if (torqueDir.lengthSquared() < .01) {
            // The ship is already level, don't try to divide by 0
            return new Vector3d();
        }

        double angleBetween = Math.toDegrees(GRAVITY_UP.angle(shipLevelNormal));
        torqueDir.normalize();

        double torquePowerFactor = angleBetween / 5;

        torquePowerFactor = Math.max(Math.min(1, torquePowerFactor), 0);

        // System.out.println(angleBetween);

        return torqueDir.mul(MAXIMUM_TORQUE * torquePowerFactor * physicsCalculations
            .getPhysicsTimeDeltaPerPhysTick() * -1D);
    }

}
