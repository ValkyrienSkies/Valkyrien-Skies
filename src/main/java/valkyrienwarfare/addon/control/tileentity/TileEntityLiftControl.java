package valkyrienwarfare.addon.control.tileentity;

import net.minecraft.entity.player.EntityPlayerMP;
import valkyrienwarfare.addon.control.nodenetwork.BasicNodeTileEntity;
import valkyrienwarfare.addon.control.piloting.ControllerInputType;
import valkyrienwarfare.addon.control.piloting.PilotControlsMessage;

public class TileEntityLiftControl extends ImplTileEntityPilotable {

	@Override
	ControllerInputType getControlInputType() {
		return ControllerInputType.LiftControl;
	}

	@Override
	void processControlMessage(PilotControlsMessage message, EntityPlayerMP sender) {
		if (message.airshipForward_KeyDown) {
			System.out.println("Lift Up");
		}
		if (message.airshipBackward_KeyDown) {
			System.out.println("Lift Down");
		}
	}

}
