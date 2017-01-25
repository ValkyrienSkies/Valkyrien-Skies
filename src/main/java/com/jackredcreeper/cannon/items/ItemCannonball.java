package com.jackredcreeper.cannon.items;

import com.jackredcreeper.cannon.Reference;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemCannonball extends Item {
	
	public ItemCannonball() {
		
		setUnlocalizedName(Reference.ModItems.CANNONBALL.getUnlocalizedName());
		setRegistryName(Reference.ModItems.CANNONBALL.getRegistryName());
		
        this.setCreativeTab(CreativeTabs.REDSTONE);
	}
	
}
