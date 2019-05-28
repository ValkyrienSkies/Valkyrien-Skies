package valkyrienwarfare.mod.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemPhysicsCore extends Item {

    public ItemPhysicsCore() {
        this.setMaxStackSize(1);
        this.setMaxDamage(80);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> itemInformation,
                               ITooltipFlag advanced) {
        itemInformation.add("" + TextFormatting.BLUE + TextFormatting.ITALIC + "The core of a physics infuser");
    }
}
