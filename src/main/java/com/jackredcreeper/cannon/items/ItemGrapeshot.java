package com.jackredcreeper.cannon.items;

import java.util.List;

import com.jackredcreeper.cannon.CannonModReference;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

public class ItemGrapeshot extends Item {

	public ItemGrapeshot() {

		setUnlocalizedName(CannonModReference.ModItems.GRAPESHOT.getUnlocalizedName());
		setRegistryName(CannonModReference.ModItems.GRAPESHOT.getRegistryName());

        this.setCreativeTab(CreativeTabs.COMBAT);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List itemInformation, boolean par4) {
		itemInformation.add(TextFormatting.BLUE + "Multi Shot");
	}

}
