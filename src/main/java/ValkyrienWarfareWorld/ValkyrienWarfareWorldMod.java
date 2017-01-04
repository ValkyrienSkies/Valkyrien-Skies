package ValkyrienWarfareWorld;

import ValkyrienWarfareWorld.Proxy.CommonProxy;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
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

	@SidedProxy(clientSide = "ValkyrienWarfareWorld.Proxy.ClientProxy", serverSide = "ValkyrienWarfareWorld.Proxy.CommonProxy")
	public static CommonProxy proxy;

	public static final String MODID = "valkyrienwarfareworld";
	public static final String MODNAME = "Valkyrien Warfare World";
	public static final String MODVER = "0.0a";

	public static ValkyrienWarfareWorldMod instance;

	public static Block etheriumOre;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		instance = this;
		registerBlocks(event);
		proxy.preInit(event);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		EntityRegistry.registerModEntity(EntityFallingUpBlock.class, "FallingUpBlockEntity", 75, this, 80, 1, true);
		proxy.init(event);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {

	}

	private void registerBlocks(FMLStateEvent event) {
		etheriumOre = new BlockEtheriumOre(Material.ROCK).setHardness(12f).setUnlocalizedName("etheriumore").setRegistryName(MODID, "etheriumore").setCreativeTab(CreativeTabs.TRANSPORTATION);

		GameRegistry.registerBlock(etheriumOre);
	}

}