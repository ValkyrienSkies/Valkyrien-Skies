package com.jackredcreeper.cannon.items;

import com.jackredcreeper.cannon.CannonModReference;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemExplosiveball extends Item {
	
	public ItemExplosiveball() {
		
		setUnlocalizedName(CannonModReference.ModItems.EXPLOSIVEBALL.getUnlocalizedName());
		setRegistryName(CannonModReference.ModItems.EXPLOSIVEBALL.getRegistryName());
		
        this.setCreativeTab(CreativeTabs.COMBAT);
	}
	
}
