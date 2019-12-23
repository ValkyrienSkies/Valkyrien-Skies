package org.valkyrienskies.mod.proxy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.valkyrienskies.mod.client.gui.VSGuiHandler;
import org.valkyrienskies.mod.common.EventsCommon;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent e) {
    }

    public void init(FMLInitializationEvent e) {
        MinecraftForge.EVENT_BUS.register(new EventsCommon());
        NetworkRegistry.INSTANCE.registerGuiHandler(ValkyrienSkiesMod.INSTANCE, new VSGuiHandler());
    }

    public void postInit(FMLPostInitializationEvent e) {
    }

}
