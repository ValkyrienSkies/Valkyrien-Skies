package ValkyrienWarfareBase.GUI;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class TabValkyrienWarfare extends CreativeTabs {
    public ItemStack stack = new ItemStack(new ItemBlock(ValkyrienWarfareMod.physicsInfuser));

    public TabValkyrienWarfare() {
        super(12, "Valkyrien Warfare");
    }

    @Override
    public ItemStack getTabIconItem() {
        return stack;
    }
}
