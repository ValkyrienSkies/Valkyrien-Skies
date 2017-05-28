package com.jackredcreeper.cannon.items;

import com.jackredcreeper.cannon.CannonModRefrence;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemLoader extends Item {
	
	public ItemLoader() {
		
		setUnlocalizedName(CannonModRefrence.ModItems.LOADER.getUnlocalizedName());
		setRegistryName(CannonModRefrence.ModItems.LOADER.getRegistryName());
		
        this.setCreativeTab(CreativeTabs.COMBAT);
	}
	
}
