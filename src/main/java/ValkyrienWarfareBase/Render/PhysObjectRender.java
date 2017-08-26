package ValkyrienWarfareBase.Render;

import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

/**
 * This class does nothing; on purpose
 *
 * @author thebest108
 */
public class PhysObjectRender extends Render<PhysicsWrapperEntity> {

	public static double renderX;
	public static double renderY;
	public static double renderZ;
	public static double renderYaw;

	public PhysObjectRender(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(PhysicsWrapperEntity wrapper, double x, double y, double z, float entityYaw, float partialTicks) {

		/*

		GL11.glPushMatrix();
		wrapper.wrapping.renderer.setupTranslation(partialTicks);

		GL11.glLineWidth(2.5f);
		GL11.glColor3d(1.0, 0.0, 0.0);
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex3d(0, 0, 0D);
		GL11.glVertex3d(0, 1, 0);
		GL11.glEnd();

		IDraggable draggable = EntityDraggable.getDraggableFromEntity(Minecraft.getMinecraft().player);

//		partialTicks *= -1;

//		Minecraft.getMinecraft().player.posX += draggable.getVelocityAddedToPlayer().X * partialTicks;
//		Minecraft.getMinecraft().player.posY += draggable.getVelocityAddedToPlayer().Y * partialTicks;
//		Minecraft.getMinecraft().player.posZ += draggable.getVelocityAddedToPlayer().Z * partialTicks;
//		Minecraft.getMinecraft().player.prevPosX += draggable.getVelocityAddedToPlayer().X * partialTicks;
//		Minecraft.getMinecraft().player.prevPosY += draggable.getVelocityAddedToPlayer().Y * partialTicks;
//		Minecraft.getMinecraft().player.prevPosZ += draggable.getVelocityAddedToPlayer().Z * partialTicks;
//		Minecraft.getMinecraft().player.rotationYaw += draggable.getYawDifVelocity() * partialTicks;

        Minecraft.getMinecraft().entityRenderer.getMouseOver(partialTicks);

//        Minecraft.getMinecraft().player.posX -= draggable.getVelocityAddedToPlayer().X * partialTicks;
//		Minecraft.getMinecraft().player.posY -= draggable.getVelocityAddedToPlayer().Y * partialTicks;
//		Minecraft.getMinecraft().player.posZ -= draggable.getVelocityAddedToPlayer().Z * partialTicks;
//		Minecraft.getMinecraft().player.prevPosX -= draggable.getVelocityAddedToPlayer().X * partialTicks;
//		Minecraft.getMinecraft().player.prevPosY -= draggable.getVelocityAddedToPlayer().Y * partialTicks;
//		Minecraft.getMinecraft().player.prevPosZ -= draggable.getVelocityAddedToPlayer().Z * partialTicks;
//		Minecraft.getMinecraft().player.rotationYaw -= draggable.getYawDifVelocity() * partialTicks;

//		partialTicks *= -1;

        RayTraceResult movingObjectPositionIn = Minecraft.getMinecraft().objectMouseOver;

        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();

        EntityPlayer player = Minecraft.getMinecraft().player;

        double xOff = wrapper.wrapping.renderer.offsetPos.getX();
        double yOff = wrapper.wrapping.renderer.offsetPos.getY();
        double zOff = wrapper.wrapping.renderer.offsetPos.getZ();

        double oldX = vertexbuffer.xOffset;
        double oldY = vertexbuffer.yOffset;
        double oldZ = vertexbuffer.zOffset;


        vertexbuffer.xOffset = -xOff;
        vertexbuffer.yOffset = -yOff;
        vertexbuffer.zOffset = -zOff;

//        GL11.glTranslated(-movingObjectPositionIn.getBlockPos().x, -movingObjectPositionIn.getBlockPos().y, -movingObjectPositionIn.getBlockPos().z);
        this.drawSelectionBoxOriginal(player, movingObjectPositionIn, 0, partialTicks);

        vertexbuffer.xOffset = oldX;
        vertexbuffer.yOffset = oldY;
        vertexbuffer.zOffset = oldZ;

//        wrapper.wrapping.renderer.inverseTransform(partialTicks);
		GL11.glPopMatrix();
		*/

		if (this.canRenderName(wrapper)) {
			this.renderLivingLabel(wrapper, wrapper.getDisplayName().getFormattedText(), x, y, z, 64);
		}
	}

	public void drawSelectionBoxOriginal(EntityPlayer player, RayTraceResult movingObjectPositionIn, int execute, float partialTicks) {
		if (execute == 0 && movingObjectPositionIn.typeOfHit == RayTraceResult.Type.BLOCK) {
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			GlStateManager.glLineWidth(2.0F);
			GlStateManager.disableTexture2D();
			GlStateManager.depthMask(false);
			BlockPos blockpos = movingObjectPositionIn.getBlockPos();
			IBlockState iblockstate = player.world.getBlockState(blockpos);

			if (iblockstate.getMaterial() != Material.AIR && player.world.getWorldBorder().contains(blockpos)) {
				double d0 = 0;// player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) partialTicks;
				double d1 = 0;//player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) partialTicks;
				double d2 = 0;//player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) partialTicks;
				Minecraft.getMinecraft().renderGlobal.drawSelectionBoundingBox(iblockstate.getSelectedBoundingBox(player.world, blockpos).expandXyz(0.0020000000949949026D).offset(-d0, -d1, -d2), 0.0F, 0.0F, 0.0F, 0.4F);
			}

			GlStateManager.depthMask(true);
			GlStateManager.enableTexture2D();
			GlStateManager.disableBlend();
		}
	}

	@Override
	protected ResourceLocation getEntityTexture(PhysicsWrapperEntity entity) {
		return TextureMap.LOCATION_BLOCKS_TEXTURE;
	}

}
