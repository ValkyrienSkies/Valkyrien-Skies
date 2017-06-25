package ValkyrienWarfareControl.TileEntity;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.Physics.PhysicsCalculations_Zepplin;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareControl.Piloting.ControllerInputType;
import ValkyrienWarfareControl.Piloting.PilotControlsMessage;
import net.minecraft.entity.player.EntityPlayerMP;

public class TileEntityZepplinController extends ImplTileEntityPilotable {

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
			PhysicsCalculations_Zepplin zepplinPhysics = (PhysicsCalculations_Zepplin) wrapper.wrapping.physicsProcessor;
			if(message.airshipLeft) {
				zepplinPhysics.yawRate -= 2.5;
			}
			if(message.airshipRight) {
				zepplinPhysics.yawRate += 2.5;
			}
			if(message.airshipUp) {
				zepplinPhysics.upRate += .25D;
			}
			if(message.airshipDown) {
				zepplinPhysics.upRate -= .25D;
			}
			if(message.airshipForward) {
				zepplinPhysics.forwardRate += .25D;
			}
			if(message.airshipBackward) {
				zepplinPhysics.forwardRate -= .25D;
			}
			if(message.airshipStop) {
				zepplinPhysics.yawRate = zepplinPhysics.upRate = zepplinPhysics.forwardRate = 0;
			}
			zepplinPhysics.yawRate = Math.min(Math.max(-50, zepplinPhysics.yawRate), 50);
			zepplinPhysics.upRate = Math.min(Math.max(-20, zepplinPhysics.upRate), 20);
			zepplinPhysics.forwardRate = Math.min(Math.max(-20, zepplinPhysics.forwardRate), 20);
		}
	}

}
