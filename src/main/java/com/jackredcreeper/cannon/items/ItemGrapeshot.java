package com.jackredcreeper.cannon.items;

import com.jackredcreeper.cannon.CannonModRefrence;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemGrapeshot extends Item {
	
	public ItemGrapeshot() {
		
		setUnlocalizedName(CannonModRefrence.ModItems.GRAPESHOT.getUnlocalizedName());
		setRegistryName(CannonModRefrence.ModItems.GRAPESHOT.getRegistryName());
		
        this.setCreativeTab(CreativeTabs.COMBAT);
	}
	
}
