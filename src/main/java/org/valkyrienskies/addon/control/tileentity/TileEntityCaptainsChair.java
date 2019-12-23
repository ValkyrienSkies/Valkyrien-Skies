package org.valkyrienskies.addon.control.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import org.valkyrienskies.addon.control.ValkyrienSkiesControl;
import org.valkyrienskies.addon.control.block.BlockCaptainsChair;
import org.valkyrienskies.addon.control.piloting.ControllerInputType;
import org.valkyrienskies.addon.control.piloting.PilotControlsMessage;
import org.valkyrienskies.mod.common.math.RotationMatrices;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;
import valkyrienwarfare.api.TransformType;

public class TileEntityCaptainsChair extends TileEntityPilotableImpl {

    @Override
    void processControlMessage(PilotControlsMessage message, EntityPlayerMP sender) {
        IBlockState blockState = getWorld().getBlockState(getPos());
        if (blockState.getBlock() == ValkyrienSkiesControl.INSTANCE.vsControlBlocks.captainsChair) {
            PhysicsObject physicsObject = getParentPhysicsEntity();
            if (physicsObject != null) {
                processCalculationsForControlMessageAndApplyCalculations(physicsObject, message,
                    blockState);
            }
        } else {
            setPilotEntity(null);
        }
    }

    @Override
    final ControllerInputType getControlInputType() {
        return ControllerInputType.CaptainsChair;
    }

    @Override
    boolean setClientPilotingEntireShip() {
        return true;
    }

    @Override
    public final void onStartTileUsage(EntityPlayer player) {
        getParentPhysicsEntity().getPhysicsCalculations().actAsArchimedes = true;
    }

    @Override
    public final void onStopTileUsage() {
        // Sanity check, sometimes we can be piloting something that's been destroyed so there's nothing to change physics on.
        if (getParentPhysicsEntity() != null) {
            getParentPhysicsEntity().getPhysicsCalculations().actAsArchimedes = false;
        }
    }

    private void processCalculationsForControlMessageAndApplyCalculations(
            PhysicsObject controlledShip, PilotControlsMessage message, IBlockState state) {
        BlockPos chairPosition = getPos();

        if (controlledShip.isShipAligningToGrid()) {
            return;
        }

        double pilotPitch = 0D;
        double pilotYaw = ((BlockCaptainsChair) state.getBlock()).getChairYaw(state, chairPosition);
        double pilotRoll = 0D;

        double[] pilotRotationMatrix = RotationMatrices
            .getRotationMatrix(pilotPitch, pilotYaw, pilotRoll);

        Vector playerDirection = new Vector(1, 0, 0);

        RotationMatrices.applyTransform(pilotRotationMatrix, playerDirection);

        Vector upDirection = new Vector(0, 1, 0);

        Vector downDirection = new Vector(0, -1, 0);

        Vector idealAngularDirection = new Vector();

        Vector idealLinearVelocity = new Vector();

        Vector shipUp = new Vector(0, 1, 0);
        Vector shipUpPosIdeal = new Vector(0, 1, 0);

        if (message.airshipForward_KeyDown) {
            idealLinearVelocity.add(playerDirection);
        }
        if (message.airshipBackward_KeyDown) {
            idealLinearVelocity.subtract(playerDirection);
        }

        controlledShip.getShipTransformationManager().getCurrentTickTransform()
            .rotate(idealLinearVelocity, TransformType.SUBSPACE_TO_GLOBAL);
        controlledShip.getShipTransformationManager().getCurrentTickTransform()
            .rotate(shipUp, TransformType.SUBSPACE_TO_GLOBAL);

        if (message.airshipUp_KeyDown) {
            idealLinearVelocity.add(upDirection.getProduct(.5));
        }
        if (message.airshipDown_KeyDown) {
            idealLinearVelocity.add(downDirection.getProduct(.5));
        }

        double sidePitch = 0;

        if (message.airshipRight_KeyDown) {
            idealAngularDirection.subtract(shipUp);
            sidePitch -= 10D;
        }
        if (message.airshipLeft_KeyDown) {
            idealAngularDirection.add(shipUp);
            sidePitch += 10D;
        }

        Vector sidesRotationAxis = new Vector(playerDirection);
        controlledShip.getShipTransformationManager().getCurrentTickTransform()
            .rotate(sidesRotationAxis, TransformType.SUBSPACE_TO_GLOBAL);

        double[] rotationSidesTransform = RotationMatrices
            .getRotationMatrix(sidesRotationAxis.x, sidesRotationAxis.y,
                sidesRotationAxis.z, Math.toRadians(sidePitch));
        shipUpPosIdeal.transform(rotationSidesTransform);

        idealAngularDirection.multiply(2);
        // The vector that points in the direction of the normal of the plane that
        // contains shipUp and shipUpPos. This is our axis of rotation.
        Vector shipUpRotationVector = shipUp.cross(shipUpPosIdeal);
        // This isnt quite right, but it handles the cases quite well.
        double shipUpTheta = shipUp.angleBetween(shipUpPosIdeal) + Math.PI;
        shipUpRotationVector.multiply(shipUpTheta);

        idealAngularDirection.add(shipUpRotationVector);
        idealLinearVelocity.multiply(20D * controlledShip.getPhysicsCalculations().getMass());

        // Move the ship faster if the player holds the sprint key.
        if (message.airshipSprinting) {
            idealLinearVelocity.multiply(2D);
        }

        double lerpFactor = .2D;
        Vector linearMomentumDif = idealLinearVelocity
            .getSubtraction(controlledShip.getPhysicsCalculations().getLinearMomentum());
        Vector angularVelocityDif = idealAngularDirection
            .getSubtraction(controlledShip.getPhysicsCalculations().getAngularVelocity());

        linearMomentumDif.multiply(lerpFactor);
        angularVelocityDif.multiply(lerpFactor);

        controlledShip.getPhysicsCalculations().getLinearMomentum().subtract(linearMomentumDif);
        controlledShip.getPhysicsCalculations().getAngularVelocity().subtract(angularVelocityDif);
    }

}
