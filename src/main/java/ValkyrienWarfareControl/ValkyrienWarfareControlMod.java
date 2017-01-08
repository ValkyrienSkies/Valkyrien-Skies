package ValkyrienWarfareControl;

import java.io.File;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareControl.Block.BlockAntiGravEngine;
import ValkyrienWarfareControl.Block.BlockBalloonBurner;
import ValkyrienWarfareControl.Block.BlockDopedEtherium;
import ValkyrienWarfareControl.Block.BlockHovercraftController;
import ValkyrienWarfareControl.Block.BlockNormalEngine;
import ValkyrienWarfareControl.Block.BlockRedstoneEngine;
import ValkyrienWarfareControl.FullBalloonControl.BlockShipPilotsChair;
import ValkyrienWarfareControl.FullBalloonControl.PilotsChairTileEntity;
import ValkyrienWarfareControl.GUI.ControlGUIHandler;
import ValkyrienWarfareControl.Item.ItemSystemLinker;
import ValkyrienWarfareControl.Network.EntityFixMessage;
import ValkyrienWarfareControl.Network.EntityFixMessageHandler;
import ValkyrienWarfareControl.Network.HovercraftControllerGUIInputHandler;
import ValkyrienWarfareControl.Network.HovercraftControllerGUIInputMessage;
import ValkyrienWarfareControl.Piloting.PilotControlsMessage;
import ValkyrienWarfareControl.Piloting.PilotControlsMessageHandler;
import ValkyrienWarfareControl.Piloting.SetShipPilotMessage;
import ValkyrienWarfareControl.Piloting.SetShipPilotMessageHandler;
import ValkyrienWarfareControl.Proxy.CommonProxyControl;
import ValkyrienWarfareControl.TileEntity.AntiGravEngineTileEntity;
import ValkyrienWarfareControl.TileEntity.BalloonBurnerTileEntity;
import ValkyrienWarfareControl.TileEntity.TileEntityHoverController;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = ValkyrienWarfareControlMod.MODID, name = ValkyrienWarfareControlMod.MODNAME, version = ValkyrienWarfareControlMod.MODVER)
public class ValkyrienWarfareControlMod {

	public static Configuration config;

	public static final String MODID = "valkyrienwarfarecontrol";
	public static final String MODNAME = "Valkyrien Warfare Control";
	public static final String MODVER = "0.3b";

	public static ValkyrienWarfareControlMod instance;

	public static SimpleNetworkWrapper controlNetwork;

	public Block basicEngine;
	public Block advancedEngine;
	public Block eliteEngine;
	public Block ultimateEngine; // Couldn't think of what to name these, so I went with the Mekanism naming style
	public Block redstoneEngine;

	public Block basicHoverController;
	public Block antigravityEngine;
	public Block dopedEtherium;
	public Block balloonBurner;
	public Block pilotsChair;

	public Item systemLinker;

	@SidedProxy(clientSide = "ValkyrienWarfareControl.Proxy.ClientProxyControl", serverSide = "ValkyrienWarfareControl.Proxy.CommonProxyControl")
	public static CommonProxyControl proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		instance = this;
		config = new Configuration(new File(ValkyrienWarfareMod.getWorkingFolder() + "/config/valkyrienwarfarecontrol.cfg"));
		config.load();
		registerBlocks(event);
		registerTileEntities(event);
		registerItems(event);
		registerRecipies(event);
		registerNetworks(event);
		proxy.preInit(event);
		config.save();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.init(event);
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new ControlGUIHandler());
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		proxy.postInit(event);
	}

	private void registerBlocks(FMLStateEvent event) {
		double basicEnginePower = config.get(Configuration.CATEGORY_GENERAL, "basicEnginePower", 4000D, "Engine power for the basic Engine").getDouble();
		double advancedEnginePower = config.get(Configuration.CATEGORY_GENERAL, "advancedEnginePower", 6000D, "Engine power for the advanced Engine").getDouble();
		double eliteEnginePower = config.get(Configuration.CATEGORY_GENERAL, "eliteEnginePower", 8000D, "Engine power for the elite Engine").getDouble();
		double ultimateEnginePower = config.get(Configuration.CATEGORY_GENERAL, "ultimateEnginePower", 16000D, "Engine power for the ultimate Engine").getDouble();
		double redstoneEnginePower = config.get(Configuration.CATEGORY_GENERAL, "redstoneEnginePower", 500D, "Multiplied by the redstone power (0-15) to the Redstone Engine").getDouble();

		basicEngine = new BlockNormalEngine(Material.WOOD, basicEnginePower).setHardness(5f).setUnlocalizedName("basicEngine").setRegistryName(ValkyrienWarfareMod.MODID, "basicEngine").setCreativeTab(CreativeTabs.TRANSPORTATION);
		advancedEngine = new BlockNormalEngine(Material.WOOD, advancedEnginePower).setHardness(6f).setUnlocalizedName("advancedEngine").setRegistryName(ValkyrienWarfareMod.MODID, "advancedEngine").setCreativeTab(CreativeTabs.TRANSPORTATION);
		eliteEngine = new BlockNormalEngine(Material.WOOD, eliteEnginePower).setHardness(8f).setUnlocalizedName("eliteEngine").setRegistryName(ValkyrienWarfareMod.MODID, "eliteEngine").setCreativeTab(CreativeTabs.TRANSPORTATION);
		ultimateEngine = new BlockNormalEngine(Material.WOOD, ultimateEnginePower).setHardness(10f).setUnlocalizedName("ultimateEngine").setRegistryName(ValkyrienWarfareMod.MODID, "ultimateEngine").setCreativeTab(CreativeTabs.TRANSPORTATION);
		redstoneEngine = new BlockRedstoneEngine(Material.REDSTONE_LIGHT, redstoneEnginePower).setHardness(7.0f).setUnlocalizedName("redstoneEngine").setRegistryName(ValkyrienWarfareMod.MODID, "redstoneEngine").setCreativeTab(CreativeTabs.TRANSPORTATION);

		basicHoverController = new BlockHovercraftController(Material.IRON).setHardness(10f).setUnlocalizedName("basichovercraftcontroller").setRegistryName(ValkyrienWarfareMod.MODID, "basichovercraftcontroller").setCreativeTab(CreativeTabs.TRANSPORTATION);
		antigravityEngine = new BlockAntiGravEngine(Material.IRON).setHardness(8f).setUnlocalizedName("antigravengine").setUnlocalizedName("antigravengine").setRegistryName(ValkyrienWarfareMod.MODID, "antigravengine").setCreativeTab(CreativeTabs.TRANSPORTATION);
		dopedEtherium = new BlockDopedEtherium(Material.GLASS).setHardness(4f).setUnlocalizedName("dopedetherium").setRegistryName(MODID, "dopedetherium").setCreativeTab(CreativeTabs.TRANSPORTATION);
		balloonBurner = new BlockBalloonBurner(Material.IRON).setHardness(4f).setUnlocalizedName("ballonburner").setRegistryName(MODID, "ballonburner").setCreativeTab(CreativeTabs.TRANSPORTATION);
		pilotsChair = new BlockShipPilotsChair(Material.IRON).setHardness(4f).setUnlocalizedName("shippilotschair").setRegistryName(MODID, "shippilotschair").setCreativeTab(CreativeTabs.TRANSPORTATION);

		GameRegistry.registerBlock(basicEngine);
		GameRegistry.registerBlock(advancedEngine);
		GameRegistry.registerBlock(eliteEngine);
		GameRegistry.registerBlock(ultimateEngine);
		GameRegistry.registerBlock(redstoneEngine);

		GameRegistry.registerBlock(basicHoverController);
		GameRegistry.registerBlock(antigravityEngine);
		GameRegistry.registerBlock(dopedEtherium);
		GameRegistry.registerBlock(balloonBurner);
		GameRegistry.registerBlock(pilotsChair);
	}

	private void registerTileEntities(FMLStateEvent event) {
		TileEntity.addMapping(TileEntityHoverController.class, "tilehovercontroller");
		TileEntity.addMapping(AntiGravEngineTileEntity.class, "tileantigravengine");
		TileEntity.addMapping(BalloonBurnerTileEntity.class, "tileballoonburner");
		TileEntity.addMapping(PilotsChairTileEntity.class, "tilemanualshipcontroller");
	}

	private void registerItems(FMLStateEvent event) {
		systemLinker = new ItemSystemLinker().setUnlocalizedName("systemlinker").setRegistryName(MODID, "systemlinker").setCreativeTab(CreativeTabs.TRANSPORTATION).setMaxStackSize(1);
		GameRegistry.registerItem(systemLinker);
	}

	private void registerRecipies(FMLStateEvent event) {
		GameRegistry.addRecipe(new ItemStack(basicEngine), new Object[] { "IWW", "IPP", "IWW", 'W', Item.getItemFromBlock(Blocks.PLANKS), 'P', Item.getItemFromBlock(Blocks.PISTON), 'I', Items.IRON_INGOT });
		GameRegistry.addRecipe(new ItemStack(basicHoverController), new Object[] { "III", "TCT", "III", 'I', Item.getItemFromBlock(Blocks.IRON_BLOCK), 'C', Items.COMPASS, 'T', Item.getItemFromBlock(Blocks.CRAFTING_TABLE) });
		GameRegistry.addRecipe(new ItemStack(antigravityEngine, 3), new Object[] { "IWI", "GDG", "IWI", 'W', Item.getItemFromBlock(Blocks.PLANKS), 'G', Item.getItemFromBlock(Blocks.GOLD_BLOCK), 'I', Item.getItemFromBlock(Blocks.IRON_BLOCK), 'D', Item.getItemFromBlock(Blocks.DIAMOND_BLOCK) });

		GameRegistry.addRecipe(new ItemStack(systemLinker), new Object[] { "IR ", " DR", "I I", 'I', Items.IRON_INGOT, 'D', Items.DIAMOND, 'R', Items.REDSTONE });
	}

	private void registerNetworks(FMLStateEvent event) {
		controlNetwork = NetworkRegistry.INSTANCE.newSimpleChannel("controlNetwork");
		controlNetwork.registerMessage(HovercraftControllerGUIInputHandler.class, HovercraftControllerGUIInputMessage.class, 0, Side.SERVER);
		controlNetwork.registerMessage(PilotControlsMessageHandler.class, PilotControlsMessage.class, 1, Side.SERVER);
		controlNetwork.registerMessage(EntityFixMessageHandler.class, EntityFixMessage.class, 2, Side.CLIENT);
		controlNetwork.registerMessage(SetShipPilotMessageHandler.class, SetShipPilotMessage.class, 3, Side.CLIENT);
	}
}
