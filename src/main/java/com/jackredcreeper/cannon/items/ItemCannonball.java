package com.jackredcreeper.cannon.items;

import com.jackredcreeper.cannon.CannonModReference;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemCannonball extends Item {

	public ItemCannonball() {

		setUnlocalizedName(CannonModReference.ModItems.CANNONBALL.getUnlocalizedName());
		setRegistryName(CannonModReference.ModItems.CANNONBALL.getRegistryName());

        this.setCreativeTab(CreativeTabs.COMBAT);
	}

}
