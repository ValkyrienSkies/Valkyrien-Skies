package org.valkyrienskies.addon.control.proxy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.valkyrienskies.addon.control.ControlEventsCommon;
import org.valkyrienskies.addon.control.ValkyrienSkiesControl;
import org.valkyrienskies.addon.control.gui.VSGuiHandler;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;

public class CommonProxyControl  {

    public void preInit(FMLStateEvent event) {

    }

    public void init(FMLStateEvent event) {
        MinecraftForge.EVENT_BUS.register(new ControlEventsCommon());
        NetworkRegistry.INSTANCE.registerGuiHandler(ValkyrienSkiesControl.INSTANCE, new VSGuiHandler());
    }

    public void postInit(FMLStateEvent event) {

    }

}
