package org.valkyrienskies.mod.common.ships.ship_world;

import lombok.Getter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.mod.common.physics.PhysicsCalculations;
import org.valkyrienskies.mod.common.piloting.PilotControlsMessage;
import org.valkyrienskies.mod.common.ships.ship_transform.ShipTransform;
import valkyrienwarfare.api.TransformType;

import java.util.UUID;

/**
 * Basically a copy of {@link org.valkyrienskies.mod.common.tileentity.TileEntityBoatChair}, except this works without a pilot block.
 */
public class ShipPilot {
    private static final double MAX_LINEAR_VELOCITY = 12;
    private static final double MAX_ANGULAR_VELOCITY = Math.PI / 2;
    private static final double LINEAR_EMA_FILTER_CONSTANT = 2;
    private static final double ANGULAR_EMA_FILTER_CONSTANT = 2;
    private static final double STABILIZATION_TORQUE_CONSTANT = 7.5;

    @Getter
    private final UUID pilot;
    private final float initialYaw;
    private Vector3d targetLinearVelocity;
    private Vector3d targetAngularVelocity;

    public ShipPilot(final EntityPlayer pilot) {
        this.pilot = pilot.entityUniqueID;
        this.initialYaw = pilot.rotationYaw;
        this.targetLinearVelocity = new Vector3d();
        this.targetAngularVelocity = new Vector3d();
    }

    public void processControlMessage(PilotControlsMessage message, EntityPlayerMP sender) {
        final double pilotYaw = -initialYaw - 90;

        // Linear velocity
        final Vector3d newTargetLinearVelocity = new Vector3d();
        if (message.airshipForward_KeyDown) {
            newTargetLinearVelocity.x += MAX_LINEAR_VELOCITY;
        }
        if (message.airshipBackward_KeyDown) {
            newTargetLinearVelocity.x -= MAX_LINEAR_VELOCITY;
        }
        if (message.airshipSprinting) {
            newTargetLinearVelocity.mul(2);
        }
        newTargetLinearVelocity.rotateAxis(Math.toRadians(pilotYaw), 0, 1, 0);

        // Angular velocity
        final Vector3d newTargetAngularVelocity = new Vector3d();
        if (message.airshipLeft_KeyDown) {
            newTargetAngularVelocity.y += MAX_ANGULAR_VELOCITY;
        }
        if (message.airshipRight_KeyDown) {
            newTargetAngularVelocity.y -= MAX_ANGULAR_VELOCITY;
        }

        // Update the target velocities
        targetLinearVelocity = newTargetLinearVelocity;
        targetAngularVelocity = newTargetAngularVelocity;

        // Check if we need to stop piloting

    }

    public Vector3dc getBlockForceInShipSpace(PhysicsObject physicsObject, double secondsToApply) {
        final ShipTransform shipTransform = physicsObject.getShipTransformationManager().getCurrentPhysicsTransform();

        final Vector3dc idealLinearVelocity = shipTransform.transformDirectionNew(new Vector3d(targetLinearVelocity), TransformType.SUBSPACE_TO_GLOBAL);
        final Vector3dc currentLinearVelocity = physicsObject.getPhysicsCalculations().getLinearVelocity();
        final Vector3dc velocityDifference = idealLinearVelocity.sub(currentLinearVelocity, new Vector3d());

        final Vector3d resultingBlockForce = new Vector3d(velocityDifference);
        resultingBlockForce.mul(physicsObject.getInertiaData().getGameTickMass());
        resultingBlockForce.mul(secondsToApply);

        resultingBlockForce.mul(LINEAR_EMA_FILTER_CONSTANT);

        // Do not affect y axis
        resultingBlockForce.y = 0;

        return resultingBlockForce;
    }

    public Vector3dc getTorqueInGlobal(PhysicsCalculations physicsCalculations) {
        final PhysicsObject physicsObject = physicsCalculations.getParent();
        final ShipTransform shipTransform = physicsObject.getShipTransformationManager().getCurrentPhysicsTransform();

        final Vector3dc idealAngularVelocity = shipTransform.transformDirectionNew(new Vector3d(targetAngularVelocity), TransformType.SUBSPACE_TO_GLOBAL);
        final Vector3dc currentAngularVelocity = physicsCalculations.getAngularVelocity();
        final Vector3dc velocityDifference = idealAngularVelocity.sub(currentAngularVelocity, new Vector3d());

        final Vector3d resultingTorque = physicsCalculations.getPhysMOITensor().transform(velocityDifference, new Vector3d());
        resultingTorque.mul(physicsCalculations.getPhysicsTimeDeltaPerPhysTick());

        resultingTorque.mul(ANGULAR_EMA_FILTER_CONSTANT);

        // Only effect y axis
        resultingTorque.x = 0;
        resultingTorque.z = 0;

        // Add a stabilization torque
        final Vector3dc shipUp = shipTransform.transformDirectionNew(new Vector3d(0, 1, 0), TransformType.SUBSPACE_TO_GLOBAL);
        final Vector3dc idealUp = new Vector3d(0, 1, 0);
        final double angleBetween = shipUp.angle(idealUp);
        if (angleBetween > .01) {
            final Vector3dc stabilizationRotationAxisNormalized = shipUp.cross(idealUp, new Vector3d()).normalize();

            final Vector3d stabilizationTorque = physicsCalculations.getPhysMOITensor().transform(stabilizationRotationAxisNormalized.mul(angleBetween, new Vector3d()));
            stabilizationTorque.mul(physicsCalculations.getPhysicsTimeDeltaPerPhysTick());
            stabilizationTorque.mul(STABILIZATION_TORQUE_CONSTANT);

            resultingTorque.add(stabilizationTorque);
        }

        return resultingTorque;
    }
}
