package com.jackredcreeper.cannon.items;

import com.jackredcreeper.cannon.CannonModReference;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemSolidball extends Item {

	public ItemSolidball() {

		setUnlocalizedName(CannonModReference.ModItems.SOLIDBALL.getUnlocalizedName());
		setRegistryName(CannonModReference.ModItems.SOLIDBALL.getRegistryName());

        this.setCreativeTab(CreativeTabs.COMBAT);
	}

}
