package com.jackredcreeper.cannon.items;

import com.jackredcreeper.cannon.CannonModReference;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemTuner extends Item {

	public ItemTuner() {

		setUnlocalizedName(CannonModReference.ModItems.TUNER.getUnlocalizedName());
		setRegistryName(CannonModReference.ModItems.TUNER.getRegistryName());

        this.setCreativeTab(CreativeTabs.COMBAT);
        this.maxStackSize = 1;
	}

}
