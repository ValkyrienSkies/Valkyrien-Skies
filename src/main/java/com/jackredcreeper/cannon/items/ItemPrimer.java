package com.jackredcreeper.cannon.items;

import com.jackredcreeper.cannon.CannonModRefrence;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemPrimer extends Item {
	
	public ItemPrimer() {
		
		setUnlocalizedName(CannonModRefrence.ModItems.PRIMER.getUnlocalizedName());
		setRegistryName(CannonModRefrence.ModItems.PRIMER.getRegistryName());
		
        this.setCreativeTab(CreativeTabs.COMBAT);
	}
	
}
