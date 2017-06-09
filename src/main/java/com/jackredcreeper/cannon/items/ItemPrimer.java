package com.jackredcreeper.cannon.items;

import com.jackredcreeper.cannon.CannonModReference;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemPrimer extends Item {
	
	public ItemPrimer() {
		
		setUnlocalizedName(CannonModReference.ModItems.PRIMER.getUnlocalizedName());
		setRegistryName(CannonModReference.ModItems.PRIMER.getRegistryName());
		
        this.setCreativeTab(CreativeTabs.COMBAT);
        this.maxStackSize = 1;
	}
	
}
