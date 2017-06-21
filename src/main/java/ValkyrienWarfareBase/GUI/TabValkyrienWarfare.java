package ValkyrienWarfareBase.GUI;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class TabValkyrienWarfare extends CreativeTabs {
    public TabValkyrienWarfare() {
        super(12, "valkyrienwarfare");
    }

    @Override
    public ItemStack getTabIconItem() {
        return new ItemStack(Item.getItemFromBlock(ValkyrienWarfareMod.physicsInfuser));
    }
}
