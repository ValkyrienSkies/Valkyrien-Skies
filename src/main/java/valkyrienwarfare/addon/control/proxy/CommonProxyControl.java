package valkyrienwarfare.addon.control.proxy;

import valkyrienwarfare.addon.control.ControlEventsCommon;
import valkyrienwarfare.api.addons.ModuleProxy;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLStateEvent;

public class CommonProxyControl extends ModuleProxy {

	public void preInit(FMLStateEvent event) {

	}

	public void init(FMLStateEvent event) {
		MinecraftForge.EVENT_BUS.register(new ControlEventsCommon());
	}

	public void postInit(FMLStateEvent event) {

	}

}
