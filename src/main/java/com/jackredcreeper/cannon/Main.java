package com.jackredcreeper.cannon;

import com.jackredcreeper.cannon.init.ModBlocks;
import com.jackredcreeper.cannon.init.ModItems;
import com.jackredcreeper.cannon.proxy.CommonProxy;
import com.jackredcreeper.cannon.tileentity.TileEntityCannon;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid = Reference.MOD_ID, version = Reference.MOD_ID, acceptedMinecraftVersions = "[1.10.2]")
public class Main {
	
//	@Instance
//	public static Main instance;
	
	@SidedProxy(clientSide = Reference.CLIENT, serverSide = Reference.SERVER)
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
		
		//Craft
		
		GameRegistry.registerTileEntity(TileEntityCannon.class, Reference.MOD_ID + "TileEntityCannon");
	}	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		System.out.println("aaAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAa");
	}
	
	
}
