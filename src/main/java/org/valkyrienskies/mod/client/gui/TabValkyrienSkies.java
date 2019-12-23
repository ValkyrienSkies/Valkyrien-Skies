package org.valkyrienskies.mod.client.gui;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;

@MethodsReturnNonnullByDefault
public class TabValkyrienSkies extends CreativeTabs {

    public TabValkyrienSkies(String label) {
        super(label);
    }

    @Override
    public ItemStack createIcon() {
        return new ItemStack(Item.getItemFromBlock(ValkyrienSkiesMod.INSTANCE.physicsInfuser));
    }

}
