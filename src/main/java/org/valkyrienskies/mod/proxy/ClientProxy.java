/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2019 the Valkyrien Skies team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Skies team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Skies team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package org.valkyrienskies.mod.proxy;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
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
	// This can be called from addon code because it doesnt set namespace:id.
    public void registerItemRender(Item item, int meta) {
        ModelLoader.setCustomModelResourceLocation(item, meta,
            new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }

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
