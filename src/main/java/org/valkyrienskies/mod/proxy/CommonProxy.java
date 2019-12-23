package org.valkyrienskies.mod.proxy;

import net.minecraft.item.Item;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.valkyrienskies.mod.client.gui.VSGuiHandler;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;

public class CommonProxy {

    public void registerItemRender(Item item, int i) {
    }

    public void preInit(FMLPreInitializationEvent e) {
    }

    public void init(FMLInitializationEvent e) {
        NetworkRegistry.INSTANCE.registerGuiHandler(ValkyrienSkiesMod.INSTANCE, new VSGuiHandler());
    }

    public void postInit(FMLPostInitializationEvent e) {
    }

}
