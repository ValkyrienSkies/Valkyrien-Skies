package com.jackredcreeper.cannon.items;

import com.jackredcreeper.cannon.CannonModRefrence;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemSolidball extends Item {
	
	public ItemSolidball() {
		
		setUnlocalizedName(CannonModRefrence.ModItems.SOLIDBALL.getUnlocalizedName());
		setRegistryName(CannonModRefrence.ModItems.SOLIDBALL.getRegistryName());
		
        this.setCreativeTab(CreativeTabs.COMBAT);
	}
	
}
