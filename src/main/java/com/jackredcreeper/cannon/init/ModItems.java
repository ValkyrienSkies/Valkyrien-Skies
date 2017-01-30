package com.jackredcreeper.cannon.init;

import com.jackredcreeper.cannon.Reference;
import com.jackredcreeper.cannon.items.ItemPrimer;
import com.jackredcreeper.cannon.items.ItemCannonball;
import com.jackredcreeper.cannon.items.ItemExplosiveball;
import com.jackredcreeper.cannon.items.ItemLoader;
import com.jackredcreeper.cannon.items.ItemTuner;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModItems {
	
	public static Item key;
	public static Item loader;
	public static Item tuner;
	public static Item cannonball;
	public static Item explosiveball;
	
	public static void init() {
		key = new ItemPrimer();
		loader = new ItemLoader();
		tuner = new ItemTuner();
		cannonball = new ItemCannonball();
		explosiveball = new ItemExplosiveball();
		
	}
	
	public static void register() {
		GameRegistry.register(key);
		GameRegistry.register(loader);
		GameRegistry.register(tuner);
		GameRegistry.register(cannonball);
		GameRegistry.register(explosiveball);
	}
	
	public static void registerRenders() {
		registerRender(key);
		registerRender(loader);
		registerRender(tuner);
		registerRender(cannonball);
		registerRender(explosiveball);
	}
	
	private static void registerRender(Item item) {
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
	}
	
}
