package valkyrienwarfare.addon.control.tileentity;

import valkyrienwarfare.addon.control.network.MessageStartPiloting;
import valkyrienwarfare.addon.control.network.MessageStopPiloting;
import valkyrienwarfare.addon.control.nodenetwork.BasicNodeTileEntity;
import valkyrienwarfare.addon.control.piloting.ControllerInputType;
import valkyrienwarfare.addon.control.piloting.ITileEntityPilotable;
import valkyrienwarfare.addon.control.piloting.PilotControlsMessage;
import valkyrienwarfare.addon.control.ValkyrienWarfareControl;
import valkyrienwarfare.api.RotationMatrices;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.physicsmanagement.PhysicsWrapperEntity;
import valkyrienwarfare.ValkyrienWarfareMod;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.world.WorldServer;

/**
 * A basic implementation of the ITileEntityPilotable interface, other tile entities can extend this for easy controls
 *
 * @author thebest108
 */
public abstract class ImplTileEntityPilotable extends BasicNodeTileEntity implements ITileEntityPilotable {
	
	private EntityPlayer pilotPlayerEntity;
	
	@Override
	public final void onPilotControlsMessage(PilotControlsMessage message, EntityPlayerMP sender) {
		if (sender == pilotPlayerEntity) {
			processControlMessage(message, sender);
		} else {
			//Wtf is this packet being sent for?
		}
	}
	
	@Override
	public final EntityPlayer getPilotEntity() {
		return pilotPlayerEntity;
	}
	
	@Override
	public final void setPilotEntity(EntityPlayer toSet) {
		if (!getWorld().isRemote) {
			sendPilotUpdatePackets((EntityPlayerMP) toSet, (EntityPlayerMP) pilotPlayerEntity);
		}
		pilotPlayerEntity = toSet;
		if (pilotPlayerEntity != null) {
			onStartTileUsage(pilotPlayerEntity);
		} else {
			onStopTileUsage();
		}
	}
	
	@Override
	public final void playerWantsToStopPiloting(EntityPlayer player) {
		if (player == getPilotEntity()) {
			setPilotEntity(null);
		} else {
			//Wtf happened here?
		}
	}
	
	@Override
	public final PhysicsWrapperEntity getParentPhysicsEntity() {
		return ValkyrienWarfareMod.physicsManager.getObjectManagingPos(getWorld(), getPos());
	}
	
	//Always call this before setting the pilotPlayerEntity to equal newPilot
	private final void sendPilotUpdatePackets(EntityPlayerMP newPilot, EntityPlayerMP oldPilot) {
		MessageStopPiloting stopMessage = new MessageStopPiloting(getPos());
		MessageStartPiloting startMessage = new MessageStartPiloting(getPos(), setClientPilotingEntireShip(), getControlInputType());
		if (oldPilot != null) {
			ValkyrienWarfareControl.controlNetwork.sendTo(stopMessage, oldPilot);
		}
		if (newPilot != null) {
			ValkyrienWarfareControl.controlNetwork.sendTo(startMessage, newPilot);
		}
	}
	
	/**
	 * Unique for each tileentity type
	 *
	 * @return
	 */
	abstract ControllerInputType getControlInputType();
	
	/**
	 * Unique for each tileentity type
	 *
	 * @return
	 */
	abstract boolean setClientPilotingEntireShip();
	
	/**
	 * Unique for each tileentity type, only called if the sender player is the same as the pilotPlayerEntity
	 *
	 * @return
	 */
	abstract void processControlMessage(PilotControlsMessage message, EntityPlayerMP sender);
	
	final void sendUpdatePacketToAllNearby() {
		SPacketUpdateTileEntity spacketupdatetileentity = getUpdatePacket();
		WorldServer serverWorld = (WorldServer) world;
		Vector pos = new Vector(getPos().getX(), getPos().getY(), getPos().getZ());
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(getWorld(), getPos());
		if (wrapper != null) {
			RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWTransform, pos);
		}
		serverWorld.mcServer.getPlayerList().sendToAllNearExcept(null, pos.X, pos.Y, pos.Z, 128D, getWorld().provider.getDimension(), spacketupdatetileentity);
	}
	
}
