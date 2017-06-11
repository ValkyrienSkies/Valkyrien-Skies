package com.jackredcreeper.cannon.items;

import com.jackredcreeper.cannon.CannonModReference;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemGrapeshot extends Item {

	public ItemGrapeshot() {

		setUnlocalizedName(CannonModReference.ModItems.GRAPESHOT.getUnlocalizedName());
		setRegistryName(CannonModReference.ModItems.GRAPESHOT.getRegistryName());

        this.setCreativeTab(CreativeTabs.COMBAT);
	}

}
