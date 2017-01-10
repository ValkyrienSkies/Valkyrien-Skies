package ValkyrienWarfareWorld;

import ValkyrienWarfareWorld.Proxy.CommonProxyWorld;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid = ValkyrienWarfareWorldMod.MODID, name = ValkyrienWarfareWorldMod.MODNAME, version = ValkyrienWarfareWorldMod.MODVER)
public class ValkyrienWarfareWorldMod {

	@SidedProxy(clientSide = "ValkyrienWarfareWorld.Proxy.ClientProxyWorld", serverSide = "ValkyrienWarfareWorld.Proxy.CommonProxyWorld")
	public static CommonProxyWorld proxy;

	public static final String MODID = "valkyrienwarfareworld";
	public static final String MODNAME = "Valkyrien Warfare World";
	public static final String MODVER = "0.0a";

	public static ValkyrienWarfareWorldMod instance;

	public static Block etheriumOre;
	
	public static Item etheriumCrystal;
	
	private static final WorldEventsCommon worldEventsCommon = new WorldEventsCommon();

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		instance = this;
		registerBlocks(event);
		registerItems(event);
		proxy.preInit(event);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		EntityRegistry.registerModEntity(EntityFallingUpBlock.class, "FallingUpBlockEntity", 75, this, 80, 1, true);
		MinecraftForge.EVENT_BUS.register(worldEventsCommon);
		proxy.init(event);
		
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		proxy.postInit(event);
	}

	private void registerBlocks(FMLStateEvent event) {
		etheriumOre = new BlockEtheriumOre(Material.ROCK).setHardness(3f).setUnlocalizedName("etheriumore").setRegistryName(MODID, "etheriumore").setCreativeTab(CreativeTabs.TRANSPORTATION);

		GameRegistry.registerBlock(etheriumOre);
	}
	
	private void registerItems(FMLStateEvent event) {
		etheriumCrystal = new ItemEtheriumCrystal().setUnlocalizedName("etheriumcrystal").setRegistryName(MODID, "etheriumcrystal").setCreativeTab(CreativeTabs.TRANSPORTATION).setMaxStackSize(16);
	
		GameRegistry.registerItem(etheriumCrystal);
	}

}