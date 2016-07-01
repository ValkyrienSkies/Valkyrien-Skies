package ValkyrienWarfareBase;

import ValkyrienWarfareBase.Interaction.CustomPlayerControllerMP;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;

public class EventsClient {

	private final static Minecraft mc = Minecraft.getMinecraft();
	
	@SubscribeEvent
	public void onChunkLoadClient(ChunkEvent.Load event){
		
	}
	
	@SubscribeEvent
	public void onChunkUnloadClient(ChunkEvent.Unload event){
		
	}
	
	@SubscribeEvent
	public void onRenderTickEvent(RenderTickEvent event){
		if(mc.thePlayer!=null&&mc.playerController!=null){
			if(!(mc.playerController instanceof CustomPlayerControllerMP)){
				PlayerControllerMP oldController = mc.playerController;
				mc.playerController = new CustomPlayerControllerMP(mc, mc.getConnection());
				mc.playerController.setGameType(oldController.getCurrentGameType());
			}
		}
	}
	
	@SubscribeEvent(priority=EventPriority.HIGHEST)
	public void onDrawBlockHighlightEvent(DrawBlockHighlightEvent event){
		/*WorldPhysObjectManager physManager = ValkyrienWarfareMod.physicsManager.getManagerForWorld(event.getPlayer().worldObj);
		
		AxisAlignedBB playerRangeBB = event.getPlayer().getEntityBoundingBox();
		
		List<PhysicsWrapperEntity> nearbyShips = physManager.getNearbyPhysObjects(event.getPlayer().worldObj, playerRangeBB);
		float partialTick = event.getPartialTicks();
		boolean changed = false;
		
		Entity entity = event.getPlayer();
		
		double d0 = (double)Minecraft.getMinecraft().playerController.getBlockReachDistance();
		
		Vec3d playerEyesPos = entity.getPositionEyes(event.getPartialTicks());
        Vec3d playerReachVector = entity.getLook(event.getPartialTicks());
        
        if(Minecraft.getMinecraft().pointedEntity!=null&&Minecraft.getMinecraft().pointedEntity instanceof PhysicsWrapperEntity){
        	Minecraft.getMinecraft().pointedEntity = null;
        	Minecraft.getMinecraft().entityRenderer.pointedEntity = null;
        	Minecraft.getMinecraft().objectMouseOver = entity.rayTrace(d0, event.getPartialTicks());
        }
        
        double worldResultDistFromPlayer = Minecraft.getMinecraft().objectMouseOver.hitVec.distanceTo(playerEyesPos);
		
		for(PhysicsWrapperEntity wrapper:nearbyShips){
			
			
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
		}*/
	}
	
}
