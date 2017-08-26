package ValkyrienWarfareControl.Proxy;

import ValkyrienWarfareBase.API.Addons.ModuleProxy;
import ValkyrienWarfareControl.ControlEventsCommon;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
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
