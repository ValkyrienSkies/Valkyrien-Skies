package com.jackredcreeper.cannon.items;

import java.util.List;

import com.jackredcreeper.cannon.CannonModReference;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

public class ItemPrimer extends Item {

	public ItemPrimer() {

		setUnlocalizedName(CannonModReference.ModItems.PRIMER.getUnlocalizedName());
		setRegistryName(CannonModReference.ModItems.PRIMER.getRegistryName());

        this.setCreativeTab(CreativeTabs.COMBAT);
        this.maxStackSize = 1;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List itemInformation, boolean par4) {
		itemInformation.add(TextFormatting.BLUE + "Fires your cannon");
	}
}
