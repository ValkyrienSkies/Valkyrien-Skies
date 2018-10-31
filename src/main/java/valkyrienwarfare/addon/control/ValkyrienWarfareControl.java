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

package valkyrienwarfare.addon.control;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.addon.control.block.multiblocks.EthereumCompressorMultiblockSchematic;
import valkyrienwarfare.addon.control.block.multiblocks.EthereumEngineMultiblockSchematic;
import valkyrienwarfare.addon.control.block.multiblocks.TileEntityEthereumCompressorPart;
import valkyrienwarfare.addon.control.block.multiblocks.TileEntityEthereumEnginePart;
import valkyrienwarfare.addon.control.block.multiblocks.TileEntityRudderAxelPart;
import valkyrienwarfare.addon.control.capability.ICapabilityLastRelay;
import valkyrienwarfare.addon.control.capability.ImplCapabilityLastRelay;
import valkyrienwarfare.addon.control.capability.StorageLastRelay;
import valkyrienwarfare.addon.control.gui.ControlGUIHandler;
import valkyrienwarfare.addon.control.item.ItemRelayWire;
import valkyrienwarfare.addon.control.item.ItemWrench;
import valkyrienwarfare.addon.control.network.EntityFixHandler;
import valkyrienwarfare.addon.control.network.EntityFixMessage;
import valkyrienwarfare.addon.control.network.MessagePlayerStoppedPiloting;
import valkyrienwarfare.addon.control.network.MessagePlayerStoppedPilotingHandler;
import valkyrienwarfare.addon.control.network.MessageStartPiloting;
import valkyrienwarfare.addon.control.network.MessageStartPilotingHandler;
import valkyrienwarfare.addon.control.network.MessageStopPiloting;
import valkyrienwarfare.addon.control.network.MessageStopPilotingHandler;
import valkyrienwarfare.addon.control.network.ThrustModulatorGuiInputHandler;
import valkyrienwarfare.addon.control.network.ThrustModulatorGuiInputMessage;
import valkyrienwarfare.addon.control.piloting.PilotControlsMessage;
import valkyrienwarfare.addon.control.piloting.PilotControlsMessageHandler;
import valkyrienwarfare.addon.control.proxy.ClientProxyControl;
import valkyrienwarfare.addon.control.proxy.CommonProxyControl;
import valkyrienwarfare.addon.control.tileentity.TileEntityGearbox;
import valkyrienwarfare.addon.control.tileentity.TileEntityGyroscopeDampener;
import valkyrienwarfare.addon.control.tileentity.TileEntityGyroscopeStabilizer;
import valkyrienwarfare.addon.control.tileentity.TileEntityLegacyEtherCompressor;
import valkyrienwarfare.addon.control.tileentity.TileEntityLiftControl;
import valkyrienwarfare.addon.control.tileentity.TileEntityLiftValve;
import valkyrienwarfare.addon.control.tileentity.TileEntityNetworkDisplay;
import valkyrienwarfare.addon.control.tileentity.TileEntityNodeRelay;
import valkyrienwarfare.addon.control.tileentity.TileEntityPilotsChair;
import valkyrienwarfare.addon.control.tileentity.TileEntityPropellerEngine;
import valkyrienwarfare.addon.control.tileentity.TileEntityShipHelm;
import valkyrienwarfare.addon.control.tileentity.TileEntityShipTelegraph;
import valkyrienwarfare.addon.control.tileentity.TileEntityThrustModulator;
import valkyrienwarfare.addon.world.ValkyrienWarfareWorld;
import valkyrienwarfare.api.addons.Module;
import valkyrienwarfare.api.addons.VWAddon;

@VWAddon
public class ValkyrienWarfareControl extends Module {

    @CapabilityInject(ICapabilityLastRelay.class)
    public static final Capability<ICapabilityLastRelay> lastRelayCapability = null;
    public static ValkyrienWarfareControl INSTANCE;
    public static SimpleNetworkWrapper controlNetwork;
    public final BlocksValkyrienWarfareControl vwControlBlocks;
    public Item relayWire;
    public Item multiblockWrench;

    public ValkyrienWarfareControl() {
        super("VW_Control", new CommonProxyControl(), "valkyrienwarfarecontrol");
        if (ValkyrienWarfareMod.INSTANCE.isRunningOnClient()) {
            this.setClientProxy(new ClientProxyControl());
        }
        vwControlBlocks = new BlocksValkyrienWarfareControl(this);
        INSTANCE = this;
    }

    @Override
    protected void preInit(FMLStateEvent event) {

    }

    @Override
    protected void init(FMLStateEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(ValkyrienWarfareMod.INSTANCE, new ControlGUIHandler());
    }

    @Override
    protected void postInit(FMLStateEvent event) {

    }

    @Override
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        vwControlBlocks.registerBlocks(event);
    }

    @Override
    protected void registerTileEntities() {
        GameRegistry.registerTileEntity(TileEntityLegacyEtherCompressor.class, "tileantigravengine");
        GameRegistry.registerTileEntity(TileEntityPilotsChair.class, "tilemanualshipcontroller");
        GameRegistry.registerTileEntity(TileEntityNodeRelay.class, "tilethrustrelay");
        GameRegistry.registerTileEntity(TileEntityThrustModulator.class, "tilethrustmodulator");
        GameRegistry.registerTileEntity(TileEntityShipHelm.class, "tileshiphelm");
        GameRegistry.registerTileEntity(TileEntityShipTelegraph.class, "tileshiptelegraph");
        GameRegistry.registerTileEntity(TileEntityPropellerEngine.class, "tilepropellerengine");
        GameRegistry.registerTileEntity(TileEntityGyroscopeStabilizer.class, "tilegyroscope_stabilizer");
        GameRegistry.registerTileEntity(TileEntityLiftValve.class, "tileliftvalve");
        GameRegistry.registerTileEntity(TileEntityNetworkDisplay.class, "tilenetworkdisplay");
        GameRegistry.registerTileEntity(TileEntityLiftControl.class, "tileliftcontrol");

        GameRegistry.registerTileEntity(TileEntityGyroscopeDampener.class, "tilegyroscope_dampener");
        GameRegistry.registerTileEntity(TileEntityEthereumEnginePart.class, "tile_big_engine_part");
        GameRegistry.registerTileEntity(TileEntityGearbox.class, "tile_gearbox");
        GameRegistry.registerTileEntity(TileEntityEthereumCompressorPart.class, "tile_ethereum_compressor_part");
        GameRegistry.registerTileEntity(TileEntityRudderAxelPart.class, "tile_rudder_axel_part");
    }

    @Override
    public void registerItems(RegistryEvent.Register<Item> event) {
    	relayWire = new ItemRelayWire().setUnlocalizedName("relaywire").setRegistryName(getModID(), "relaywire").setCreativeTab(ValkyrienWarfareMod.vwTab);
    	multiblockWrench = new ItemWrench().setUnlocalizedName("vw_wrench").setRegistryName(getModID(), "vw_wrench").setCreativeTab(ValkyrienWarfareMod.vwTab);
    	
    	event.getRegistry().register(relayWire);
    	event.getRegistry().register(multiblockWrench);

        vwControlBlocks.registerBlockItems(event);
        // This doesn't really belong here, but whatever.
        MultiblockRegistry.registerSchematic(1, new EthereumEngineMultiblockSchematic());
        MultiblockRegistry.registerSchematic(2, new EthereumCompressorMultiblockSchematic());
    }

    @Override
    public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        registerRecipe(event, "recipe_pilots_chair", new ItemStack(vwControlBlocks.pilotsChair), "SLS", "EWE", " S ", 'S', Items.STICK, 'L', Items.LEATHER, 'W', Item.getItemFromBlock(Blocks.LOG), 'E', ValkyrienWarfareWorld.INSTANCE.etheriumCrystal);

        registerRecipe(event, "recipe_antigrav_engine", new ItemStack(vwControlBlocks.antigravityEngine, 4), "#I#", "#E#", "WEW", '#', Item.getItemFromBlock(Blocks.PLANKS), 'I', Items.IRON_INGOT, 'E', ValkyrienWarfareWorld.INSTANCE.etheriumCrystal, 'W', Item.getItemFromBlock(Blocks.LOG));
        registerRecipe(event, "recipe_advanced_ether1", new ItemStack(vwControlBlocks.advancedEtherCompressor, 4), "#I#", "#E#", "WEW", '#', Item.getItemFromBlock(Blocks.STONE), 'I', Items.IRON_INGOT, 'E', ValkyrienWarfareWorld.INSTANCE.etheriumCrystal, 'W', Item.getItemFromBlock(Blocks.LOG));
        registerRecipe(event, "recipe_advanced_ether2", new ItemStack(vwControlBlocks.advancedEtherCompressor, 2), "#I#", "#E#", "WEW", '#', Item.getItemFromBlock(Blocks.COBBLESTONE), 'I', Items.IRON_INGOT, 'E', ValkyrienWarfareWorld.INSTANCE.etheriumCrystal, 'W', Item.getItemFromBlock(Blocks.LOG));
        registerRecipe(event, "recipe_elite_ether", new ItemStack(vwControlBlocks.eliteEtherCompressor, 4), "III", "IEI", "WEW", 'I', Items.IRON_INGOT, 'E', ValkyrienWarfareWorld.INSTANCE.etheriumCrystal, 'W', Item.getItemFromBlock(Blocks.LOG));
        registerRecipe(event, "recipe_ultimate_ether", new ItemStack(vwControlBlocks.ultimateEtherCompressor, 4), "#I#", "#E#", "WEW", '#', Item.getItemFromBlock(Blocks.OBSIDIAN), 'I', Items.IRON_INGOT, 'E', ValkyrienWarfareWorld.INSTANCE.etheriumCrystal, 'W', Item.getItemFromBlock(Blocks.LOG));

        registerRecipe(event, "recipe_basic_engine", new ItemStack(vwControlBlocks.basicEngine, 4), "I##", "IPP", "I##", '#', Item.getItemFromBlock(Blocks.PLANKS), 'P', Item.getItemFromBlock(Blocks.PISTON), 'I', Items.IRON_INGOT);
        registerRecipe(event, "recipe_advanced_engine1", new ItemStack(vwControlBlocks.advancedEngine, 4), "I##", "IPP", "I##", '#', Item.getItemFromBlock(Blocks.STONE), 'P', Item.getItemFromBlock(Blocks.PISTON), 'I', Items.IRON_INGOT);
        registerRecipe(event, "recipe_advanced_engine2", new ItemStack(vwControlBlocks.advancedEngine, 2), "I##", "IPP", "I##", '#', Item.getItemFromBlock(Blocks.COBBLESTONE), 'P', Item.getItemFromBlock(Blocks.PISTON), 'I', Items.IRON_INGOT);
        registerRecipe(event, "recipe_elite_engine", new ItemStack(vwControlBlocks.eliteEngine, 4), "III", "IPP", "III", 'P', Item.getItemFromBlock(Blocks.PISTON), 'I', Items.IRON_INGOT);
        registerRecipe(event, "recipe_ultimate_engine", new ItemStack(vwControlBlocks.ultimateEngine, 4), "I##", "IPP", "I##", '#', Item.getItemFromBlock(Blocks.OBSIDIAN), 'P', Item.getItemFromBlock(Blocks.PISTON), 'I', Items.IRON_INGOT);
    }

    @Override
    protected void registerNetworks() {
        controlNetwork = NetworkRegistry.INSTANCE.newSimpleChannel("controlnetwork");
        controlNetwork.registerMessage(EntityFixHandler.class, EntityFixMessage.class, 0, Side.CLIENT);
        controlNetwork.registerMessage(PilotControlsMessageHandler.class, PilotControlsMessage.class, 2, Side.SERVER);
        controlNetwork.registerMessage(MessageStartPilotingHandler.class, MessageStartPiloting.class, 3, Side.CLIENT);
        controlNetwork.registerMessage(MessageStopPilotingHandler.class, MessageStopPiloting.class, 4, Side.CLIENT);
        controlNetwork.registerMessage(MessagePlayerStoppedPilotingHandler.class, MessagePlayerStoppedPiloting.class, 5, Side.SERVER);
        controlNetwork.registerMessage(ThrustModulatorGuiInputHandler.class, ThrustModulatorGuiInputMessage.class, 6, Side.SERVER);
    }

    @Override
    protected void registerCapabilities() {
        CapabilityManager.INSTANCE.register(ICapabilityLastRelay.class, new StorageLastRelay(), ImplCapabilityLastRelay.class);
    }

    @Override
    public void applyConfig(Configuration config) {
        config.addCustomCategoryComment("control", "Settings for Valkyrien Warfare's control module");
        double basicEnginePower = config.get("control.power.engine", "basicEnginePower", 4000D, "engine power for the basic engine").getDouble();
        double advancedEnginePower = config.get("control.power.engine", "advancedEnginePower", 6000D, "engine power for the advanced engine").getDouble();
        double eliteEnginePower = config.get("control.power.engine", "eliteEnginePower", 8000D, "engine power for the elite engine").getDouble();
        double ultimateEnginePower = config.get("control.power.engine", "ultimateEnginePower", 16000D, "engine power for the ultimate engine").getDouble();
        double redstoneEnginePower = config.get("control.power.engine", "redstoneEnginePower", 500D, "Multiplied by the redstone power (0-15) to the Redstone engine").getDouble();

        double basicEtherCompressorPower = config.get("control.power.compressor", "basicEtherCompressorPower", 25000D, "engine power for the basic Ether Compressor").getDouble();
        double advancedEtherCompressorPower = config.get("control.power.compressor", "advancedEtherCompressorPower", 45000D, "engine power for the advanced Ether Compressor").getDouble();
        double eliteEtherCompressorPower = config.get("control.power.compressor", "eliteEtherCompressorPower", 80000D, "engine power for the elite Ether Compressor").getDouble();
        double ultimateEtherCompressorPower = config.get("control.power.compressor", "ultimateEtherCompressorPower", 100000D, "engine power for the ultimate Ether Compressor").getDouble();

        vwControlBlocks.basicEngine.setEnginePower(basicEnginePower);
        vwControlBlocks.advancedEngine.setEnginePower(advancedEnginePower);
        vwControlBlocks.eliteEngine.setEnginePower(eliteEnginePower);
        vwControlBlocks.ultimateEngine.setEnginePower(ultimateEnginePower);
        vwControlBlocks.redstoneEngine.setEnginePower(redstoneEnginePower);

        vwControlBlocks.antigravityEngine.setEnginePower(basicEtherCompressorPower);
        vwControlBlocks.advancedEtherCompressor.setEnginePower(advancedEtherCompressorPower);
        vwControlBlocks.eliteEtherCompressor.setEnginePower(eliteEtherCompressorPower);
        vwControlBlocks.ultimateEtherCompressor.setEnginePower(ultimateEtherCompressorPower);
    }
}
