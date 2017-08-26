package ValkyrienWarfareControl.Proxy;

import ValkyrienWarfareControl.ControlEventsCommon;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxyControl {

	public void preInit(FMLPreInitializationEvent event) {

	}

	public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new ControlEventsCommon());
	}

	public void postInit(FMLPostInitializationEvent event) {

	}

}
