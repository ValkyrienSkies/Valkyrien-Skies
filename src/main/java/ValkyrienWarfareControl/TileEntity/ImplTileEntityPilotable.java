package ValkyrienWarfareControl.TileEntity;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareControl.ValkyrienWarfareControlMod;
import ValkyrienWarfareControl.Network.MessageStartPiloting;
import ValkyrienWarfareControl.Network.MessageStopPiloting;
import ValkyrienWarfareControl.NodeNetwork.BasicNodeTileEntity;
import ValkyrienWarfareControl.Piloting.ControllerInputType;
import ValkyrienWarfareControl.Piloting.ITileEntityPilotable;
import ValkyrienWarfareControl.Piloting.PilotControlsMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.world.WorldServer;

/**
 * A basic implementation of the ITileEntityPilotable interface, other tile entities can extend this for easy controls
 * @author thebest108
 *
 */
public abstract class ImplTileEntityPilotable extends BasicNodeTileEntity implements ITileEntityPilotable {

	private EntityPlayer pilotPlayerEntity;

	@Override
	public final void onPilotControlsMessage(PilotControlsMessage message, EntityPlayerMP sender) {
		if(sender == pilotPlayerEntity) {
			processControlMessage(message, sender);
		}else{
			//Wtf is this packet being sent for?
		}
	}

	@Override
	public final EntityPlayer getPilotEntity() {
		return pilotPlayerEntity;
	}

	@Override
	public final void setPilotEntity(EntityPlayer toSet) {
		if(!getWorld().isRemote) {
			sendPilotUpdatePackets((EntityPlayerMP) toSet, (EntityPlayerMP) pilotPlayerEntity);
		}
		pilotPlayerEntity = toSet;
		if(pilotPlayerEntity != null) {
			onStartTileUsage(pilotPlayerEntity);
		}else{
			onStopTileUsage();
		}
	}

	@Override
	public final void playerWantsToStopPiloting(EntityPlayer player) {
		if(player == getPilotEntity()) {
			setPilotEntity(null);
		}else{
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
		if(oldPilot != null) {
			ValkyrienWarfareControlMod.controlNetwork.sendTo(stopMessage, oldPilot);
		}
		if(newPilot != null) {
			ValkyrienWarfareControlMod.controlNetwork.sendTo(startMessage, newPilot);
		}
	}

	/**
	 * Unique for each TileEntity type
	 * @return
	 */
	abstract ControllerInputType getControlInputType();

	/**
	 * Unique for each TileEntity type
	 * @return
	 */
	abstract boolean setClientPilotingEntireShip();

	/**
	 * Unique for each TileEntity type, only called if the sender player is the same as the pilotPlayerEntity
	 * @return
	 */
	abstract void processControlMessage(PilotControlsMessage message, EntityPlayerMP sender);

	final void sendUpdatePacketToAllNearby(){
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
