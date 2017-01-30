package com.jackredcreeper.cannon.items;

import com.jackredcreeper.cannon.Reference;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemLoader extends Item {
	
	public ItemLoader() {
		
		setUnlocalizedName(Reference.ModItems.LOADER.getUnlocalizedName());
		setRegistryName(Reference.ModItems.LOADER.getRegistryName());
		
        this.setCreativeTab(CreativeTabs.REDSTONE);
	}
	
}
