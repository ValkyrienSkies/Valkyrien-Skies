package ValkyrienWarfareBase.Render;

import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class PhysObjectRenderFactory implements IRenderFactory<PhysicsWrapperEntity> {

	@Override
	public Render createRenderFor(RenderManager manager) {
		return new PhysObjectRender(manager);
	}

}
