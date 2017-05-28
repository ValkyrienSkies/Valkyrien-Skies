package com.jackredcreeper.cannon.items;

import com.jackredcreeper.cannon.CannonModRefrence;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemTuner extends Item {
	
	public ItemTuner() {
		
		setUnlocalizedName(CannonModRefrence.ModItems.TUNER.getUnlocalizedName());
		setRegistryName(CannonModRefrence.ModItems.TUNER.getRegistryName());
		
        this.setCreativeTab(CreativeTabs.COMBAT);
	}
	
}
