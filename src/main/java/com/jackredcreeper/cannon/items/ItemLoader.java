package com.jackredcreeper.cannon.items;

import com.jackredcreeper.cannon.CannonModReference;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemLoader extends Item {

	public ItemLoader() {

		setUnlocalizedName(CannonModReference.ModItems.LOADER.getUnlocalizedName());
		setRegistryName(CannonModReference.ModItems.LOADER.getRegistryName());

        this.setCreativeTab(CreativeTabs.COMBAT);
        this.maxStackSize = 1;
	}

}
