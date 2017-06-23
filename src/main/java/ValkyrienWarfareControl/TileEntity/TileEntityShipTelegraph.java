package ValkyrienWarfareControl.TileEntity;

import ValkyrienWarfareControl.Piloting.ControllerInputType;
import ValkyrienWarfareControl.Piloting.PilotControlsMessage;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;

public class TileEntityShipTelegraph extends ImplTileEntityPilotable {

	private double handleRotation = 0D;

	@Override
	ControllerInputType getControlInputType() {
		return ControllerInputType.Telegraph;
	}

	@Override
	boolean setClientPilotingEntireShip() {
		return false;
	}

	@Override
	void processControlMessage(PilotControlsMessage message, EntityPlayerMP sender) {

	}

}
