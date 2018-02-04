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

import java.io.File;

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
import valkyrienwarfare.addon.control.capability.ICapabilityLastRelay;
import valkyrienwarfare.addon.control.capability.ImplCapabilityLastRelay;
import valkyrienwarfare.addon.control.capability.StorageLastRelay;
import valkyrienwarfare.addon.control.gui.ControlGUIHandler;
import valkyrienwarfare.addon.control.item.ItemRelayWire;
import valkyrienwarfare.addon.control.item.ItemShipStealer;
import valkyrienwarfare.addon.control.item.ItemSystemLinker;
import valkyrienwarfare.addon.control.network.EntityFixMessage;
import valkyrienwarfare.addon.control.network.EntityFixMessageHandler;
import valkyrienwarfare.addon.control.network.HovercraftControllerGUIInputHandler;
import valkyrienwarfare.addon.control.network.HovercraftControllerGUIInputMessage;
import valkyrienwarfare.addon.control.network.MessagePlayerStoppedPiloting;
import valkyrienwarfare.addon.control.network.MessagePlayerStoppedPilotingHandler;
import valkyrienwarfare.addon.control.network.MessageStartPiloting;
import valkyrienwarfare.addon.control.network.MessageStartPilotingHandler;
import valkyrienwarfare.addon.control.network.MessageStopPiloting;
import valkyrienwarfare.addon.control.network.MessageStopPilotingHandler;
import valkyrienwarfare.addon.control.network.ThrustModulatorGuiInputMessage;
import valkyrienwarfare.addon.control.network.ThrustModulatorGuiInputMessageHandler;
import valkyrienwarfare.addon.control.piloting.PilotControlsMessage;
import valkyrienwarfare.addon.control.piloting.PilotControlsMessageHandler;
import valkyrienwarfare.addon.control.proxy.ClientProxyControl;
import valkyrienwarfare.addon.control.proxy.CommonProxyControl;
import valkyrienwarfare.addon.control.tileentity.BalloonBurnerTileEntity;
import valkyrienwarfare.addon.control.tileentity.ThrustModulatorTileEntity;
import valkyrienwarfare.addon.control.tileentity.ThrustRelayTileEntity;
import valkyrienwarfare.addon.control.tileentity.TileEntityGyroscope;
import valkyrienwarfare.addon.control.tileentity.TileEntityHoverController;
import valkyrienwarfare.addon.control.tileentity.TileEntityHullSealer;
import valkyrienwarfare.addon.control.tileentity.TileEntityNormalEtherCompressor;
import valkyrienwarfare.addon.control.tileentity.TileEntityPilotsChair;
import valkyrienwarfare.addon.control.tileentity.TileEntityPropellerEngine;
import valkyrienwarfare.addon.control.tileentity.TileEntityShipHelm;
import valkyrienwarfare.addon.control.tileentity.TileEntityShipTelegraph;
import valkyrienwarfare.addon.control.tileentity.TileEntityZepplinController;
import valkyrienwarfare.addon.world.ValkyrienWarfareWorld;
import valkyrienwarfare.api.addons.Module;
import valkyrienwarfare.api.addons.VWAddon;

@VWAddon
public class ValkyrienWarfareControl extends Module<ValkyrienWarfareControl> {

    @CapabilityInject(ICapabilityLastRelay.class)
    public static final Capability<ICapabilityLastRelay> lastRelayCapability = null;
    public static Configuration config;
    public static ValkyrienWarfareControl INSTANCE;
    public static SimpleNetworkWrapper controlNetwork;
    public final BlocksValkyrienWarfareControl blocks;
    public Item systemLinker;
    public Item airshipStealer;
    public Item relayWire;
    
    public ValkyrienWarfareControl() {
        super("VW_Control", new CommonProxyControl(), "valkyrienwarfarecontrol");
        if (ValkyrienWarfareMod.INSTANCE.isRunningOnClient()) {
            this.setClientProxy(new ClientProxyControl());
        }
        blocks = new BlocksValkyrienWarfareControl(this);
        INSTANCE = this;
    }

    @Override
    protected void setupConfig() {
        config = new Configuration(new File(ValkyrienWarfareMod.getWorkingFolder() + "/config/valkyrienwarfarecontrol.cfg"));
        config.load();
        config.save();
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
        blocks.registerBlocks(event);
    }

    @Override
    protected void registerTileEntities() {
        GameRegistry.registerTileEntity(TileEntityHoverController.class, "tilehovercontroller");
        GameRegistry.registerTileEntity(TileEntityNormalEtherCompressor.class, "tileantigravengine");
        GameRegistry.registerTileEntity(BalloonBurnerTileEntity.class, "tileballoonburner");
        GameRegistry.registerTileEntity(TileEntityPilotsChair.class, "tilemanualshipcontroller");
        GameRegistry.registerTileEntity(ThrustRelayTileEntity.class, "tilethrustrelay");
        GameRegistry.registerTileEntity(ThrustModulatorTileEntity.class, "tilethrustmodulator");
        GameRegistry.registerTileEntity(TileEntityShipHelm.class, "tileshiphelm");
        GameRegistry.registerTileEntity(TileEntityShipTelegraph.class, "tileshiptelegraph");
        GameRegistry.registerTileEntity(TileEntityPropellerEngine.class, "tilepropellerengine");
        GameRegistry.registerTileEntity(TileEntityZepplinController.class, "tilezepplin_controller");
        GameRegistry.registerTileEntity(TileEntityHullSealer.class, "tileentityshiphullsealer");
        GameRegistry.registerTileEntity(TileEntityGyroscope.class, "tileentitygyroscope");
    }

    @Override
    public void registerItems(RegistryEvent.Register<Item> event) {
        systemLinker = new ItemSystemLinker().setUnlocalizedName("systemlinker").setRegistryName(getModID(), "systemlinker").setCreativeTab(ValkyrienWarfareMod.vwTab).setMaxStackSize(1);
        airshipStealer = new ItemShipStealer().setUnlocalizedName("airshipStealer").setRegistryName(getModID(), "airshipStealer").setCreativeTab(ValkyrienWarfareMod.vwTab).setMaxStackSize(1);
        relayWire = new ItemRelayWire().setUnlocalizedName("relaywire").setRegistryName(getModID(), "relaywire").setCreativeTab(ValkyrienWarfareMod.vwTab).setMaxStackSize(1);

        event.getRegistry().register(systemLinker);
        event.getRegistry().register(airshipStealer);
        event.getRegistry().register(relayWire);

        blocks.registerBlockItems(event);
    }

    @Override
    public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        registerRecipe(event, new ItemStack(blocks.pilotsChair), "SLS", "EWE", " S ", 'S', Items.STICK, 'L', Items.LEATHER, 'W', Item.getItemFromBlock(Blocks.LOG), 'E', ValkyrienWarfareWorld.INSTANCE.etheriumCrystal);
        registerRecipe(event, new ItemStack(systemLinker), "IR ", " DR", "I I", 'I', Items.IRON_INGOT, 'D', Items.DIAMOND, 'R', Items.REDSTONE);
        registerRecipe(event, new ItemStack(blocks.balloonBurner), "IFI", "WIW", "PPP", 'I', Items.IRON_INGOT, 'F', Items.FLINT_AND_STEEL, 'W', Item.getItemFromBlock(Blocks.LOG), 'P', Item.getItemFromBlock(Blocks.PLANKS));
        registerRecipe(event, new ItemStack(blocks.basicHoverController), "III", "TCT", "III", 'I', Item.getItemFromBlock(Blocks.IRON_BLOCK), 'C', Items.COMPASS, 'T', Item.getItemFromBlock(Blocks.CRAFTING_TABLE));

        registerRecipe(event, new ItemStack(blocks.antigravityEngine, 4), "#I#", "#E#", "WEW", '#', Item.getItemFromBlock(Blocks.PLANKS), 'I', Items.IRON_INGOT, 'E', ValkyrienWarfareWorld.INSTANCE.etheriumCrystal, 'W', Item.getItemFromBlock(Blocks.LOG));
        registerRecipe(event, new ItemStack(blocks.advancedEtherCompressor, 4), "#I#", "#E#", "WEW", '#', Item.getItemFromBlock(Blocks.STONE), 'I', Items.IRON_INGOT, 'E', ValkyrienWarfareWorld.INSTANCE.etheriumCrystal, 'W', Item.getItemFromBlock(Blocks.LOG));
        registerRecipe(event, new ItemStack(blocks.advancedEtherCompressor, 2), "#I#", "#E#", "WEW", '#', Item.getItemFromBlock(Blocks.COBBLESTONE), 'I', Items.IRON_INGOT, 'E', ValkyrienWarfareWorld.INSTANCE.etheriumCrystal, 'W', Item.getItemFromBlock(Blocks.LOG));
        registerRecipe(event, new ItemStack(blocks.eliteEtherCompressor, 4), "III", "IEI", "WEW", 'I', Items.IRON_INGOT, 'E', ValkyrienWarfareWorld.INSTANCE.etheriumCrystal, 'W', Item.getItemFromBlock(Blocks.LOG));
        registerRecipe(event, new ItemStack(blocks.ultimateEtherCompressor, 4), "#I#", "#E#", "WEW", '#', Item.getItemFromBlock(Blocks.OBSIDIAN), 'I', Items.IRON_INGOT, 'E', ValkyrienWarfareWorld.INSTANCE.etheriumCrystal, 'W', Item.getItemFromBlock(Blocks.LOG));

        registerRecipe(event, new ItemStack(blocks.basicEngine, 4), "I##", "IPP", "I##", '#', Item.getItemFromBlock(Blocks.PLANKS), 'P', Item.getItemFromBlock(Blocks.PISTON), 'I', Items.IRON_INGOT);
        registerRecipe(event, new ItemStack(blocks.advancedEngine, 4), "I##", "IPP", "I##", '#', Item.getItemFromBlock(Blocks.STONE), 'P', Item.getItemFromBlock(Blocks.PISTON), 'I', Items.IRON_INGOT);
        registerRecipe(event, new ItemStack(blocks.advancedEngine, 2), "I##", "IPP", "I##", '#', Item.getItemFromBlock(Blocks.COBBLESTONE), 'P', Item.getItemFromBlock(Blocks.PISTON), 'I', Items.IRON_INGOT);
        registerRecipe(event, new ItemStack(blocks.eliteEngine, 4), "III", "IPP", "III", 'P', Item.getItemFromBlock(Blocks.PISTON), 'I', Items.IRON_INGOT);
        registerRecipe(event, new ItemStack(blocks.ultimateEngine, 4), "I##", "IPP", "I##", '#', Item.getItemFromBlock(Blocks.OBSIDIAN), 'P', Item.getItemFromBlock(Blocks.PISTON), 'I', Items.IRON_INGOT);
    }

    @Override
    protected void registerNetworks() {
        controlNetwork = NetworkRegistry.INSTANCE.newSimpleChannel("controlnetwork");
        controlNetwork.registerMessage(EntityFixMessageHandler.class, EntityFixMessage.class, 0, Side.CLIENT);
        controlNetwork.registerMessage(HovercraftControllerGUIInputHandler.class, HovercraftControllerGUIInputMessage.class, 1, Side.SERVER);
        controlNetwork.registerMessage(PilotControlsMessageHandler.class, PilotControlsMessage.class, 2, Side.SERVER);
        controlNetwork.registerMessage(MessageStartPilotingHandler.class, MessageStartPiloting.class, 3, Side.CLIENT);
        controlNetwork.registerMessage(MessageStopPilotingHandler.class, MessageStopPiloting.class, 4, Side.CLIENT);
        controlNetwork.registerMessage(MessagePlayerStoppedPilotingHandler.class, MessagePlayerStoppedPiloting.class, 5, Side.SERVER);
        controlNetwork.registerMessage(ThrustModulatorGuiInputMessageHandler.class, ThrustModulatorGuiInputMessage.class, 6, Side.SERVER);
    }

    @Override
    protected void registerCapabilities() {
        CapabilityManager.INSTANCE.register(ICapabilityLastRelay.class, new StorageLastRelay(), ImplCapabilityLastRelay.class);
    }

}
