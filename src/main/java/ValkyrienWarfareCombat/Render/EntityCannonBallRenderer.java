package ValkyrienWarfareCombat.Render;

import ValkyrienWarfareBase.Render.FastBlockModelRenderer;
import ValkyrienWarfareCombat.ValkyrienWarfareCombatMod;
import ValkyrienWarfareCombat.Entity.EntityCannonBall;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class EntityCannonBallRenderer extends Render<EntityCannonBall> {

	private final IBlockState cannonballState;

	protected EntityCannonBallRenderer(RenderManager renderManager) {
		super(renderManager);
		cannonballState = ValkyrienWarfareCombatMod.instance.fakecannonblock.getStateFromMeta(2);
	}

	@Override
	public void doRender(EntityCannonBall entity, double x, double y, double z, float entityYaw, float partialTicks) {
		World world = entity.world;

		this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.pushMatrix();
		GlStateManager.disableLighting();
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer vertexbuffer = tessellator.getBuffer();

		GlStateManager.translate((float) (x - .25D), (float) (y - .07D), (float) (z + .278D));
		FastBlockModelRenderer.renderBlockModel(vertexbuffer, tessellator, world, cannonballState, entity.getBrightnessForRender(partialTicks));

		if (this.renderOutlines) {
			GlStateManager.disableOutlineMode();
			GlStateManager.disableColorMaterial();
		}

		GlStateManager.enableLighting();
		GlStateManager.popMatrix();

		super.doRender(entity, x, y, z, entityYaw, partialTicks);
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
