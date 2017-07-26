package ValkyrienWarfareControl.TileEntity;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.Physics.PhysicsCalculationsManualControl;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareControl.Piloting.ControllerInputType;
import ValkyrienWarfareControl.Piloting.PilotControlsMessage;
import net.minecraft.entity.player.EntityPlayerMP;

public class TileEntityZepplinController extends ImplTileEntityPilotable {

	@Override
	public void onStopTileUsage() {
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(getWorld(), getPos());
		if(wrapper != null) {
			PhysicsCalculationsManualControl zepplinPhysics = (PhysicsCalculationsManualControl) wrapper.wrapping.physicsProcessor;
			zepplinPhysics.upRate = 0;
			zepplinPhysics.forwardRate = 0;
		}
	}

	@Override
	ControllerInputType getControlInputType() {
		return ControllerInputType.Zepplin;
	}

	@Override
	boolean setClientPilotingEntireShip() {
		return true;
	}

	@Override
	void processControlMessage(PilotControlsMessage message, EntityPlayerMP sender) {
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(getWorld(), getPos());
		if(wrapper != null) {
			PhysicsCalculationsManualControl zepplinPhysics = (PhysicsCalculationsManualControl) wrapper.wrapping.physicsProcessor;
			if(message.airshipLeft_KeyDown) {
				zepplinPhysics.yawRate -= 2.5;
			}
			if(message.airshipRight_KeyDown) {
				zepplinPhysics.yawRate += 2.5;
			}
			if(message.airshipUp_KeyDown) {
				zepplinPhysics.upRate += .25D;
			}
			if(message.airshipDown_KeyDown) {
				zepplinPhysics.upRate -= .25D;
			}
			if(message.airshipForward_KeyDown) {
				zepplinPhysics.forwardRate += .25D;
			}
			if(message.airshipBackward_KeyDown) {
				zepplinPhysics.forwardRate -= .25D;
			}
			if(message.airshipStop_KeyDown) {
				zepplinPhysics.yawRate = zepplinPhysics.upRate = zepplinPhysics.forwardRate = 0;
			}
			zepplinPhysics.yawRate = Math.min(Math.max(-50, zepplinPhysics.yawRate), 50);
			zepplinPhysics.upRate = Math.min(Math.max(-20, zepplinPhysics.upRate), 20);
			zepplinPhysics.forwardRate = Math.min(Math.max(-20, zepplinPhysics.forwardRate), 20);
		}
	}

}
