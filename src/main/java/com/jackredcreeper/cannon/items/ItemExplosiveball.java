package com.jackredcreeper.cannon.items;

import com.jackredcreeper.cannon.Reference;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemExplosiveball extends Item {
	
	public ItemExplosiveball() {
		
		setUnlocalizedName(Reference.ModItems.EXPLOSIVEBALL.getUnlocalizedName());
		setRegistryName(Reference.ModItems.EXPLOSIVEBALL.getRegistryName());
		
        this.setCreativeTab(CreativeTabs.REDSTONE);
	}
	
}
