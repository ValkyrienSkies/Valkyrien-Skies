package valkyrienwarfare.addon.combat.render;

import valkyrienwarfare.addon.combat.entity.EntityCannonBall;
import valkyrienwarfare.addon.combat.entity.EntityCannonBasic;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class EntityCannonBasicRenderFactory implements IRenderFactory<EntityCannonBasic> {

	@Override
	public Render<? super EntityCannonBasic> createRenderFor(RenderManager manager) {
		return new EntityCannonBasicRender(manager);
	}

	public static class EntityCannonBallRenderFactory implements IRenderFactory<EntityCannonBall> {
		@Override
		public Render<? super EntityCannonBall> createRenderFor(RenderManager manager) {
			return new EntityCannonBallRenderer(manager);
		}

	}
}
