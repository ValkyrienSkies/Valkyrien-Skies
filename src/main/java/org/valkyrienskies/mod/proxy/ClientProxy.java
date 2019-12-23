package org.valkyrienskies.mod.proxy;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.valkyrienskies.mod.client.EventsClient;
import org.valkyrienskies.mod.client.VSKeyHandler;
import org.valkyrienskies.mod.client.render.GibsAnimationRegistry;
import org.valkyrienskies.mod.client.render.GibsModelRegistry;
import org.valkyrienskies.mod.client.render.PhysicsWrapperEntityRenderFactory;
import org.valkyrienskies.mod.client.render.tile_entity_renderers.TileEntityPhysicsInfuserRenderer;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.entity.PhysicsWrapperEntity;
import org.valkyrienskies.mod.common.tileentity.TileEntityPhysicsInfuser;

public class ClientProxy extends CommonProxy {

    public static ICamera lastCamera;
    private final VSKeyHandler keyEvents = new VSKeyHandler();

    private static void registerBlockItem(Block toRegister) {
        Item item = Item.getItemFromBlock(toRegister);
        Minecraft.getMinecraft()
            .getRenderItem()
            .getItemModelMesher()
            .register(item, 0,
                new ModelResourceLocation(ValkyrienSkiesMod.MOD_ID + ":" + item.getTranslationKey()
                    .substring(5), "inventory"));
    }

    private static void registerItemModel(Item toRegister) {
        RenderItem renderItem = Minecraft.getMinecraft()
            .getRenderItem();
        renderItem.getItemModelMesher()
            .register(toRegister, 0, new ModelResourceLocation(
                ValkyrienSkiesMod.MOD_ID + ":" + toRegister.getTranslationKey()
                    .substring(5), "inventory"));
    }

    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
        OBJLoader.INSTANCE.addDomain(ValkyrienSkiesMod.MOD_ID.toLowerCase());
        RenderingRegistry.registerEntityRenderingHandler(PhysicsWrapperEntity.class,
            new PhysicsWrapperEntityRenderFactory());
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

        // Register physics infuser tile entity renderer.
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPhysicsInfuser.class,
            new TileEntityPhysicsInfuserRenderer());
    }

    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);
        registerBlockItem(ValkyrienSkiesMod.INSTANCE.physicsInfuser);
        registerBlockItem(ValkyrienSkiesMod.INSTANCE.physicsInfuserCreative);

        // registerItemModel(ValkyrienSkiesMod.INSTANCE.physicsCore);

        RenderItem renderItem = Minecraft.getMinecraft()
            .getRenderItem();
        renderItem.getItemModelMesher()
            .register(ValkyrienSkiesMod.INSTANCE.physicsCore, 0,
                new ModelResourceLocation(ValkyrienSkiesMod.MOD_ID + ":testmodel", "inventory"));
    }

    private void registerAnimations() {
        GibsAnimationRegistry.registerAnimation("physics_infuser",
            new ResourceLocation(ValkyrienSkiesMod.MOD_ID,
                "models/block/physics_infuser/physics_infuser.atom"));

        GibsAnimationRegistry.registerAnimation("physics_infuser_empty",
            new ResourceLocation(ValkyrienSkiesMod.MOD_ID,
                "models/block/physics_infuser/physics_infuser_empty.atom"));
        // Not an actual animation, just easier to put in than writing out all the core names.
        GibsAnimationRegistry.registerAnimation("physics_infuser_cores",
            new ResourceLocation(ValkyrienSkiesMod.MOD_ID,
                "models/block/physics_infuser/cores.atom"));
    }
}
