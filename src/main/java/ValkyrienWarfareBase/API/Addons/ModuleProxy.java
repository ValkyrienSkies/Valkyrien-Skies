package ValkyrienWarfareBase.API.Addons;

import net.minecraftforge.fml.common.event.FMLStateEvent;

public abstract class ModuleProxy {
	public abstract void preInit(FMLStateEvent event);
	
	public abstract void init(FMLStateEvent event);
	
	public abstract void postInit(FMLStateEvent event);
}
