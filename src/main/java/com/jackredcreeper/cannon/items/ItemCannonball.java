package com.jackredcreeper.cannon.items;

import com.jackredcreeper.cannon.CannonModRefrence;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemCannonball extends Item {
	
	public ItemCannonball() {
		
		setUnlocalizedName(CannonModRefrence.ModItems.CANNONBALL.getUnlocalizedName());
		setRegistryName(CannonModRefrence.ModItems.CANNONBALL.getRegistryName());
		
        this.setCreativeTab(CreativeTabs.COMBAT);
	}
	
}
