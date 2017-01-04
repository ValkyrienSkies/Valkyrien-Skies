package ValkyrienWarfareWorld.Render;

import ValkyrienWarfareWorld.EntityFallingUpBlock;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderFallingBlock;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class EntityFallingUpBlockRenderFactory implements IRenderFactory<EntityFallingUpBlock> {

	@Override
	public Render<? super EntityFallingUpBlock> createRenderFor(RenderManager manager) {
		return new RenderFallingBlock(manager);
	}

}
