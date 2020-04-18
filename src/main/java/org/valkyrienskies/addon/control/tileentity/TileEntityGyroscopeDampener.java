package org.valkyrienskies.addon.control.tileentity;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.mod.common.physics.PhysicsCalculations;
import valkyrienwarfare.api.TransformType;

public class TileEntityGyroscopeDampener extends TileEntity {

    private static final Vector3dc GRAVITY_UP = new Vector3d(0, 1, 0);
    // 300,000 newton-meters maximum of torque.
    private double maximumTorque = 10000;

    public Vector3dc getTorqueInGlobal(PhysicsCalculations physicsCalculations, BlockPos pos) {
        Vector3d shipLevelNormal = new Vector3d(GRAVITY_UP);
        physicsCalculations.getParent().getShipTransformationManager().getCurrentPhysicsTransform()
            .transformDirection(shipLevelNormal, TransformType.SUBSPACE_TO_GLOBAL);

        double dampingComponent = shipLevelNormal.dot(new Vector3d(physicsCalculations.getAngularVelocity()));
        Vector3d angularChangeAllowed = shipLevelNormal
            .mul(shipLevelNormal.dot(new Vector3d(physicsCalculations.getAngularVelocity())), new Vector3d());
        Vector3d angularVelocityToDamp = new Vector3d(physicsCalculations.getAngularVelocity())
            .sub(angularChangeAllowed);

        Vector3d dampingTorque = angularVelocityToDamp
            .mul(physicsCalculations.getPhysicsTimeDeltaPerPhysTick());


        Vector3d dampingTorqueWithRespectToInertia = physicsCalculations.getPhysMOITensor().transform(dampingTorque);

        double dampingTorqueRespectMagnitude = dampingTorqueWithRespectToInertia.length();
        if (dampingTorqueRespectMagnitude > maximumTorque) {
            dampingTorqueWithRespectToInertia
                .mul(maximumTorque / dampingTorqueRespectMagnitude);
            // System.out.println("yee");
        }

        return dampingTorqueWithRespectToInertia.mul(-1);
    }
}
