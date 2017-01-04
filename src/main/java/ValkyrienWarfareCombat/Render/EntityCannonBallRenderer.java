package ValkyrienWarfareCombat.Render;

import ValkyrienWarfareCombat.ValkyrienWarfareCombatMod;
import ValkyrienWarfareCombat.Entity.EntityCannonBall;
import ValkyrienWarfareCombat.Entity.EntityCannonBasic;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityCannonBallRenderer extends Render<EntityCannonBall> {

	private final IBlockState cannonballState;

	protected EntityCannonBallRenderer(RenderManager renderManager) {
		super(renderManager);
		cannonballState = ValkyrienWarfareCombatMod.instance.fakeCannonBlock.getStateFromMeta(2);
	}

	@Override
	public void doRender(EntityCannonBall entity, double x, double y, double z, float entityYaw, float partialTicks) {
		IBlockState iblockstate = ValkyrienWarfareCombatMod.instance.fakeCannonBlock.getStateFromMeta(2);

		double renderX = (entity.posX - entity.lastTickPosX) * partialTicks + entity.lastTickPosX;
		double renderY = (entity.posY - entity.lastTickPosY) * partialTicks + entity.lastTickPosY;
		double renderZ = (entity.posZ - entity.lastTickPosZ) * partialTicks + entity.lastTickPosZ;

		if (iblockstate.getRenderType() == EnumBlockRenderType.MODEL) {
			World world = entity.worldObj;

			if (iblockstate != world.getBlockState(new BlockPos(entity)) && iblockstate.getRenderType() != EnumBlockRenderType.INVISIBLE) {
				this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
				GlStateManager.pushMatrix();
				GlStateManager.disableLighting();
				Tessellator tessellator = Tessellator.getInstance();
				VertexBuffer vertexbuffer = tessellator.getBuffer();

				if (this.renderOutlines) {
					GlStateManager.enableColorMaterial();
					GlStateManager.enableOutlineMode(this.getTeamColor(entity));
				}

				vertexbuffer.begin(7, DefaultVertexFormats.BLOCK);
				// BlockPos blockpos = new BlockPos(renderX, renderY, renderZ);
				GlStateManager.translate((float) (x - renderX + .25D), (float) (y - renderY - .07D), (float) (z - renderZ + .778D));
				BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

				vertexbuffer.setTranslation(renderX - .5D, renderY, renderZ - .5D);

				blockrendererdispatcher.getBlockModelRenderer().renderModel(world, blockrendererdispatcher.getModelForState(iblockstate), iblockstate, BlockPos.ORIGIN, vertexbuffer, false, 0);
				tessellator.draw();

				vertexbuffer.setTranslation(0, 0, 0);

				if (this.renderOutlines) {
					GlStateManager.disableOutlineMode();
					GlStateManager.disableColorMaterial();
				}

				GlStateManager.enableLighting();
				GlStateManager.popMatrix();
				super.doRender(entity, x, y, z, entityYaw, partialTicks);
			}
		}
	}

	@Override
	public boolean shouldRender(EntityCannonBall livingEntity, ICamera camera, double camX, double camY, double camZ) {
		return true;
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityCannonBall entity) {
		return null;
	}

}
