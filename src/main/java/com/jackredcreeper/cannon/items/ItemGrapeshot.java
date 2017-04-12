package com.jackredcreeper.cannon.items;

import com.jackredcreeper.cannon.Reference;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemGrapeshot extends Item {
	
	public ItemGrapeshot() {
		
		setUnlocalizedName(Reference.ModItems.GRAPESHOT.getUnlocalizedName());
		setRegistryName(Reference.ModItems.GRAPESHOT.getRegistryName());
		
        this.setCreativeTab(CreativeTabs.COMBAT);
	}
	
}
