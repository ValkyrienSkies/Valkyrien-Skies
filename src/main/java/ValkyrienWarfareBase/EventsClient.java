package ValkyrienWarfareBase;

import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.Math.BigBastardMath;
import ValkyrienWarfareBase.Math.Quaternion;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.PhysicsManagement.WorldPhysObjectManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;

public class EventsClient {

	private final static Minecraft mc = Minecraft.getMinecraft();

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onClientTickEvent(ClientTickEvent event) {
		if (mc.theWorld != null) {
			if (!mc.isGamePaused()) {
				WorldPhysObjectManager manager = ValkyrienWarfareMod.physicsManager.getManagerForWorld(mc.theWorld);
				if (event.phase == Phase.END) {
					for (PhysicsWrapperEntity wrapper : manager.physicsEntities) {
						wrapper.wrapping.onPostTickClient();
					}
				}
			}
		}

	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onCameraSetup(CameraSetup event) {

	}

	@SubscribeEvent
	public void onChunkLoadClient(ChunkEvent.Load event) {

	}

	@SubscribeEvent
	public void onChunkUnloadClient(ChunkEvent.Unload event) {

	}

	@SubscribeEvent
	public void onRenderTickEvent(RenderTickEvent event) {
		if (mc.thePlayer != null && mc.playerController != null) {
			// if(!(mc.playerController instanceof CustomPlayerControllerMP)){
			// PlayerControllerMP oldController = mc.playerController;
			// mc.playerController = new CustomPlayerControllerMP(mc, mc.getConnection());
			// mc.playerController.setGameType(oldController.getCurrentGameType());
			// }
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onDrawBlockHighlightEvent(DrawBlockHighlightEvent event) {
		/*
		 * WorldPhysObjectManager physManager = ValkyrienWarfareMod.physicsManager.getManagerForWorld(event.getPlayer().worldObj);
		 * 
		 * AxisAlignedBB playerRangeBB = event.getPlayer().getEntityBoundingBox();
		 * 
		 * List<PhysicsWrapperEntity> nearbyShips = physManager.getNearbyPhysObjects(event.getPlayer().worldObj, playerRangeBB); float partialTick = event.getPartialTicks(); boolean changed = false;
		 * 
		 * Entity entity = event.getPlayer();
		 * 
		 * double d0 = (double)Minecraft.getMinecraft().playerController.getBlockReachDistance();
		 * 
		 * Vec3d playerEyesPos = entity.getPositionEyes(event.getPartialTicks()); Vec3d playerReachVector = entity.getLook(event.getPartialTicks());
		 * 
		 * if(Minecraft.getMinecraft().pointedEntity!=null&&Minecraft.getMinecraft().pointedEntity instanceof PhysicsWrapperEntity){ Minecraft.getMinecraft().pointedEntity = null; Minecraft.getMinecraft().entityRenderer.pointedEntity = null; Minecraft.getMinecraft().objectMouseOver = entity.rayTrace(d0, event.getPartialTicks()); }
		 * 
		 * double worldResultDistFromPlayer = Minecraft.getMinecraft().objectMouseOver.hitVec.distanceTo(playerEyesPos);
		 * 
		 * for(PhysicsWrapperEntity wrapper:nearbyShips){
		 * 
		 * 
		 * playerEyesPos = entity.getPositionEyes(event.getPartialTicks()); playerReachVector = entity.getLook(event.getPartialTicks());
		 * 
		 * //Transform the coordinate system for the player eye pos playerEyesPos = RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.wToLTransform, playerEyesPos); playerReachVector = RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.wToLRotation, playerReachVector);
		 * 
		 * Vec3d playerEyesReachAdded = playerEyesPos.addVector(playerReachVector.xCoord * d0, playerReachVector.yCoord * d0, playerReachVector.zCoord * d0);
		 * 
		 * RayTraceResult resultInShip = entity.worldObj.rayTraceBlocks(playerEyesPos, playerEyesReachAdded, false, false, true);
		 * 
		 * double shipResultDistFromPlayer = resultInShip.hitVec.distanceTo(playerEyesPos);
		 * 
		 * if(shipResultDistFromPlayer<worldResultDistFromPlayer){ worldResultDistFromPlayer = shipResultDistFromPlayer; Minecraft.getMinecraft().objectMouseOver = resultInShip; } }
		 */
	}

	protected static final Vec3d getVectorForRotation(float pitch, float yaw) {
		float f = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
		float f1 = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
		float f2 = -MathHelper.cos(-pitch * 0.017453292F);
		float f3 = MathHelper.sin(-pitch * 0.017453292F);
		return new Vec3d((double) (f1 * f2), (double) f3, (double) (f * f2));
	}

}
