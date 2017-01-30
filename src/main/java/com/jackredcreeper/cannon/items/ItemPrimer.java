package com.jackredcreeper.cannon.items;

import com.jackredcreeper.cannon.Reference;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemPrimer extends Item {
	
	public ItemPrimer() {
		
		setUnlocalizedName(Reference.ModItems.PRIMER.getUnlocalizedName());
		setRegistryName(Reference.ModItems.PRIMER.getRegistryName());
		
        this.setCreativeTab(CreativeTabs.REDSTONE);
	}
	
}
