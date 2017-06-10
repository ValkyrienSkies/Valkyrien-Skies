package com.jackredcreeper.cannon.init;

import com.jackredcreeper.cannon.CannonModReference;
import com.jackredcreeper.cannon.blocks.BlockAirMine;
import com.jackredcreeper.cannon.blocks.BlockCannon;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModBlocks {

	public static Block cannon;
	public static Block airmine;
	
	
	public static void init() {
		cannon = new BlockCannon();
		airmine = new BlockAirMine();
		
	}
	
	public static void register() {
		registerBlock(cannon);
		registerBlock(airmine);
	}
	
	public static void registerBlock(Block block) {

		GameRegistry.register(block);
		ItemBlock Item = new ItemBlock(block);
		Item.setRegistryName(block.getRegistryName());
		GameRegistry.register(Item);
	}
	
	public static void registerRenders() {
		registerRender(cannon);
		registerRender(airmine);
		
	}
	
	private static void registerRender(Block block) {
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(Item.getItemFromBlock(block), 0, new ModelResourceLocation(block.getRegistryName(), "inventory"));
	}
	
}
