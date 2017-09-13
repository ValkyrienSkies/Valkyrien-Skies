package valkyrienwarfare.addon.control.tileentity;

import valkyrienwarfare.addon.control.controlsystems.ShipTelegraphState;
import valkyrienwarfare.addon.control.piloting.ControllerInputType;
import valkyrienwarfare.addon.control.piloting.PilotControlsMessage;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.ITickable;

public class TileEntityShipTelegraph extends ImplTileEntityPilotable implements ITickable {

	public ShipTelegraphState telegraphState = ShipTelegraphState.LANGSAM_1;
	public double oldHandleRotation;
	public double handleRotation;

	double nextHandleRotation;

	@Override
	void processControlMessage(PilotControlsMessage message, EntityPlayerMP sender) {
		int ordinal = telegraphState.ordinal();
		if (message.airshipLeft_KeyPressed && ordinal > 0) {
			handleRotation -= 22.5D;
			ordinal--;
		}
		if (message.airshipRight_KeyPressed && ordinal < 12) {
			handleRotation += 22.5D;
			ordinal++;
		}
//		ordinal = math.max(0, math.min(12, ordinal));
		telegraphState = ShipTelegraphState.values()[ordinal];
	}


	@Override
	public void onDataPacket(net.minecraft.network.NetworkManager net, net.minecraft.network.play.server.SPacketUpdateTileEntity pkt) {
//		lastWheelRotation = wheelRotation;

		nextHandleRotation = pkt.getNbtCompound().getDouble("handleRotation");
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound tagToSend = new NBTTagCompound();
		tagToSend.setDouble("handleRotation", handleRotation);
		return new SPacketUpdateTileEntity(this.getPos(), 0, tagToSend);
	}

	public double getHandleRenderRotation() {
		return handleRotation;
	}

	@Override
	public void update() {
		if (getWorld().isRemote) {
			oldHandleRotation = handleRotation;
			handleRotation = nextHandleRotation;
		} else {
			sendUpdatePacketToAllNearby();
		}
//		this.markDirty();
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound toReturn = super.getUpdateTag();
		toReturn.setDouble("handleRotation", handleRotation);
		return toReturn;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		handleRotation = compound.getDouble("handleRotation");
		telegraphState = ShipTelegraphState.values()[compound.getInteger("telegraphStateOrdinal")];
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagCompound toReturn = super.writeToNBT(compound);
		toReturn.setDouble("handleRotation", handleRotation);
		toReturn.setInteger("telegraphStateOrdinal", telegraphState.ordinal());
		return toReturn;
	}

	@Override
	ControllerInputType getControlInputType() {
		return ControllerInputType.Telegraph;
	}

	@Override
	boolean setClientPilotingEntireShip() {
		return false;
	}

}
