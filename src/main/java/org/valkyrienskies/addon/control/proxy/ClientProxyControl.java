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
import org.valkyrienskies.addon.control.block.multiblocks.TileEntityEthereumCompressorPart;
import org.valkyrienskies.addon.control.block.multiblocks.TileEntityEthereumEnginePart;
import org.valkyrienskies.addon.control.block.multiblocks.TileEntityGiantPropellerPart;
import org.valkyrienskies.addon.control.block.multiblocks.TileEntityRudderAxlePart;
import org.valkyrienskies.addon.control.block.torque.TileEntityRotationTrainAxle;
import org.valkyrienskies.addon.control.renderer.BasicNodeTileEntityRenderer;
import org.valkyrienskies.addon.control.renderer.EthereumCompressorPartTileEntityRenderer;
import org.valkyrienskies.addon.control.renderer.EthereumEnginePartTileEntityRenderer;
import org.valkyrienskies.addon.control.renderer.GearboxTileEntityRenderer;
import org.valkyrienskies.addon.control.renderer.GiantPropellerPartTileEntityRenderer;
import org.valkyrienskies.addon.control.renderer.LiftControlTileEntityRenderer;
import org.valkyrienskies.addon.control.renderer.PropellerEngineTileEntityRenderer;
import org.valkyrienskies.addon.control.renderer.RotationTrainAxleTileEntityRenderer;
import org.valkyrienskies.addon.control.renderer.RudderAxlePartTileEntityRenderer;
import org.valkyrienskies.addon.control.renderer.ShipHelmTileEntityRenderer;
import org.valkyrienskies.addon.control.renderer.ShipTelegraphTileEntityRenderer;
import org.valkyrienskies.addon.control.tileentity.TileEntityGearbox;
import org.valkyrienskies.addon.control.tileentity.TileEntityLiftControl;
import org.valkyrienskies.addon.control.tileentity.TileEntityNodeRelay;
import org.valkyrienskies.addon.control.tileentity.TileEntityPropellerEngine;
import org.valkyrienskies.addon.control.tileentity.TileEntityShipHelm;
import org.valkyrienskies.addon.control.tileentity.TileEntityShipTelegraph;
import org.valkyrienskies.mod.client.render.GibsAnimationRegistry;
import org.valkyrienskies.mod.client.render.GibsModelRegistry;

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
        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vwControlBlocks.basicEngine);
        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vwControlBlocks.advancedEngine);
        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vwControlBlocks.eliteEngine);
        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vwControlBlocks.ultimateEngine);
        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vwControlBlocks.redstoneEngine);

        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vwControlBlocks.pilotsChair);
        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vwControlBlocks.passengerChair);
        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vwControlBlocks.shipHelm);

        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vwControlBlocks.shipTelegraph);
        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vwControlBlocks.dopedEthereum);
        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vwControlBlocks.thrustRelay);

        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vwControlBlocks.gyroscopeStabilizer);
        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vwControlBlocks.liftValve);
        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vwControlBlocks.networkDisplay);
        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vwControlBlocks.liftControl);

        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vwControlBlocks.etherCompressorPanel);
        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vwControlBlocks.gyroscopeDampener);

        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vwControlBlocks.ethereumEnginePart);
        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vwControlBlocks.gearbox);
        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vwControlBlocks.rudderAxelPart);

        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vwControlBlocks.giantPropellerPart);
        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vwControlBlocks.rotationTrainAxle);
    }

    private static void registerItemModels() {
        registerItemModel(ValkyrienSkiesControl.INSTANCE.relayWire);
        registerItemModel(ValkyrienSkiesControl.INSTANCE.multiBlockWrench);
    }

    private static void registerTileEntityRenderers() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityNodeRelay.class,
            new BasicNodeTileEntityRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityShipHelm.class,
            new ShipHelmTileEntityRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityShipTelegraph.class,
            new ShipTelegraphTileEntityRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPropellerEngine.class,
            new PropellerEngineTileEntityRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEthereumEnginePart.class,
            new EthereumEnginePartTileEntityRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGearbox.class,
            new GearboxTileEntityRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLiftControl.class,
            new LiftControlTileEntityRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEthereumCompressorPart.class,
            new EthereumCompressorPartTileEntityRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRudderAxlePart.class,
            new RudderAxlePartTileEntityRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGiantPropellerPart.class,
            new GiantPropellerPartTileEntityRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRotationTrainAxle.class,
            new RotationTrainAxleTileEntityRenderer());
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
        registerControlGibs("chadburn_speedtelegraph_simplevoxel_geo");

        registerControlGibs("shiphelmbase");
        registerControlGibs("shiphelmdial");
        registerControlGibs("shiphelmdialglass");
        registerControlGibs("shiphelmwheel");

        registerRudderGibs("rudder_geo");
        registerRudderGibs("rudder_axel_geo");

        registerGearboxGibs("gearboxbackengineaxel_geo");
        registerGearboxGibs("gearboxbottomengineaxel_geo");
        registerGearboxGibs("gearboxfrontengineaxel_geo");
        registerGearboxGibs("gearboxleftengineaxel_geo");
        registerGearboxGibs("gearboxrightengineaxel_geo");
        registerGearboxGibs("gearboxvtopengineaxel_geo");

        GibsAnimationRegistry.registerAnimation("ethereum_compressor",
            new ResourceLocation(ValkyrienSkiesControl.MOD_ID,
                "models/block/ether_compressor/compressoranimations.atom"));

        GibsAnimationRegistry.registerAnimation("ethereum_engine",
            new ResourceLocation(ValkyrienSkiesControl.MOD_ID,
                "models/block/multipart_engines/small_engine.atom"));

        GibsAnimationRegistry.registerAnimation("lift_control",
            new ResourceLocation(ValkyrienSkiesControl.MOD_ID,
                "models/block/controls/liftcontrol_keyframes.atom"));

        GibsAnimationRegistry.registerAnimation("gearbox",
            new ResourceLocation(ValkyrienSkiesControl.MOD_ID,
                "models/block/gearbox/small_gearbox.atom"));

        GibsAnimationRegistry.registerAnimation("pocketwatch_body",
            new ResourceLocation(ValkyrienSkiesControl.MOD_ID,
                "models/block/pocketwatch/pocketwatch_keyframes.atom"));

        GibsAnimationRegistry.registerAnimation("pocketwatch_lid",
            new ResourceLocation(ValkyrienSkiesControl.MOD_ID,
                "models/block/pocketwatch/pocketwatchlid_keyframes.atom"));

        GibsAnimationRegistry.registerAnimation("telescope",
            new ResourceLocation(ValkyrienSkiesControl.MOD_ID,
                "models/block/telescope/telescope_keyframes.atom"));

        GibsAnimationRegistry.registerAnimation("steering_rudder",
            new ResourceLocation(ValkyrienSkiesControl.MOD_ID,
                "models/block/steering_rudder/rudder_animation.atom"));

        GibsAnimationRegistry.registerAnimation("rotation_train_axle",
            new ResourceLocation(ValkyrienSkiesControl.MOD_ID,
                "models/block/rotation_train_axle/small_engine_axle.atom"));

        GibsAnimationRegistry.registerAnimation("giant_propeller",
            new ResourceLocation(ValkyrienSkiesControl.MOD_ID,
                "models/block/giant_propeller/small_propeller.atom"));
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
            "block/steering_rudder/" + name + ".obj"));
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
