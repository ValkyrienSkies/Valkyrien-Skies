/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2018 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package valkyrienwarfare.mod.proxy;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import valkyrienwarfare.mod.client.EventsClient;
import valkyrienwarfare.mod.client.VWKeyHandler;
import valkyrienwarfare.mod.client.render.GibsModelRegistry;
import valkyrienwarfare.mod.client.render.PhysicsWrapperEntityRenderFactory;
import valkyrienwarfare.mod.client.render.tile_entity_renderers.TileEntityPhysicsInfuserRenderer;
import valkyrienwarfare.mod.common.ValkyrienWarfareMod;
import valkyrienwarfare.mod.common.entity.PhysicsWrapperEntity;
import valkyrienwarfare.mod.common.math.Quaternion;
import valkyrienwarfare.mod.common.math.Vector;
import valkyrienwarfare.mod.common.tileentity.TileEntityPhysicsInfuser;

public class ClientProxy extends CommonProxy {

    public static ICamera lastCamera;
    private final VWKeyHandler keyEvents = new VWKeyHandler();

    private static void registerBlockItem(Block toRegister) {
        Item item = Item.getItemFromBlock(toRegister);
        Minecraft.getMinecraft()
                .getRenderItem()
                .getItemModelMesher()
                .register(item, 0, new ModelResourceLocation(ValkyrienWarfareMod.MOD_ID + ":" + item.getTranslationKey()
                        .substring(5), "inventory"));
    }

    private static void registerItemModel(Item toRegister) {
        RenderItem renderItem = Minecraft.getMinecraft()
                .getRenderItem();
        renderItem.getItemModelMesher()
                .register(toRegister, 0, new ModelResourceLocation(ValkyrienWarfareMod.MOD_ID + ":" + toRegister.getTranslationKey()
                        .substring(5), "inventory"));
    }

    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
        OBJLoader.INSTANCE.addDomain(ValkyrienWarfareMod.MOD_ID.toLowerCase());
        RenderingRegistry.registerEntityRenderingHandler(PhysicsWrapperEntity.class, new PhysicsWrapperEntityRenderFactory());
        // Register events
        MinecraftForge.EVENT_BUS.register(new EventsClient());
        MinecraftForge.EVENT_BUS.register(keyEvents);

        // Register VW Minecraft resource reload listener.
        IReloadableResourceManager mcResourceManager = (IReloadableResourceManager) Minecraft.getMinecraft()
                .getResourceManager();

        // When Minecraft reloads resources tell GibsModelRegistry to delete all its caches.
        mcResourceManager.registerReloadListener(GibsModelRegistry::onResourceManagerReload);

        //  new ModelResourceLocation("valkyrienwarfarecontrol:infuser_core_main", "inventory"),
    }

    @Override
    public void postInit(FMLPostInitializationEvent e) {
        super.postInit(e);
        Minecraft.getMinecraft().getFramebuffer().enableStencil();

        // Register physics infuser tile entity renderer.
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPhysicsInfuser.class, new TileEntityPhysicsInfuserRenderer());
    }

    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);
        registerBlockItem(ValkyrienWarfareMod.INSTANCE.physicsInfuser);
        registerBlockItem(ValkyrienWarfareMod.INSTANCE.physicsInfuserCreative);


        // registerItemModel(ValkyrienWarfareMod.INSTANCE.physicsCore);

        RenderItem renderItem = Minecraft.getMinecraft()
                .getRenderItem();
        renderItem.getItemModelMesher()
                .register(ValkyrienWarfareMod.INSTANCE.physicsCore, 0,
                        new ModelResourceLocation("valkyrienwarfarecontrol:testmodel", "inventory"));
    }

    @Override
    public void updateShipPartialTicks(PhysicsWrapperEntity entity) {
        double partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();
        // entity.wrapping.renderer.updateTranslation(partialTicks);
        Vector centerOfRotation = entity.getPhysicsObject().getCenterCoord();
        if (entity.getPhysicsObject().getShipRenderer() == null) {
            return;
        }
        entity.getPhysicsObject().getShipRenderer().curPartialTick = partialTicks;

        double moddedX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
        double moddedY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
        double moddedZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;
        double p0 = Minecraft.getMinecraft().player.lastTickPosX +
                (Minecraft.getMinecraft().player.posX - Minecraft.getMinecraft().player.lastTickPosX) * partialTicks;
        double p1 = Minecraft.getMinecraft().player.lastTickPosY +
                (Minecraft.getMinecraft().player.posY - Minecraft.getMinecraft().player.lastTickPosY) * partialTicks;
        double p2 = Minecraft.getMinecraft().player.lastTickPosZ +
                (Minecraft.getMinecraft().player.posZ - Minecraft.getMinecraft().player.lastTickPosZ) * partialTicks;

        Quaternion smoothRotation = entity.getPhysicsObject().getShipRenderer().getSmoothRotationQuat(partialTicks);
        double[] radians = smoothRotation.toRadians();

        double moddedPitch = Math.toDegrees(radians[0]);
        double moddedYaw = Math.toDegrees(radians[1]);
        double moddedRoll = Math.toDegrees(radians[2]);

        entity.getPhysicsObject().getShipTransformationManager()
                .updateRenderTransform(moddedX, moddedY, moddedZ, moddedPitch, moddedYaw, moddedRoll);
    }
}
