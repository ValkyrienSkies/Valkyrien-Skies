package valkyrienwarfare.proxy;

import valkyrienwarfare.api.addons.Module;
import valkyrienwarfare.api.addons.ModuleProxy;
import valkyrienwarfare.EventsServer;
import valkyrienwarfare.ValkyrienWarfareMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ServerProxy extends CommonProxy {

	@Override
	public void preInit(FMLPreInitializationEvent e) {
		super.preInit(e);
		
		for (Module addon : ValkyrienWarfareMod.addons) {
			ModuleProxy proxy = addon.getServerProxy();
			if (proxy != null)  {
				proxy.preInit(e);
			}
		}
	}

	@Override
	public void init(FMLInitializationEvent e) {
		super.init(e);
		MinecraftForge.EVENT_BUS.register(new EventsServer());
		
		for (Module addon : ValkyrienWarfareMod.addons) {
			ModuleProxy proxy = addon.getServerProxy();
			if (proxy != null)  {
				proxy.init(e);
			}
		}
	}

	@Override
	public void postInit(FMLPostInitializationEvent e) {
		super.postInit(e);
		
		for (Module addon : ValkyrienWarfareMod.addons) {
			ModuleProxy proxy = addon.getServerProxy();
			if (proxy != null)  {
				proxy.postInit(e);
			}
		}
	}

}
