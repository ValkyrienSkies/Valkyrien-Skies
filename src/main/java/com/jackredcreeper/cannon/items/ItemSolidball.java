package com.jackredcreeper.cannon.items;

import com.jackredcreeper.cannon.Reference;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemSolidball extends Item {
	
	public ItemSolidball() {
		
		setUnlocalizedName(Reference.ModItems.SOLIDBALL.getUnlocalizedName());
		setRegistryName(Reference.ModItems.SOLIDBALL.getRegistryName());
		
        this.setCreativeTab(CreativeTabs.COMBAT);
	}
	
}
