package com.jackredcreeper.cannon.items;

import com.jackredcreeper.cannon.CannonModRefrence;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemExplosiveball extends Item {
	
	public ItemExplosiveball() {
		
		setUnlocalizedName(CannonModRefrence.ModItems.EXPLOSIVEBALL.getUnlocalizedName());
		setRegistryName(CannonModRefrence.ModItems.EXPLOSIVEBALL.getRegistryName());
		
        this.setCreativeTab(CreativeTabs.COMBAT);
	}
	
}
