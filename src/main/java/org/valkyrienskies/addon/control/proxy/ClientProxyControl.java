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
import org.valkyrienskies.addon.control.renderer.*;
import org.valkyrienskies.addon.control.tileentity.*;
import org.valkyrienskies.addon.control.renderer.GibsAtomAnimationRegistry;
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

        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vsControlBlocks.physicsInfuser);
        registerBlockItem(ValkyrienSkiesControl.INSTANCE.vsControlBlocks.physicsInfuserCreative);
    }

    private static void registerItemModels() {
        registerItemModel(ValkyrienSkiesControl.INSTANCE.relayWire);
        registerItemModel(ValkyrienSkiesControl.INSTANCE.vanishingWire);
        registerItemModel(ValkyrienSkiesControl.INSTANCE.vsWrench);
        // Disabled because we have a custom item render model for physicsCore
        // registerItemModel(ValkyrienSkiesControl.INSTANCE.physicsCore);
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
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPhysicsInfuser.class,
                new TileEntityPhysicsInfuserRenderer());
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

        GibsAtomAnimationRegistry.registerAnimation("valkyrium_compressor",
            new ResourceLocation(ValkyrienSkiesControl.MOD_ID,
                "models/block/valkyrium_compressor/compressor_animations.atom"));

        GibsAtomAnimationRegistry.registerAnimation("valkyrium_engine",
            new ResourceLocation(ValkyrienSkiesControl.MOD_ID,
                "models/block/valkyrium_engine/valkyrium_engine.atom"));

        GibsAtomAnimationRegistry.registerAnimation("lift_lever",
            new ResourceLocation(ValkyrienSkiesControl.MOD_ID,
                "models/block/controls/lift_lever_keyframes.atom"));

        GibsAtomAnimationRegistry.registerAnimation("gearbox",
            new ResourceLocation(ValkyrienSkiesControl.MOD_ID,
                "models/block/gearbox/gearbox.atom"));

        GibsAtomAnimationRegistry.registerAnimation("pocketwatch_body",
            new ResourceLocation(ValkyrienSkiesControl.MOD_ID,
                "models/block/pocketwatch/pocketwatch_keyframes.atom"));

        GibsAtomAnimationRegistry.registerAnimation("pocketwatch_lid",
            new ResourceLocation(ValkyrienSkiesControl.MOD_ID,
                "models/block/pocketwatch/pocketwatch_lid_keyframes.atom"));

        GibsAtomAnimationRegistry.registerAnimation("telescope",
            new ResourceLocation(ValkyrienSkiesControl.MOD_ID,
                "models/block/telescope/telescope_keyframes.atom"));

        GibsAtomAnimationRegistry.registerAnimation("rudder",
            new ResourceLocation(ValkyrienSkiesControl.MOD_ID,
                "models/block/rudder/rudder_animation.atom"));

        GibsAtomAnimationRegistry.registerAnimation("rotation_axle",
            new ResourceLocation(ValkyrienSkiesControl.MOD_ID,
                "models/block/rotation_axle/rotation_axle.atom"));

        GibsAtomAnimationRegistry.registerAnimation("giant_propeller",
            new ResourceLocation(ValkyrienSkiesControl.MOD_ID,
                "models/block/giant_propeller/giant_propeller.atom"));

        GibsAtomAnimationRegistry.registerAnimation("physics_infuser",
                new ResourceLocation(ValkyrienSkiesControl.MOD_ID,
                        "models/block/physics_infuser/physics_infuser.atom"));

        GibsAtomAnimationRegistry.registerAnimation("physics_infuser_empty",
                new ResourceLocation(ValkyrienSkiesControl.MOD_ID,
                        "models/block/physics_infuser/physics_infuser_empty.atom"));
        // Not an actual animation, just easier to put in than writing out all the core names.
        GibsAtomAnimationRegistry.registerAnimation("physics_infuser_cores",
                new ResourceLocation(ValkyrienSkiesControl.MOD_ID,
                        "models/block/physics_infuser/cores.atom"));
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
        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
        renderItem.getItemModelMesher().register(ValkyrienSkiesControl.INSTANCE.physicsCore, 0,
                new ModelResourceLocation(ValkyrienSkiesControl.MOD_ID + ":testmodel", "inventory"));
    }

    @Override
    public void postInit(FMLStateEvent event) {
        super.postInit(event);
        registerBlockItemModels();
        registerItemModels();
        registerTileEntityRenderers();
    }

}
