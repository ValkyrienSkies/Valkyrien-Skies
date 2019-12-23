package org.valkyrienskies.addon.control.proxy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import org.valkyrienskies.addon.control.ControlEventsCommon;

public class CommonProxyControl  {

    public void preInit(FMLStateEvent event) {

    }

    public void init(FMLStateEvent event) {
        MinecraftForge.EVENT_BUS.register(new ControlEventsCommon());
    }

    public void postInit(FMLStateEvent event) {

    }

}
