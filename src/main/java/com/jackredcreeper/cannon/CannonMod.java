package com.jackredcreeper.cannon;

import com.jackredcreeper.cannon.init.ModBlocks;
import com.jackredcreeper.cannon.init.ModItems;
import com.jackredcreeper.cannon.proxy.CommonProxy;
import com.jackredcreeper.cannon.tileentity.TileEntityCannon;
import com.jackredcreeper.cannon.world.ExplosionHandler;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid = CannonModRefrence.MOD_ID, version = CannonModRefrence.MOD_ID, acceptedMinecraftVersions = "[1.10.2]")
public class CannonMod {

	@Instance(CannonModRefrence.MOD_ID)
	public static CannonMod instance = new CannonMod();

	@SidedProxy(clientSide = CannonModRefrence.CLIENT, serverSide = CannonModRefrence.SERVER)
	public static CommonProxy proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		ModItems.init();
		ModItems.register();

		ModBlocks.init();
		ModBlocks.register();

	}
	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		proxy.init();

		MinecraftForge.EVENT_BUS.register(new ExplosionHandler());

		//Craft

		GameRegistry.registerTileEntity(TileEntityCannon.class, CannonModRefrence.MOD_ID + "TileEntityCannon");
	}
	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
	}


}
