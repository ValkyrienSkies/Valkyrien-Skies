package org.valkyrienskies.mod.common.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.mod.common.block.BlockBoatChair;
import org.valkyrienskies.mod.common.physics.PhysicsCalculations;
import org.valkyrienskies.mod.common.piloting.ControllerInputType;
import org.valkyrienskies.mod.common.piloting.PilotControlsMessage;
import org.valkyrienskies.mod.common.ships.ship_transform.ShipTransform;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;
import valkyrienwarfare.api.TransformType;

import javax.annotation.Nullable;

public class TileEntityBoatChair extends TileEntityPilotableImpl {

    private static final double MAX_LINEAR_VELOCITY = 12;
    private static final double MAX_ANGULAR_VELOCITY = Math.PI / 2;
    private static final double LINEAR_EMA_FILTER_CONSTANT = 2;
    private static final double ANGULAR_EMA_FILTER_CONSTANT = 2;
    private static final double STABILIZATION_TORQUE_CONSTANT = 7.5;

    private Vector3dc targetLinearVelocity = new Vector3d();
    private Vector3dc targetAngularVelocity = new Vector3d();

    @Override
    public ControllerInputType getControlInputType() {
        return ControllerInputType.CaptainsChair;
    }

    @Override
    public void processControlMessage(PilotControlsMessage message, EntityPlayerMP sender) {

        final IBlockState state = getWorld().getBlockState(getPos());
        final double pilotYaw = ((BlockBoatChair) state.getBlock()).getChairYaw(state);

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
    }

    @Nullable
    public Vector3dc getBlockForceInShipSpace(World world, BlockPos pos, IBlockState state, PhysicsObject physicsObject, double secondsToApply) {
        // Don't add force if theres no pilot
        if (getPilotEntity() == null) {
            return null;
        }
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

    @Nullable
    public Vector3dc getTorqueInGlobal(PhysicsCalculations physicsCalculations) {
        // Don't add force if theres no pilot
        if (getPilotEntity() == null) {
            return null;
        }
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

    @Override
    public void onStopTileUsage() {
        targetLinearVelocity = new Vector3d();
        targetAngularVelocity = new Vector3d();
    }

}
