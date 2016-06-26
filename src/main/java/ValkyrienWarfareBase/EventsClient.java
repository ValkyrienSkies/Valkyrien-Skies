package ValkyrienWarfareBase;

import java.util.List;

import ValkyrienWarfareBase.Math.RotationMatrices;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.PhysicsManagement.WorldPhysObjectManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventsClient {

	@SubscribeEvent
	public void onChunkLoadClient(ChunkEvent.Load event){
		
	}
	
	@SubscribeEvent
	public void onChunkUnloadClient(ChunkEvent.Unload event){
		
	}
	
	@SubscribeEvent(priority=EventPriority.HIGHEST)
	public void onDrawBlockHighlightEvent(DrawBlockHighlightEvent event){
		WorldPhysObjectManager physManager = ValkyrienWarfareMod.physicsManager.getManagerForWorld(event.getPlayer().worldObj);
		
		AxisAlignedBB playerRangeBB = event.getPlayer().getEntityBoundingBox();
		
		List<PhysicsWrapperEntity> nearbyShips = physManager.getNearbyPhysObjects(event.getPlayer().worldObj, playerRangeBB);
		float partialTick = event.getPartialTicks();
		boolean changed = false;
		
		Entity entity = event.getPlayer();
		
		Vec3d playerEyesPos = entity.getPositionEyes(event.getPartialTicks());
        Vec3d playerReachVector = entity.getLook(event.getPartialTicks());
        
        double worldResultDistFromPlayer = Minecraft.getMinecraft().objectMouseOver.hitVec.distanceTo(playerEyesPos);
		
		for(PhysicsWrapperEntity wrapper:nearbyShips){
			
			double d0 = (double)Minecraft.getMinecraft().playerController.getBlockReachDistance();
            
            playerEyesPos = entity.getPositionEyes(event.getPartialTicks());
            playerReachVector = entity.getLook(event.getPartialTicks());
            
            //Transform the coordinate system for the player eye pos
            playerEyesPos = RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.wToLTransform, playerEyesPos);
            playerReachVector = RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.wToLRotation, playerReachVector);
            
            Vec3d playerEyesReachAdded = playerEyesPos.addVector(playerReachVector.xCoord * d0, playerReachVector.yCoord * d0, playerReachVector.zCoord * d0);
            
            RayTraceResult resultInShip = entity.worldObj.rayTraceBlocks(playerEyesPos, playerEyesReachAdded, false, false, true);
            
            double shipResultDistFromPlayer = resultInShip.hitVec.distanceTo(playerEyesPos);
            
            if(shipResultDistFromPlayer<worldResultDistFromPlayer){
            	worldResultDistFromPlayer = shipResultDistFromPlayer;
            	Minecraft.getMinecraft().objectMouseOver = resultInShip;
            }
		}
	}
	
}
