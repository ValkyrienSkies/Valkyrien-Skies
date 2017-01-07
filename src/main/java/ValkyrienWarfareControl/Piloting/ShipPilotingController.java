package ValkyrienWarfareControl.Piloting;

import java.util.UUID;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsObject;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.PhysicsManagement.WorldPhysObjectManager;
import ValkyrienWarfareControl.ValkyrienWarfareControlMod;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/**
 * Used only on the Server ship entity
 * @author thebest108
 *
 */
public class ShipPilotingController {

	public final PhysicsObject controlledShip;
	private EntityPlayerMP shipPilot;
	
	//Used for world saving/loading purposes
	private UUID mostRecentPilotID;
	public static final UUID nullID = new UUID(0L,0L);
	
	public ShipPilotingController(PhysicsObject toControl){
		controlledShip = toControl;
	}
	
	public EntityPlayerMP getPilotEntity(){
		return shipPilot;
	}
	
	public void receivePilotControlsMessage(PilotControlsMessage message, EntityPlayerMP whoSentIt){
		if(shipPilot == whoSentIt){
			handlePilotControlMessage(message, whoSentIt);
		}
	}
	
	private void handlePilotControlMessage(PilotControlsMessage message, EntityPlayerMP whoSentIt){
		
	}
	
	/**
	 * Sets the inputed player as the pilot of this ship
	 * @param toSet
	 * @param ignorePilotConflicts Should be set to false for almost every single case
	 */
	public void setPilotEntity(EntityPlayerMP toSet, boolean ignorePilotConflicts){
		if(shipPilot != null){
			sendPlayerPilotingPacket(shipPilot, null);
		}
		
		if(toSet != null){
			//Send packets here or something

			mostRecentPilotID = toSet.getPersistentID();
			PhysicsWrapperEntity otherShipPiloted = getShipPlayerIsPiloting(toSet);
			if(otherShipPiloted != null){
				//Removes this player from piloting the other ship
				otherShipPiloted.wrapping.pilotingController.setPilotEntity(null, true);
			}
			sendPlayerPilotingPacket(toSet, controlledShip.wrapper);
		}else{
			mostRecentPilotID = null;
		}
		shipPilot = toSet;
	}
	
	public boolean isShipBeingPiloted(){
		return getPilotEntity() != null;
	}
	
	public void writeToNBTTag(NBTTagCompound compound) {
		if(mostRecentPilotID != null){
			compound.setUniqueId("mostRecentPilotID", mostRecentPilotID);
		}
	}
	
	public void readFromNBTTag(NBTTagCompound compound) {
		mostRecentPilotID = compound.getUniqueId("mostRecentPilotID");
		if(mostRecentPilotID.getLeastSignificantBits() == 0L && mostRecentPilotID.getMostSignificantBits() == 0L){
			//UUID parameter was empty, go back to null
			mostRecentPilotID = null;
		}
	}
	
	private static void sendPlayerPilotingPacket(EntityPlayerMP toSend, PhysicsWrapperEntity entityPilotingPacket){
		UUID entityUniqueID = nullID;
		if(entityPilotingPacket != null){
			entityUniqueID = entityPilotingPacket.getUniqueID();
		}
		SetShipPilotMessage message = new SetShipPilotMessage(entityUniqueID);
		ValkyrienWarfareControlMod.controlNetwork.sendTo(message, toSend);
	}
	
	public static PhysicsWrapperEntity getShipPlayerIsPiloting(EntityPlayer pilot){
		World playerWorld = pilot.worldObj;
		WorldPhysObjectManager worldManager = ValkyrienWarfareMod.physicsManager.getManagerForWorld(playerWorld);
		for(PhysicsWrapperEntity wrapperEntity:worldManager.physicsEntities){
			if(wrapperEntity.wrapping.pilotingController.getPilotEntity() == pilot){
				return wrapperEntity;
			}
		}
		return null;
	}
}
