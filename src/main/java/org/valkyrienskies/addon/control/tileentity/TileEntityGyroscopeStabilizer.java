package org.valkyrienskies.addon.control.tileentity;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.PhysicsCalculations;
import valkyrienwarfare.api.TransformType;

public class TileEntityGyroscopeStabilizer extends TileEntity {

    // Up to 15,000,000 newton-meters of torque generated.
    public static final double MAXIMUM_TORQUE = 15000000;
    // The direction we are want to align to.
    private static final Vector GRAVITY_UP = new Vector(0, 1, 0);

    public Vector getTorqueInGlobal(PhysicsCalculations physicsCalculations, BlockPos pos) {
        Vector shipLevelNormal = new Vector(GRAVITY_UP);
        physicsCalculations.getParent().getShipTransformationManager().getCurrentPhysicsTransform()
            .rotate(shipLevelNormal, TransformType.SUBSPACE_TO_GLOBAL);
        Vector torqueDir = GRAVITY_UP.cross(shipLevelNormal);
        double angleBetween = Math.toDegrees(GRAVITY_UP.angleBetween(shipLevelNormal));
        torqueDir.normalize();

        double torquePowerFactor = angleBetween / 5;

        torquePowerFactor = Math.max(Math.min(1, torquePowerFactor), 0);

        // System.out.println(angleBetween);

        torqueDir.multiply(MAXIMUM_TORQUE * torquePowerFactor * physicsCalculations
            .getPhysicsTimeDeltaPerPhysTick() * -1D);
        return torqueDir;
    }

}
