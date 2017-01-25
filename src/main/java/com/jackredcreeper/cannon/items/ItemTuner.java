package com.jackredcreeper.cannon.items;

import com.jackredcreeper.cannon.Reference;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemTuner extends Item {
	
	public ItemTuner() {
		
		setUnlocalizedName(Reference.ModItems.TUNER.getUnlocalizedName());
		setRegistryName(Reference.ModItems.TUNER.getRegistryName());
		
        this.setCreativeTab(CreativeTabs.REDSTONE);
	}
	
}
