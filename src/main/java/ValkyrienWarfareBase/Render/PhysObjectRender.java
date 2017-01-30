package ValkyrienWarfareBase.Render;

import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

/**
 * This class does nothing; on purpose
 * 
 * @author thebest108
 *
 */
public class PhysObjectRender extends Render<PhysicsWrapperEntity> {

	public PhysObjectRender(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(PhysicsWrapperEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {

	}

	@Override
	protected ResourceLocation getEntityTexture(PhysicsWrapperEntity entity) {
		return TextureMap.LOCATION_BLOCKS_TEXTURE;
	}

}
