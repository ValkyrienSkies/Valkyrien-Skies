package ValkyrienWarfareControl.Piloting;

import java.util.UUID;

import ValkyrienWarfareBase.PhysicsManagement.PhysicsObject;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Used only on the Server ship entity
 * @author thebest108
 *
 */
public class ShipPilotingController {

	public final PhysicsObject controlledShip;
	private EntityPlayerMP shipPilot;
	
	private UUID mostRecentPilotID;
	
	public ShipPilotingController(PhysicsObject toControl){
		controlledShip = toControl;
	}
	
	public EntityPlayerMP getPilotEntity(){
		return shipPilot;
	}
	
	public void setPilotEntity(EntityPlayerMP toSet){
		//Send packets here or something
		shipPilot = toSet;
		mostRecentPilotID = toSet.getPersistentID();
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
}
