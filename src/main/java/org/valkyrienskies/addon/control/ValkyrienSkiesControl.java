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

package org.valkyrienskies.addon.control;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.valkyrienskies.addon.control.block.multiblocks.EthereumCompressorMultiblockSchematic;
import org.valkyrienskies.addon.control.block.multiblocks.EthereumEngineMultiblockSchematic;
import org.valkyrienskies.addon.control.block.multiblocks.GiantPropellerMultiblockSchematic;
import org.valkyrienskies.addon.control.block.multiblocks.RudderAxleMultiblockSchematic;
import org.valkyrienskies.addon.control.block.multiblocks.TileEntityEthereumCompressorPart;
import org.valkyrienskies.addon.control.block.multiblocks.TileEntityEthereumEnginePart;
import org.valkyrienskies.addon.control.block.multiblocks.TileEntityGiantPropellerPart;
import org.valkyrienskies.addon.control.block.multiblocks.TileEntityRudderAxlePart;
import org.valkyrienskies.addon.control.block.torque.TileEntityRotationTrainAxle;
import org.valkyrienskies.addon.control.capability.ICapabilityLastRelay;
import org.valkyrienskies.addon.control.capability.ImplCapabilityLastRelay;
import org.valkyrienskies.addon.control.capability.StorageLastRelay;
import org.valkyrienskies.addon.control.item.ItemRelayWire;
import org.valkyrienskies.addon.control.item.ItemWrench;
import org.valkyrienskies.addon.control.network.MessagePlayerStoppedPiloting;
import org.valkyrienskies.addon.control.network.MessagePlayerStoppedPilotingHandler;
import org.valkyrienskies.addon.control.network.MessageStartPiloting;
import org.valkyrienskies.addon.control.network.MessageStartPilotingHandler;
import org.valkyrienskies.addon.control.network.MessageStopPiloting;
import org.valkyrienskies.addon.control.network.MessageStopPilotingHandler;
import org.valkyrienskies.addon.control.piloting.PilotControlsMessage;
import org.valkyrienskies.addon.control.piloting.PilotControlsMessageHandler;
import org.valkyrienskies.addon.control.proxy.CommonProxyControl;
import org.valkyrienskies.addon.control.tileentity.TileEntityGearbox;
import org.valkyrienskies.addon.control.tileentity.TileEntityGyroscopeDampener;
import org.valkyrienskies.addon.control.tileentity.TileEntityGyroscopeStabilizer;
import org.valkyrienskies.addon.control.tileentity.TileEntityLiftControl;
import org.valkyrienskies.addon.control.tileentity.TileEntityLiftValve;
import org.valkyrienskies.addon.control.tileentity.TileEntityNetworkDisplay;
import org.valkyrienskies.addon.control.tileentity.TileEntityNodeRelay;
import org.valkyrienskies.addon.control.tileentity.TileEntityPassengerChair;
import org.valkyrienskies.addon.control.tileentity.TileEntityPilotsChair;
import org.valkyrienskies.addon.control.tileentity.TileEntityPropellerEngine;
import org.valkyrienskies.addon.control.tileentity.TileEntityShipHelm;
import org.valkyrienskies.addon.control.tileentity.TileEntityShipTelegraph;
import org.valkyrienskies.addon.world.ValkyrienWarfareWorld;
import org.valkyrienskies.api.addons.Module;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;

@Mod(
        name = ValkyrienSkiesControl.MOD_NAME,
        modid = ValkyrienSkiesControl.MOD_ID,
        version = ValkyrienSkiesControl.MOD_VERSION,
        dependencies = "required-after:" + ValkyrienWarfareWorld.MOD_ID
)
@Mod.EventBusSubscriber(modid = ValkyrienSkiesControl.MOD_ID)
public class ValkyrienSkiesControl {

    public static final String MOD_ID = "vs_control";
    public static final String MOD_NAME = "Valkyrien Skies Control";
    public static final String MOD_VERSION = ValkyrienSkiesMod.MOD_VERSION;
    @SidedProxy(clientSide = "org.valkyrienskies.addon.control.proxy.ClientProxyControl", serverSide = "org.valkyrienskies.addon.control.proxy.CommonProxyControl")
    private static CommonProxyControl proxy;

    @Instance(MOD_ID)
    public static ValkyrienSkiesControl INSTANCE;

    @CapabilityInject(ICapabilityLastRelay.class)
    public static final Capability<ICapabilityLastRelay> lastRelayCapability = null;

    public static SimpleNetworkWrapper controlNetwork;
    public final BlocksValkyrienSkiesControl vwControlBlocks = new BlocksValkyrienSkiesControl();
    public Item relayWire;
    public Item multiBlockWrench;

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        INSTANCE.vwControlBlocks.registerBlocks(event);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        INSTANCE.relayWire = new ItemRelayWire().setTranslationKey("relaywire")
                .setRegistryName(MOD_ID, "relaywire")
                .setCreativeTab(ValkyrienSkiesMod.vwTab);
        INSTANCE.multiBlockWrench = new ItemWrench().setTranslationKey("vw_wrench")
                .setRegistryName(MOD_ID, "vw_wrench")
                .setCreativeTab(ValkyrienSkiesMod.vwTab);

        event.getRegistry()
                .register(INSTANCE.relayWire);
        event.getRegistry()
                .register(INSTANCE.multiBlockWrench);

        INSTANCE.vwControlBlocks.registerBlockItems(event);
        // This doesn't really belong here, but whatever.
        MultiblockRegistry.registerAllPossibleSchematicVariants(EthereumEngineMultiblockSchematic.class);
        MultiblockRegistry.registerAllPossibleSchematicVariants(EthereumCompressorMultiblockSchematic.class);
        MultiblockRegistry.registerAllPossibleSchematicVariants(RudderAxleMultiblockSchematic.class);
        MultiblockRegistry.registerAllPossibleSchematicVariants(GiantPropellerMultiblockSchematic.class);
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        Module.registerRecipe(event, "recipe_pilots_chair",
                new ItemStack(INSTANCE.vwControlBlocks.pilotsChair),
                "SLS",
                "EWE",
                " S ",
                'S', Items.STICK,
                'L', Items.LEATHER,
                'W', Item.getItemFromBlock(Blocks.LOG),
                'E', ValkyrienWarfareWorld.INSTANCE.ethereumCrystal);

        Module.registerRecipe(event, "recipe_basic_engine",
                new ItemStack(INSTANCE.vwControlBlocks.basicEngine, 4),
                "I##",
                "IPP",
                "I##",
                '#', Item.getItemFromBlock(Blocks.PLANKS),
                'P', Item.getItemFromBlock(Blocks.PISTON),
                'I', Items.IRON_INGOT);

        Module.registerRecipe(event, "recipe_advanced_engine1", new ItemStack(INSTANCE.vwControlBlocks.advancedEngine, 4), "I##", "IPP", "I##", '#', Item.getItemFromBlock(Blocks.STONE), 'P', Item.getItemFromBlock(Blocks.PISTON), 'I', Items.IRON_INGOT);
        Module.registerRecipe(event, "recipe_advanced_engine2", new ItemStack(INSTANCE.vwControlBlocks.advancedEngine, 2), "I##", "IPP", "I##", '#', Item.getItemFromBlock(Blocks.COBBLESTONE), 'P', Item.getItemFromBlock(Blocks.PISTON), 'I', Items.IRON_INGOT);
        Module.registerRecipe(event, "recipe_elite_engine", new ItemStack(INSTANCE.vwControlBlocks.eliteEngine, 4), "III", "IPP", "III", 'P', Item.getItemFromBlock(Blocks.PISTON), 'I', Items.IRON_INGOT);
        Module.registerRecipe(event, "recipe_ultimate_engine", new ItemStack(INSTANCE.vwControlBlocks.ultimateEngine, 4), "I##", "IPP", "I##", '#', Item.getItemFromBlock(Blocks.OBSIDIAN), 'P', Item.getItemFromBlock(Blocks.PISTON), 'I', Items.IRON_INGOT);
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        registerTileEntities();
        registerNetworks();
        registerCapabilities();
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    private void registerTileEntities() {
        GameRegistry.registerTileEntity(TileEntityPilotsChair.class, new ResourceLocation(MOD_ID, "tilemanualshipcontroller"));
        GameRegistry.registerTileEntity(TileEntityNodeRelay.class, new ResourceLocation(MOD_ID, "tilethrustrelay"));
        GameRegistry.registerTileEntity(TileEntityShipHelm.class, new ResourceLocation(MOD_ID, "tileshiphelm"));
        GameRegistry.registerTileEntity(TileEntityShipTelegraph.class, new ResourceLocation(MOD_ID, "tileshiptelegraph"));
        GameRegistry.registerTileEntity(TileEntityPropellerEngine.class, new ResourceLocation(MOD_ID, "tilepropellerengine"));
        GameRegistry.registerTileEntity(TileEntityGyroscopeStabilizer.class, new ResourceLocation(MOD_ID, "tilegyroscope_stabilizer"));
        GameRegistry.registerTileEntity(TileEntityLiftValve.class, new ResourceLocation(MOD_ID, "tileliftvalve"));
        GameRegistry.registerTileEntity(TileEntityNetworkDisplay.class, new ResourceLocation(MOD_ID, "tilenetworkdisplay"));
        GameRegistry.registerTileEntity(TileEntityLiftControl.class, new ResourceLocation(MOD_ID, "tileliftcontrol"));

        GameRegistry.registerTileEntity(TileEntityGyroscopeDampener.class, new ResourceLocation(MOD_ID, "tilegyroscope_dampener"));
        GameRegistry.registerTileEntity(TileEntityEthereumEnginePart.class, new ResourceLocation(MOD_ID, "tile_big_engine_part"));
        GameRegistry.registerTileEntity(TileEntityGearbox.class, new ResourceLocation(MOD_ID, "tile_gearbox"));
        GameRegistry.registerTileEntity(TileEntityEthereumCompressorPart.class, new ResourceLocation(MOD_ID, "tile_ethereum_compressor_part"));
        GameRegistry.registerTileEntity(TileEntityRudderAxlePart.class, new ResourceLocation(MOD_ID, "tile_rudder_axle_part"));
        GameRegistry.registerTileEntity(TileEntityGiantPropellerPart.class, new ResourceLocation(MOD_ID, "tile_giant_propeller_part"));
        GameRegistry.registerTileEntity(TileEntityRotationTrainAxle.class, new ResourceLocation(MOD_ID, "tile_rotation_train_axle"));

        GameRegistry.registerTileEntity(TileEntityPassengerChair.class, new ResourceLocation(MOD_ID, "tile_passengers_chair"));
    }

    private void registerNetworks() {
        controlNetwork = NetworkRegistry.INSTANCE.newSimpleChannel("controlnetwork");
        controlNetwork.registerMessage(PilotControlsMessageHandler.class, PilotControlsMessage.class, 2, Side.SERVER);
        controlNetwork.registerMessage(MessageStartPilotingHandler.class, MessageStartPiloting.class, 3, Side.CLIENT);
        controlNetwork.registerMessage(MessageStopPilotingHandler.class, MessageStopPiloting.class, 4, Side.CLIENT);
        controlNetwork.registerMessage(MessagePlayerStoppedPilotingHandler.class, MessagePlayerStoppedPiloting.class, 5, Side.SERVER);
    }

    private void registerCapabilities() {
        CapabilityManager.INSTANCE.register(ICapabilityLastRelay.class, new StorageLastRelay(),
                ImplCapabilityLastRelay::new);
    }
}
