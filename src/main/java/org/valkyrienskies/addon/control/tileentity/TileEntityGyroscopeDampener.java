package org.valkyrienskies.addon.control.tileentity;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.valkyrienskies.mod.common.math.RotationMatrices;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.PhysicsCalculations;
import valkyrienwarfare.api.TransformType;

public class TileEntityGyroscopeDampener extends TileEntity {

    private static final Vector GRAVITY_UP = new Vector(0, 1, 0);
    // 300,000 newton-meters maximum of torque.
    private double maximumTorque = 10000;

    public Vector getTorqueInGlobal(PhysicsCalculations physicsCalculations, BlockPos pos) {
        Vector shipLevelNormal = new Vector(GRAVITY_UP);
        physicsCalculations.getParent().getShipTransformationManager().getCurrentPhysicsTransform().rotate(shipLevelNormal, TransformType.SUBSPACE_TO_GLOBAL);

        double dampingComponent = shipLevelNormal.dot(physicsCalculations.angularVelocity);
        Vector angularChangeAllowed = shipLevelNormal.getProduct(shipLevelNormal.dot(physicsCalculations.angularVelocity));
        Vector angularVelocityToDamp = physicsCalculations.angularVelocity.getSubtraction(angularChangeAllowed);

        Vector dampingTorque = angularVelocityToDamp.getProduct(physicsCalculations.getPhysicsTimeDeltaPerPhysTick());

        Vector dampingTorqueWithRespectToInertia = RotationMatrices.get3by3TransformedVec(physicsCalculations.getPhysMOITensor(), dampingTorque);

        double dampingTorqueRespectMagnitude = dampingTorqueWithRespectToInertia.length();
        if (dampingTorqueRespectMagnitude > maximumTorque) {
            dampingTorqueWithRespectToInertia.multiply(maximumTorque / dampingTorqueRespectMagnitude);
            // System.out.println("yee");
        }

        return dampingTorqueWithRespectToInertia;
    }
}
