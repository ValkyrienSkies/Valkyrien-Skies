package org.valkyrienskies.mod.proxy;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.valkyrienskies.mod.client.EventsClient;
import org.valkyrienskies.mod.client.VSKeyHandler;
import org.valkyrienskies.mod.client.render.GibsModelRegistry;
import org.valkyrienskies.mod.client.render.TileEntitySmallShipSailRenderer;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.tileentity.TileEntitySmallShipSail;

public class ClientProxy extends CommonProxy {
    // This can be called from addon code because it doesnt set namespace:id.
    public void registerItemRender(Item item, int meta) {
        ModelLoader.setCustomModelResourceLocation(item, meta,
            new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }

    private final VSKeyHandler keyEvents = new VSKeyHandler();

    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
        OBJLoader.INSTANCE.addDomain(ValkyrienSkiesMod.MOD_ID.toLowerCase());

        // Register events
        MinecraftForge.EVENT_BUS.register(new EventsClient());
        MinecraftForge.EVENT_BUS.register(keyEvents);

        // Register VS Minecraft resource reload listener.
        IReloadableResourceManager mcResourceManager = (IReloadableResourceManager) Minecraft
            .getMinecraft()
            .getResourceManager();

        // When Minecraft reloads resources tell GibsModelRegistry to delete all its caches.
        mcResourceManager.registerReloadListener(GibsModelRegistry::onResourceManagerReload);

        registerAnimations();
    }

    @Override
    public void postInit(FMLPostInitializationEvent e) {
        super.postInit(e);
        Minecraft.getMinecraft().getFramebuffer().enableStencil();

        registerBlockItem(ValkyrienSkiesMod.INSTANCE.captainsChair);
        registerBlockItem(ValkyrienSkiesMod.INSTANCE.passengerChair);

        registerTileEntityRenderers();
    }

    private void registerTileEntityRenderers() {
        ClientRegistry.bindTileEntitySpecialRenderer(
                TileEntitySmallShipSail.class,
                new TileEntitySmallShipSailRenderer()
        );
    }

    private void registerAnimations() {
        registerRudderGibs("boats_rudder_geo", "rudder_geo");
        registerRudderGibs("boats_rudder_axle_geo", "rudder_axle_geo");
    }

    private void registerRudderGibs(String name, String modelName) {
        GibsModelRegistry.registerGibsModel(
                name, new ResourceLocation(
                        ValkyrienSkiesMod.MOD_ID,
                        "block/rudder/" + modelName + ".obj"
                )
        );
    }

    // Registers the inventory model for the ItemBlock of "toRegister"
    private static void registerBlockItem(Block toRegister) {
        Item item = Item.getItemFromBlock(toRegister);
        Minecraft.getMinecraft()
                .getRenderItem()
                .getItemModelMesher()
                .register(item, 0, new ModelResourceLocation(
                        ValkyrienSkiesMod.MOD_ID + ":" + item.getTranslationKey()
                                .substring(5), "inventory"));
    }

}
