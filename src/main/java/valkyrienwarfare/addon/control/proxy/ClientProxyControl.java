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

package valkyrienwarfare.addon.control.proxy;

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
import valkyrienwarfare.addon.control.ControlEventsClient;
import valkyrienwarfare.addon.control.ValkyrienWarfareControl;
import valkyrienwarfare.addon.control.block.multiblocks.TileEntityEthereumCompressorPart;
import valkyrienwarfare.addon.control.block.multiblocks.TileEntityEthereumEnginePart;
import valkyrienwarfare.addon.control.block.multiblocks.TileEntityGiantPropellerPart;
import valkyrienwarfare.addon.control.block.multiblocks.TileEntityRudderAxlePart;
import valkyrienwarfare.addon.control.block.torque.TileEntityRotationTrainAxle;
import valkyrienwarfare.addon.control.controlsystems.controlgui.ThrustModulatorGui;
import valkyrienwarfare.addon.control.renderer.BasicNodeTileEntityRenderer;
import valkyrienwarfare.addon.control.renderer.EthereumCompressorPartTileEntityRenderer;
import valkyrienwarfare.addon.control.renderer.EthereumEnginePartTileEntityRenderer;
import valkyrienwarfare.addon.control.renderer.GearboxTileEntityRenderer;
import valkyrienwarfare.addon.control.renderer.GiantPropellerPartTileEntityRenderer;
import valkyrienwarfare.addon.control.renderer.LiftControlTileEntityRenderer;
import valkyrienwarfare.addon.control.renderer.PropellerEngineTileEntityRenderer;
import valkyrienwarfare.addon.control.renderer.RotationTrainAxleTileEntityRenderer;
import valkyrienwarfare.addon.control.renderer.RudderAxlePartTileEntityRenderer;
import valkyrienwarfare.addon.control.renderer.ShipHelmTileEntityRenderer;
import valkyrienwarfare.addon.control.renderer.ShipTelegraphTileEntityRenderer;
import valkyrienwarfare.addon.control.tileentity.TileEntityGearbox;
import valkyrienwarfare.addon.control.tileentity.TileEntityLiftControl;
import valkyrienwarfare.addon.control.tileentity.TileEntityNodeRelay;
import valkyrienwarfare.addon.control.tileentity.TileEntityPropellerEngine;
import valkyrienwarfare.addon.control.tileentity.TileEntityShipHelm;
import valkyrienwarfare.addon.control.tileentity.TileEntityShipTelegraph;
import valkyrienwarfare.addon.control.tileentity.TileEntityThrustModulator;
import valkyrienwarfare.mod.client.render.GibsAnimationRegistry;
import valkyrienwarfare.mod.client.render.GibsModelRegistry;

public class ClientProxyControl extends CommonProxyControl {

    private static void registerBlockItem(Block toRegister) {
        Item item = Item.getItemFromBlock(toRegister);
        Minecraft.getMinecraft()
                .getRenderItem()
                .getItemModelMesher()
                .register(item, 0, new ModelResourceLocation(ValkyrienWarfareControl.INSTANCE.getModID() + ":" + item.getTranslationKey()
                        .substring(5), "inventory"));
    }

    private static void registerItemModel(Item toRegister) {
        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
        renderItem.getItemModelMesher()
                .register(toRegister, 0, new ModelResourceLocation(ValkyrienWarfareControl.INSTANCE.getModID() + ":" + toRegister.getTranslationKey()
                        .substring(5), "inventory"));
    }

    private static void registerBlockItemModels() {
        registerBlockItem(ValkyrienWarfareControl.INSTANCE.vwControlBlocks.basicEngine);
        registerBlockItem(ValkyrienWarfareControl.INSTANCE.vwControlBlocks.advancedEngine);
        registerBlockItem(ValkyrienWarfareControl.INSTANCE.vwControlBlocks.eliteEngine);
        registerBlockItem(ValkyrienWarfareControl.INSTANCE.vwControlBlocks.ultimateEngine);
        registerBlockItem(ValkyrienWarfareControl.INSTANCE.vwControlBlocks.redstoneEngine);

        registerBlockItem(ValkyrienWarfareControl.INSTANCE.vwControlBlocks.passengerChair);
        registerBlockItem(ValkyrienWarfareControl.INSTANCE.vwControlBlocks.shipHelm);

        registerBlockItem(ValkyrienWarfareControl.INSTANCE.vwControlBlocks.shipTelegraph);
        registerBlockItem(ValkyrienWarfareControl.INSTANCE.vwControlBlocks.dopedEthereum);
        registerBlockItem(ValkyrienWarfareControl.INSTANCE.vwControlBlocks.thrustRelay);
        registerBlockItem(ValkyrienWarfareControl.INSTANCE.vwControlBlocks.thrustModulator);

        registerBlockItem(ValkyrienWarfareControl.INSTANCE.vwControlBlocks.gyroscopeStabilizer);
        registerBlockItem(ValkyrienWarfareControl.INSTANCE.vwControlBlocks.liftValve);
        registerBlockItem(ValkyrienWarfareControl.INSTANCE.vwControlBlocks.networkDisplay);
        registerBlockItem(ValkyrienWarfareControl.INSTANCE.vwControlBlocks.liftControl);

        registerBlockItem(ValkyrienWarfareControl.INSTANCE.vwControlBlocks.etherCompressorPanel);
        registerBlockItem(ValkyrienWarfareControl.INSTANCE.vwControlBlocks.gyroscopeDampener);

        registerBlockItem(ValkyrienWarfareControl.INSTANCE.vwControlBlocks.ethereumEnginePart);
        registerBlockItem(ValkyrienWarfareControl.INSTANCE.vwControlBlocks.gearbox);
        registerBlockItem(ValkyrienWarfareControl.INSTANCE.vwControlBlocks.rudderAxelPart);

        registerBlockItem(ValkyrienWarfareControl.INSTANCE.vwControlBlocks.giantPropellerPart);
        registerBlockItem(ValkyrienWarfareControl.INSTANCE.vwControlBlocks.rotationTrainAxle);
    }

    private static void registerItemModels() {
        registerItemModel(ValkyrienWarfareControl.INSTANCE.relayWire);
        registerItemModel(ValkyrienWarfareControl.INSTANCE.multiblockWrench);
    }

    private static void registerTileEntityRenderers() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityNodeRelay.class, new BasicNodeTileEntityRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityShipHelm.class, new ShipHelmTileEntityRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityShipTelegraph.class, new ShipTelegraphTileEntityRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPropellerEngine.class, new PropellerEngineTileEntityRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEthereumEnginePart.class, new EthereumEnginePartTileEntityRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGearbox.class, new GearboxTileEntityRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLiftControl.class, new LiftControlTileEntityRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEthereumCompressorPart.class, new EthereumCompressorPartTileEntityRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRudderAxlePart.class, new RudderAxlePartTileEntityRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGiantPropellerPart.class, new GiantPropellerPartTileEntityRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRotationTrainAxle.class, new RotationTrainAxleTileEntityRenderer());
    }

    public static void checkForTextFieldUpdate(TileEntityThrustModulator entity) {
        if (Minecraft.getMinecraft().currentScreen instanceof ThrustModulatorGui) {
            ThrustModulatorGui gui = (ThrustModulatorGui) Minecraft.getMinecraft().currentScreen;
            gui.updateTextFields();
        }
    }

    @Override
    public void preInit(FMLStateEvent event) {
        // Register events
        MinecraftForge.EVENT_BUS.register(new ControlEventsClient());
        // Register gibs
        OBJLoader.INSTANCE.addDomain(ValkyrienWarfareControl.INSTANCE.getModID().toLowerCase());

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

        GibsAnimationRegistry.registerAnimation("ethereum_compressor", new ResourceLocation("valkyrienwarfarecontrol", "models/block/ether_compressor/compressoranimations.atom"));

        GibsAnimationRegistry.registerAnimation("ethereum_engine", new ResourceLocation("valkyrienwarfarecontrol", "models/block/multipart_engines/small_engine.atom"));

        GibsAnimationRegistry.registerAnimation("lift_control", new ResourceLocation("valkyrienwarfarecontrol", "models/block/controls/liftcontrol_keyframes.atom"));

        GibsAnimationRegistry.registerAnimation("gearbox", new ResourceLocation("valkyrienwarfarecontrol", "models/block/gearbox/small_gearbox.atom"));

        GibsAnimationRegistry.registerAnimation("pocketwatch_body", new ResourceLocation("valkyrienwarfarecontrol", "models/block/pocketwatch/pocketwatch_keyframes.atom"));

        GibsAnimationRegistry.registerAnimation("pocketwatch_lid", new ResourceLocation("valkyrienwarfarecontrol", "models/block/pocketwatch/pocketwatchlid_keyframes.atom"));

        GibsAnimationRegistry.registerAnimation("telescope", new ResourceLocation("valkyrienwarfarecontrol", "models/block/telescope/telescope_keyframes.atom"));

        GibsAnimationRegistry.registerAnimation("steering_rudder", new ResourceLocation("valkyrienwarfarecontrol", "models/block/steering_rudder/rudder_animation.atom"));

        GibsAnimationRegistry.registerAnimation("rotation_train_axle", new ResourceLocation("valkyrienwarfarecontrol", "models/block/rotation_train_axle/small_engine_axle.atom"));

        GibsAnimationRegistry.registerAnimation("giant_propeller", new ResourceLocation("valkyrienwarfarecontrol", "models/block/giant_propeller/small_propeller.atom"));

        GibsAnimationRegistry.registerAnimation("physics_infuser", new ResourceLocation("valkyrienwarfarecontrol", "models/block/basic_phys_infuser/physics_infuser.atom"));

        GibsAnimationRegistry.registerAnimation("physics_infuser_empty", new ResourceLocation("valkyrienwarfarecontrol", "models/block/basic_phys_infuser/physics_infuser_empty.atom"));
        // Not an actual animation, just easier to put in than writing out all the core names.
        GibsAnimationRegistry.registerAnimation("physics_infuser_cores", new ResourceLocation("valkyrienwarfarecontrol", "models/block/basic_phys_infuser/cores.atom"));
    }

    private void registerGearboxGibs(String name) {
        GibsModelRegistry.registerGibsModel(name, new ResourceLocation("valkyrienwarfarecontrol", "block/gearbox/" + name + ".obj"));
    }

    private void registerControlGibs(String name) {
        GibsModelRegistry.registerGibsModel(name, new ResourceLocation("valkyrienwarfarecontrol", "block/controls/" + name + ".obj"));
    }

    private void registerRudderGibs(String name) {
        GibsModelRegistry.registerGibsModel(name, new ResourceLocation("valkyrienwarfarecontrol", "block/steering_rudder/" + name + ".obj"));
    }

    @Override
    public void init(FMLStateEvent event) {
    }

    @Override
    public void postInit(FMLStateEvent event) {
        registerBlockItemModels();
        registerItemModels();
        registerTileEntityRenderers();
    }

}
