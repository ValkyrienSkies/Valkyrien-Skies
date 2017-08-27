package valkyrienwarfare.addon.control.tileentity;

import valkyrienwarfare.api.RotationMatrices;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.physicsmanagement.PhysicsObject;
import valkyrienwarfare.physicsmanagement.PhysicsWrapperEntity;
import valkyrienwarfare.addon.control.block.BlockShipPilotsChair;
import valkyrienwarfare.addon.control.piloting.ControllerInputType;
import valkyrienwarfare.addon.control.piloting.PilotControlsMessage;
import valkyrienwarfare.addon.control.ValkyrienWarfareControl;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;

public class TileEntityPilotsChair extends ImplTileEntityPilotable {

	@Override
	void processControlMessage(PilotControlsMessage message, EntityPlayerMP sender) {
		IBlockState blockState = getWorld().getBlockState(getPos());
		if (blockState.getBlock() == ValkyrienWarfareControl.INSTANCE.pilotsChair) {
			PhysicsWrapperEntity wrapper = getParentPhysicsEntity();
			if (wrapper != null) {
				processCalculationsForControlMessageAndApplyCalculations(wrapper, message, blockState);
			}
		} else {
			setPilotEntity(null);
		}
	}

	@Override
	final ControllerInputType getControlInputType() {
		return ControllerInputType.PilotsChair;
	}

	@Override
	final boolean setClientPilotingEntireShip() {
		return true;
	}


	@Override
	public final void onStartTileUsage(EntityPlayer player) {
		getParentPhysicsEntity().wrapping.physicsProcessor.actAsArchimedes = true;
	}

	@Override
	public final void onStopTileUsage() {
		getParentPhysicsEntity().wrapping.physicsProcessor.actAsArchimedes = false;
	}

	private final void processCalculationsForControlMessageAndApplyCalculations(PhysicsWrapperEntity wrapper, PilotControlsMessage message, IBlockState state) {
		BlockPos chairPosition = getPos();
		PhysicsObject controlledShip = wrapper.wrapping;

		double pilotPitch = 0D;
		double pilotYaw = ((BlockShipPilotsChair) state.getBlock()).getChairYaw(state, chairPosition);
		double pilotRoll = 0D;

		double[] pilotRotationMatrix = RotationMatrices.getRotationMatrix(pilotPitch, pilotYaw, pilotRoll);

		Vector playerDirection = new Vector(1, 0, 0);

		Vector rightDirection = new Vector(0, 0, 1);

		Vector leftDirection = new Vector(0, 0, -1);

		RotationMatrices.applyTransform(pilotRotationMatrix, playerDirection);
		RotationMatrices.applyTransform(pilotRotationMatrix, rightDirection);
		RotationMatrices.applyTransform(pilotRotationMatrix, leftDirection);

		Vector upDirection = new Vector(0, 1, 0);

		Vector downDirection = new Vector(0, -1, 0);

		Vector idealAngularDirection = new Vector();

		Vector idealLinearVelocity = new Vector();

		Vector shipUp = new Vector(0, 1, 0);
		Vector shipUpPos = new Vector(0, 1, 0);

		if (message.airshipForward_KeyDown) {
			idealLinearVelocity.add(playerDirection);
		}
		if (message.airshipBackward_KeyDown) {
			idealLinearVelocity.subtract(playerDirection);
		}

		RotationMatrices.applyTransform(controlledShip.coordTransform.lToWRotation, idealLinearVelocity);

		RotationMatrices.applyTransform(controlledShip.coordTransform.lToWRotation, shipUp);

		if (message.airshipUp_KeyDown) {
			idealLinearVelocity.add(upDirection);
		}
		if (message.airshipDown_KeyDown) {
			idealLinearVelocity.add(downDirection);
		}


		if (message.airshipRight_KeyDown) {
			idealAngularDirection.add(rightDirection);
		}
		if (message.airshipLeft_KeyDown) {
			idealAngularDirection.add(leftDirection);
		}

		//Upside down if you want it
//		Vector shipUpOffset = shipUpPos.getSubtraction(shipUp);
		Vector shipUpOffset = shipUp.getSubtraction(shipUpPos);


		double mass = controlledShip.physicsProcessor.mass;

//		idealAngularDirection.multiply(mass/2.5D);
		idealLinearVelocity.multiply(mass / 5D);
//		shipUpOffset.multiply(mass/2.5D);


		idealAngularDirection.multiply(1D / 6D);
		shipUpOffset.multiply(1D / 3D);

		Vector velocityCompenstationLinear = controlledShip.physicsProcessor.linearMomentum;

		Vector velocityCompensationAngular = controlledShip.physicsProcessor.angularVelocity.cross(playerDirection);

		Vector velocityCompensationAlignment = controlledShip.physicsProcessor.angularVelocity.cross(shipUpPos);

		velocityCompensationAlignment.multiply(controlledShip.physicsProcessor.physRawSpeed);
		velocityCompensationAngular.multiply(2D * controlledShip.physicsProcessor.physRawSpeed);

		shipUpOffset.subtract(velocityCompensationAlignment);
		velocityCompensationAngular.subtract(velocityCompensationAngular);

		RotationMatrices.applyTransform3by3(controlledShip.physicsProcessor.framedMOI, idealAngularDirection);
		RotationMatrices.applyTransform3by3(controlledShip.physicsProcessor.framedMOI, shipUpOffset);


		if (message.airshipSprinting) {
			idealLinearVelocity.multiply(2D);
		}

		idealLinearVelocity.subtract(idealAngularDirection);
		idealLinearVelocity.subtract(shipUpOffset);

		//TEMPORARY CODE!!!

		controlledShip.physicsProcessor.addForceAtPoint(playerDirection, idealAngularDirection);

		controlledShip.physicsProcessor.addForceAtPoint(shipUpPos, shipUpOffset);

		controlledShip.physicsProcessor.addForceAtPoint(new Vector(), idealLinearVelocity);

		controlledShip.physicsProcessor.convertTorqueToVelocity();
	}

}
