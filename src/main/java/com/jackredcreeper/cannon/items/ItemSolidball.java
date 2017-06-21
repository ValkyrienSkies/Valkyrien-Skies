package com.jackredcreeper.cannon.items;

import com.jackredcreeper.cannon.CannonModReference;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class ItemSolidball extends Item {

    public ItemSolidball() {

        setUnlocalizedName(CannonModReference.ModItems.SOLIDBALL.getUnlocalizedName());
        setRegistryName(CannonModReference.ModItems.SOLIDBALL.getRegistryName());

        this.setCreativeTab(CreativeTabs.COMBAT);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List itemInformation, boolean par4) {
        itemInformation.add(TextFormatting.BLUE + "Railgun Shot");
    }

}
