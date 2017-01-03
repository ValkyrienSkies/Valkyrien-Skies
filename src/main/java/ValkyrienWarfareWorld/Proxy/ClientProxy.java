package ValkyrienWarfareWorld.Proxy;

import ValkyrienWarfareWorld.EntityFallingUpBlock;
import ValkyrienWarfareWorld.Render.EntityFallingUpBlockRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy{

	@Override
	public void preInit(FMLPreInitializationEvent e) {
		RenderingRegistry.registerEntityRenderingHandler(EntityFallingUpBlock.class,new EntityFallingUpBlockRenderFactory());
	}

}
