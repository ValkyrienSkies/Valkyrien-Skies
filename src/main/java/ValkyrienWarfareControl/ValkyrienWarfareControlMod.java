package ValkyrienWarfareControl;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareControl.Block.BlockAirShipEngine;
import ValkyrienWarfareControl.Block.BlockAntiGravEngine;
import ValkyrienWarfareControl.Block.BlockHovercraftController;
import ValkyrienWarfareControl.GUI.ControlGUIHandler;
import ValkyrienWarfareControl.Item.ItemSystemLinker;
import ValkyrienWarfareControl.Network.HovercraftControllerGUIInputHandler;
import ValkyrienWarfareControl.Network.HovercraftControllerGUIInputMessage;
import ValkyrienWarfareControl.TileEntity.AntiGravEngineTileEntity;
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
    public static final String MODVER = "0.2";
    
    public static ValkyrienWarfareControlMod instance;
    
    public static SimpleNetworkWrapper controlNetwork;
    
    public Block basicEngine;
    public Block basicHoverController;
    public Block antigravityEngine;
    
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
    	basicEngine = new BlockAirShipEngine(Material.WOOD).setHardness(5f).setUnlocalizedName("basicengine").setRegistryName(ValkyrienWarfareMod.MODID, "basicengine").setCreativeTab(CreativeTabs.REDSTONE);
    	basicHoverController = new BlockHovercraftController(Material.IRON).setHardness(10f).setUnlocalizedName("basichovercraftcontroller").setRegistryName(ValkyrienWarfareMod.MODID, "basichovercraftcontroller").setCreativeTab(CreativeTabs.REDSTONE);
    	antigravityEngine = new BlockAntiGravEngine(Material.IRON).setHardness(8f).setUnlocalizedName("antigravengine").setUnlocalizedName("antigravengine").setRegistryName(ValkyrienWarfareMod.MODID, "antigravengine").setCreativeTab(CreativeTabs.REDSTONE);
    	
    	GameRegistry.registerBlock(basicEngine);
    	GameRegistry.registerBlock(basicHoverController);
    	GameRegistry.registerBlock(antigravityEngine);
    }
    
    private void registerTileEntities(FMLStateEvent event){
    	TileEntity.addMapping(TileEntityHoverController.class, "tilehovercontroller");
    	TileEntity.addMapping(AntiGravEngineTileEntity.class, "tileantigravengine");
    }

    private void registerItems(FMLStateEvent event){
    	systemLinker = new ItemSystemLinker().setUnlocalizedName("systemlinker").setRegistryName(MODID, "systemlinker").setCreativeTab(CreativeTabs.REDSTONE).setMaxStackSize(1);
    	GameRegistry.registerItem(systemLinker);
    }
    
    private void registerRecipies(FMLStateEvent event){
    	GameRegistry.addRecipe(new ItemStack(basicEngine), new Object[] {"IWW", "IPP","IWW",'W',Item.getItemFromBlock(Blocks.PLANKS), 'P', Item.getItemFromBlock(Blocks.PISTON),'I',Items.IRON_INGOT});
    	
    }
    
    private void registerNetworks(FMLStateEvent event){
    	controlNetwork = NetworkRegistry.INSTANCE.newSimpleChannel("controlNetwork");
    	controlNetwork.registerMessage(HovercraftControllerGUIInputHandler.class, HovercraftControllerGUIInputMessage.class, 0, Side.SERVER);
    }
    
}
