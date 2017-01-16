package ValkyrienWarfareControl.Piloting;

import java.util.UUID;


import ValkyrienWarfareBase.NBTUtils;
import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsObject;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.PhysicsManagement.WorldPhysObjectManager;
import ValkyrienWarfareControl.ValkyrienWarfareControlMod;
import ValkyrienWarfareControl.Block.BlockShipPilotsChair;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
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
	
	private boolean hasChair = false;
	private BlockPos chairPosition = BlockPos.ORIGIN;
	
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
		//Set to whatever the player was pointing at in Ship space
		//These vectors can be re-arranged depending on the direction the chair was placed
		
		IBlockState state = controlledShip.worldObj.getBlockState(chairPosition);
		double[] pilotRotationMatrix = getRotationMatrixFromBlockState(state, chairPosition);
		
		Vector playerDirection = new Vector(1,0,0);
		
		Vector rightDirection = new Vector(0,0,1);
		
		Vector leftDirection = new Vector(0,0,-1);
		
		RotationMatrices.applyTransform(pilotRotationMatrix, playerDirection);
		RotationMatrices.applyTransform(pilotRotationMatrix, rightDirection);
		RotationMatrices.applyTransform(pilotRotationMatrix, leftDirection);
		
		Vector upDirection = new Vector(0,1,0);
		
		Vector downDirection = new Vector(0,-1,0);
		
		Vector idealAngularDirection = new Vector();
		
		Vector idealLinearVelocity = new Vector();
		
		Vector shipUp = new Vector(0,1,0);
		Vector shipUpPos = new Vector(0,1,0);
		
		if(message.airshipForward){
			idealLinearVelocity.add(playerDirection);
		}
		if(message.airshipBackward){
			idealLinearVelocity.subtract(playerDirection);
		}
		
		RotationMatrices.applyTransform(controlledShip.coordTransform.lToWRotation, idealLinearVelocity);
		
		RotationMatrices.applyTransform(controlledShip.coordTransform.lToWRotation, shipUp);
		
		if(message.airshipUp){
			idealLinearVelocity.add(upDirection);
		}
		if(message.airshipDown){
			idealLinearVelocity.add(downDirection);
		}
		
		
		
		if(message.airshipRight){
			idealAngularDirection.add(rightDirection);
		}
		if(message.airshipLeft){
			idealAngularDirection.add(leftDirection);
		}
		
		
		Vector shipUpOffset = shipUp.getSubtraction(shipUpPos);
		
		
		
		double mass = controlledShip.physicsProcessor.mass;
		
		idealAngularDirection.multiply(mass/2.5D);
		idealLinearVelocity.multiply(mass/5D);
		shipUpOffset.multiply(mass/2.5D);
		
		if(message.airshipSprinting){
			idealLinearVelocity.multiply(2D);
		}
		
		idealLinearVelocity.subtract(idealAngularDirection);
		idealLinearVelocity.subtract(shipUpOffset);
		
		//TEMPORARY CODE!!!
		
		controlledShip.physicsProcessor.addForceAtPoint(playerDirection, idealAngularDirection);
		
		controlledShip.physicsProcessor.addForceAtPoint(shipUpPos, shipUpOffset);
		
		controlledShip.physicsProcessor.addForceAtPoint(new Vector(), idealLinearVelocity);
		
		controlledShip.physicsProcessor.convertTorqueToVelocity();
		
//		RotationMatrices.applyTransform(controlledShip.coordTransform.lToWRotation, idealAngularDirection);
//		System.out.println(idealAngularDirection);
	}

	/**
	 * Gets called whenever world.setBlockState is called inside of Ship Space
	 * @param posChanged
	 */
	public void onSetBlockInShip(BlockPos posChanged, IBlockState newState){
		if(getHasPilotChair()){
			if(posChanged.equals(chairPosition)){
				if(!newState.getBlock().equals(ValkyrienWarfareControlMod.instance.pilotsChair)){
					hasChair = false;
					chairPosition = BlockPos.ORIGIN;
				}
			}else{
				if(newState.getBlock().equals(ValkyrienWarfareControlMod.instance.pilotsChair)){
					controlledShip.worldObj.destroyBlock(posChanged, true);
				}
			}
		}else{
			if(newState.getBlock().equals(ValkyrienWarfareControlMod.instance.pilotsChair)){
				hasChair = true;
				chairPosition = posChanged;
			}
		}
	}
	
	public BlockPos getPilotChairPosition(){
		return chairPosition;
	}
	
	public boolean getHasPilotChair(){
		return hasChair;
	}
	
	/**
	 * Sets the inputed player as the pilot of this ship
	 * @param toSet
	 * @param ignorePilotConflicts Should be set to false for almost every single case
	 */
	public void setPilotEntity(EntityPlayerMP toSet, boolean ignorePilotConflicts){
		if(shipPilot != null){
			sendPlayerPilotingPacket(shipPilot, null);
			//TEMPORARY CODE!!!
			controlledShip.physicsProcessor.actAsArchimedes = false;
		}else{
			//TEMPORARY CODE!!!
			controlledShip.physicsProcessor.actAsArchimedes = true;
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
		compound.setBoolean("hasChair", hasChair);
		NBTUtils.writeBlockPosToNBT("chairPosition", chairPosition, compound);
	}
	
	public void readFromNBTTag(NBTTagCompound compound) {
		mostRecentPilotID = compound.getUniqueId("mostRecentPilotID");
		if(mostRecentPilotID.getLeastSignificantBits() == 0L && mostRecentPilotID.getMostSignificantBits() == 0L){
			//UUID parameter was empty, go back to null
			mostRecentPilotID = null;
		}
		hasChair = compound.getBoolean("hasChair");
		chairPosition = NBTUtils.readBlockPosFromNBT("chairPosition", compound);
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
	
	public static double[] getRotationMatrixFromBlockState(IBlockState state, BlockPos chairPosition)	{
		double playerChairYaw = 0;
		if(state.getBlock() instanceof BlockShipPilotsChair){
			playerChairYaw = BlockShipPilotsChair.getChairYaw(state, chairPosition);
		}
		double[] pilotRotationMatrix = RotationMatrices.getRotationMatrix(0.0D, 1.0D, 0.0D, Math.toRadians(playerChairYaw));
		
		return pilotRotationMatrix;
	}
}
