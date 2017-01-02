package ValkyrienWarfareControl;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareControl.Block.BlockAirShipEngine;
import ValkyrienWarfareControl.Block.BlockAntiGravEngine;
import ValkyrienWarfareControl.Block.BlockBalloonBurner;
import ValkyrienWarfareControl.Block.BlockDopedEtherium;
import ValkyrienWarfareControl.Block.BlockHovercraftController;
import ValkyrienWarfareControl.FullBalloonControl.ManualShipControllerBlock;
import ValkyrienWarfareControl.FullBalloonControl.ManualShipControllerTileEntity;
import ValkyrienWarfareControl.GUI.ControlGUIHandler;
import ValkyrienWarfareControl.Item.ItemSystemLinker;
import ValkyrienWarfareControl.Network.EntityFixMessage;
import ValkyrienWarfareControl.Network.EntityFixMessageHandler;
import ValkyrienWarfareControl.Network.HovercraftControllerGUIInputHandler;
import ValkyrienWarfareControl.Network.HovercraftControllerGUIInputMessage;
import ValkyrienWarfareControl.Network.PilotControlsMessage;
import ValkyrienWarfareControl.Network.PilotControlsMessageHandler;
import ValkyrienWarfareControl.TileEntity.AntiGravEngineTileEntity;
import ValkyrienWarfareControl.TileEntity.BalloonBurnerTileEntity;
import ValkyrienWarfareControl.TileEntity.TileEntityHoverController;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
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

@Mod(modid=ValkyrienWarfareControlMod.MODID, name=ValkyrienWarfareControlMod.MODNAME, version=ValkyrienWarfareControlMod.MODVER)
public class ValkyrienWarfareControlMod {
	
	public static final String MODID = "valkyrienwarfarecontrol";
    public static final String MODNAME = "Valkyrien Warfare Control";
    public static final String MODVER = "0.3b";
    
    public static ValkyrienWarfareControlMod instance;
    
    public static SimpleNetworkWrapper controlNetwork;
    
    public Block basicEngine;
    public Block basicHoverController;
    public Block antigravityEngine;
    public Block dopedEtherium;
    public Block balloonBurner;
    public Block manualController;
    
    public Item systemLinker;
    
    @SidedProxy(clientSide="ValkyrienWarfareControl.ClientProxyControl", serverSide="ValkyrienWarfareControl.CommonProxyControl")
	public static CommonProxyControl proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event){
    	instance = this;
    	registerBlocks(event);
    	registerTileEntities(event);
    	registerItems(event);
    	registerRecipies(event);
    	registerNetworks(event);
    	proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event){
    	proxy.init(event);
    	NetworkRegistry.INSTANCE.registerGuiHandler(this, new ControlGUIHandler());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event){
    	proxy.postInit(event);
    }
    
    private void registerBlocks(FMLStateEvent event){
    	basicEngine = new BlockAirShipEngine(Material.WOOD).setHardness(5f).setUnlocalizedName("basicengine").setRegistryName(ValkyrienWarfareMod.MODID, "basicengine").setCreativeTab(CreativeTabs.TRANSPORTATION);
    	basicHoverController = new BlockHovercraftController(Material.IRON).setHardness(10f).setUnlocalizedName("basichovercraftcontroller").setRegistryName(ValkyrienWarfareMod.MODID, "basichovercraftcontroller").setCreativeTab(CreativeTabs.TRANSPORTATION);
    	antigravityEngine = new BlockAntiGravEngine(Material.IRON).setHardness(8f).setUnlocalizedName("antigravengine").setUnlocalizedName("antigravengine").setRegistryName(ValkyrienWarfareMod.MODID, "antigravengine").setCreativeTab(CreativeTabs.TRANSPORTATION);
    	dopedEtherium = new BlockDopedEtherium(Material.GLASS).setHardness(4f).setUnlocalizedName("dopedetherium").setRegistryName(MODID, "dopedetherium").setCreativeTab(CreativeTabs.TRANSPORTATION);
    	balloonBurner = new BlockBalloonBurner(Material.IRON).setHardness(4f).setUnlocalizedName("ballonburner").setRegistryName(MODID, "ballonburner").setCreativeTab(CreativeTabs.TRANSPORTATION);
    	manualController = new ManualShipControllerBlock(Material.IRON).setHardness(4f).setUnlocalizedName("manualshipcontroller").setRegistryName(MODID, "manualshipcontroller").setCreativeTab(CreativeTabs.TRANSPORTATION);
    	
    	GameRegistry.registerBlock(basicEngine);
    	GameRegistry.registerBlock(basicHoverController);
    	GameRegistry.registerBlock(antigravityEngine);
    	GameRegistry.registerBlock(dopedEtherium);
    	GameRegistry.registerBlock(balloonBurner);
    	GameRegistry.registerBlock(manualController);
    }
    
    private void registerTileEntities(FMLStateEvent event){
    	TileEntity.addMapping(TileEntityHoverController.class, "tilehovercontroller");
    	TileEntity.addMapping(AntiGravEngineTileEntity.class, "tileantigravengine");
    	TileEntity.addMapping(BalloonBurnerTileEntity.class, "tileballoonburner");
    	TileEntity.addMapping(ManualShipControllerTileEntity.class,"tilemanualshipcontroller");
    }

    private void registerItems(FMLStateEvent event){
    	systemLinker = new ItemSystemLinker().setUnlocalizedName("systemlinker").setRegistryName(MODID, "systemlinker").setCreativeTab(CreativeTabs.TRANSPORTATION).setMaxStackSize(1);
    	GameRegistry.registerItem(systemLinker);
    }
    
    private void registerRecipies(FMLStateEvent event){
    	GameRegistry.addRecipe(new ItemStack(basicEngine), new Object[] {"IWW", "IPP","IWW",'W',Item.getItemFromBlock(Blocks.PLANKS), 'P', Item.getItemFromBlock(Blocks.PISTON),'I',Items.IRON_INGOT});
    	GameRegistry.addRecipe(new ItemStack(basicHoverController), new Object[] {"III", "TCT","III",'I',Item.getItemFromBlock(Blocks.IRON_BLOCK),'C',Items.COMPASS,'T',Item.getItemFromBlock(Blocks.CRAFTING_TABLE)});
    	GameRegistry.addRecipe(new ItemStack(antigravityEngine,3), new Object[] {"IWI", "GDG","IWI",'W',Item.getItemFromBlock(Blocks.PLANKS), 'G', Item.getItemFromBlock(Blocks.GOLD_BLOCK),'I',Item.getItemFromBlock(Blocks.IRON_BLOCK),'D',Item.getItemFromBlock(Blocks.DIAMOND_BLOCK)});
    	
    	GameRegistry.addRecipe(new ItemStack(systemLinker), new Object[] {"IR ", " DR","I I",'I',Items.IRON_INGOT,'D',Items.DIAMOND,'R',Items.REDSTONE});
    }
    
    private void registerNetworks(FMLStateEvent event){
    	controlNetwork = NetworkRegistry.INSTANCE.newSimpleChannel("controlNetwork");
    	controlNetwork.registerMessage(HovercraftControllerGUIInputHandler.class, HovercraftControllerGUIInputMessage.class, 0, Side.SERVER);
    	controlNetwork.registerMessage(PilotControlsMessageHandler.class, PilotControlsMessage.class, 1, Side.SERVER);
    	controlNetwork.registerMessage(EntityFixMessageHandler.class, EntityFixMessage.class, 2, Side.CLIENT);
    }
    
}
