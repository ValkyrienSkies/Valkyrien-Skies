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

package org.valkyrienskies.addon.control.proxy;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import org.valkyrienskies.addon.control.ControlEventsClient;
import org.valkyrienskies.addon.control.ValkyrienSkiesControl;
import org.valkyrienskies.addon.control.block.multiblocks.TileEntityGiantPropellerPart;
import org.valkyrienskies.addon.control.block.multiblocks.TileEntityRudderPart;
import org.valkyrienskies.addon.control.block.multiblocks.TileEntityValkyriumCompressorPart;
import org.valkyrienskies.addon.control.block.multiblocks.TileEntityValkyriumEnginePart;
import org.valkyrienskies.addon.control.block.torque.TileEntityRotationAxle;
import org.valkyrienskies.addon.control.renderer.BasicNodeTileEntityRenderer;
import org.valkyrienskies.addon.control.renderer.GearboxTileEntityRenderer;
import org.valkyrienskies.addon.control.renderer.GiantPropellerPartTileEntityRenderer;
import org.valkyrienskies.addon.control.renderer.LiftLeverTileEntityRenderer;
import org.valkyrienskies.addon.control.renderer.PropellerEngineTileEntityRenderer;
import org.valkyrienskies.addon.control.renderer.RotationAxleTileEntityRenderer;
import org.valkyrienskies.addon.control.renderer.RudderPartTileEntityRenderer;
import org.valkyrienskies.addon.control.renderer.ShipHelmTileEntityRenderer;
import org.valkyrienskies.addon.control.renderer.SpeedTelegraphTileEntityRenderer;
import org.valkyrienskies.addon.control.renderer.ValkyriumCompressorPartTileEntityRenderer;
import org.valkyrienskies.addon.control.renderer.ValkyriumEnginePartTileEntityRenderer;
import org.valkyrienskies.addon.control.tileentity.TileEntityGearbox;
import org.valkyrienskies.addon.control.tileentity.TileEntityLiftLever;
import org.valkyrienskies.addon.control.tileentity.TileEntityNetworkRelay;
import org.valkyrienskies.addon.control.tileentity.TileEntityPropellerEngine;
import org.valkyrienskies.addon.control.tileentity.TileEntityShipHelm;
import org.valkyrienskies.addon.control.tileentity.TileEntitySpeedTelegraph;
import org.valkyrienskies.mod.client.render.GibsAnimationRegistry;
import org.valkyrienskies.mod.client.render.GibsModelRegistry;

@SuppressWarnings("unused")
public class ClientProxyControl extends CommonProxyControl {

    private static void registerBlockItem(Block toRegister) {
        Item item = Item.getItemFromBlock(toRegister);
        Minecraft.getMinecraft()
            .getRenderItem()
            .getItemModelMesher()
            .register(item, 0, new ModelResourceLocation(
                ValkyrienSkiesControl.MOD_ID + ":" + item.getTranslationKey()
                    .substring(5), "inventory"));
    }

    private static void registerItemModel(Item toRegister) {
        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
        ModelResourceLocation modelResourceLocation = new ModelResourceLocation(
            ValkyrienSkiesControl.MOD_ID + ":" + toRegister.getTranslationKey()
                .substring(5), "inventory");
        renderItem.getItemModelMesher()
            .register(toRegister, 0, modelResourceLocation);
    }

    private static void registerBlockItemModels() {
        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vsControlBlocks.basicEngine);
        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vsControlBlocks.advancedEngine);
        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vsControlBlocks.eliteEngine);
        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vsControlBlocks.ultimateEngine);
        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vsControlBlocks.redstoneEngine);

        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vsControlBlocks.captainsChair);
        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vsControlBlocks.passengerChair);
        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vsControlBlocks.shipHelm);

        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vsControlBlocks.speedTelegraph);
        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vsControlBlocks.compactedValkyrium);
        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vsControlBlocks.networkRelay);

        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vsControlBlocks.gyroscopeStabilizer);
        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vsControlBlocks.liftValve);
        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vsControlBlocks.networkDisplay);
        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vsControlBlocks.liftLever);

        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vsControlBlocks.valkyriumCompressorPart);
        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vsControlBlocks.gyroscopeDampener);

        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vsControlBlocks.valkyriumEnginePart);
        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vsControlBlocks.gearbox);
        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vsControlBlocks.rudderPart);

        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vsControlBlocks.giantPropellerPart);
        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vsControlBlocks.rotationAxle);
    }

    private static void registerItemModels() {
        registerItemModel(ValkyrienSkiesControl.INSTANCE.relayWire);
        registerItemModel(ValkyrienSkiesControl.INSTANCE.vanishingWire);
        registerItemModel(ValkyrienSkiesControl.INSTANCE.vsWrench);
    }

    private static void registerTileEntityRenderers() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityNetworkRelay.class,
            new BasicNodeTileEntityRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityShipHelm.class,
            new ShipHelmTileEntityRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySpeedTelegraph.class,
            new SpeedTelegraphTileEntityRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPropellerEngine.class,
            new PropellerEngineTileEntityRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityValkyriumEnginePart.class,
            new ValkyriumEnginePartTileEntityRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGearbox.class,
            new GearboxTileEntityRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLiftLever.class,
            new LiftLeverTileEntityRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityValkyriumCompressorPart.class,
            new ValkyriumCompressorPartTileEntityRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRudderPart.class,
            new RudderPartTileEntityRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGiantPropellerPart.class,
            new GiantPropellerPartTileEntityRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRotationAxle.class,
            new RotationAxleTileEntityRenderer());
    }

    @Override
    public void preInit(FMLStateEvent event) {
        super.preInit(event);

        // Register events
        MinecraftForge.EVENT_BUS.register(new ControlEventsClient());
        // Register gibs
        OBJLoader.INSTANCE.addDomain(ValkyrienSkiesControl.MOD_ID.toLowerCase());

        registerControlGibs("chadburn_dial_simplevoxel_geo");
        registerControlGibs("chadburn_glass_simplevoxel_geo");
        registerControlGibs("chadburn_handles_simplevoxel_geo");
        registerControlGibs("chadburn_speed_telegraph_simplevoxel_geo");

        registerControlGibs("ship_helm_base");
        registerControlGibs("ship_helm_dial");
        registerControlGibs("ship_helm_dial_glass");
        registerControlGibs("ship_helm_wheel");

        registerRudderGibs("rudder_geo");
        registerRudderGibs("rudder_axle_geo");

        registerGearboxGibs("gearbox_back_geo");
        registerGearboxGibs("gearbox_bottom_geo");
        registerGearboxGibs("gearbox_front_geo");
        registerGearboxGibs("gearbox_left_geo");
        registerGearboxGibs("gearbox_right_geo");
        registerGearboxGibs("gearbox_top_geo");

        GibsAnimationRegistry.registerAnimation("valkyrium_compressor",
            new ResourceLocation(ValkyrienSkiesControl.MOD_ID,
                "models/block/valkyrium_compressor/compressor_animations.atom"));

        GibsAnimationRegistry.registerAnimation("valkyrium_engine",
            new ResourceLocation(ValkyrienSkiesControl.MOD_ID,
                "models/block/valkyrium_engine/valkyrium_engine.atom"));

        GibsAnimationRegistry.registerAnimation("lift_lever",
            new ResourceLocation(ValkyrienSkiesControl.MOD_ID,
                "models/block/controls/lift_lever_keyframes.atom"));

        GibsAnimationRegistry.registerAnimation("gearbox",
            new ResourceLocation(ValkyrienSkiesControl.MOD_ID,
                "models/block/gearbox/gearbox.atom"));

        GibsAnimationRegistry.registerAnimation("pocketwatch_body",
            new ResourceLocation(ValkyrienSkiesControl.MOD_ID,
                "models/block/pocketwatch/pocketwatch_keyframes.atom"));

        GibsAnimationRegistry.registerAnimation("pocketwatch_lid",
            new ResourceLocation(ValkyrienSkiesControl.MOD_ID,
                "models/block/pocketwatch/pocketwatch_lid_keyframes.atom"));

        GibsAnimationRegistry.registerAnimation("telescope",
            new ResourceLocation(ValkyrienSkiesControl.MOD_ID,
                "models/block/telescope/telescope_keyframes.atom"));

        GibsAnimationRegistry.registerAnimation("rudder",
            new ResourceLocation(ValkyrienSkiesControl.MOD_ID,
                "models/block/rudder/rudder_animation.atom"));

        GibsAnimationRegistry.registerAnimation("rotation_axle",
            new ResourceLocation(ValkyrienSkiesControl.MOD_ID,
                "models/block/rotation_axle/rotation_axle.atom"));

        GibsAnimationRegistry.registerAnimation("giant_propeller",
            new ResourceLocation(ValkyrienSkiesControl.MOD_ID,
                "models/block/giant_propeller/giant_propeller.atom"));
    }

    private void registerGearboxGibs(String name) {
        GibsModelRegistry.registerGibsModel(name,
            new ResourceLocation(ValkyrienSkiesControl.MOD_ID, "block/gearbox/" + name + ".obj"));
    }

    private void registerControlGibs(String name) {
        GibsModelRegistry.registerGibsModel(name,
            new ResourceLocation(ValkyrienSkiesControl.MOD_ID, "block/controls/" + name + ".obj"));
    }

    private void registerRudderGibs(String name) {
        GibsModelRegistry.registerGibsModel(name, new ResourceLocation(ValkyrienSkiesControl.MOD_ID,
            "block/rudder/" + name + ".obj"));
    }

    @Override
    public void init(FMLStateEvent event) {
        super.init(event);
    }

    @Override
    public void postInit(FMLStateEvent event) {
        super.postInit(event);
        registerBlockItemModels();
        registerItemModels();
        registerTileEntityRenderers();
    }

}
