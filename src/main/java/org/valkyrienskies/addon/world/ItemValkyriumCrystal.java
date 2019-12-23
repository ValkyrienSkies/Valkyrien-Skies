package org.valkyrienskies.addon.world;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.valkyrienskies.addon.world.util.BaseItem;

public class ItemValkyriumCrystal extends BaseItem {

    public ItemValkyriumCrystal() {
        super("valkyrium_crystal", true);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player,
        List<String> itemInformation, ITooltipFlag advanced) {
        itemInformation.add(TextFormatting.ITALIC + "" + TextFormatting.BLUE + TextFormatting.ITALIC + I18n.format("tooltip.vs_world.valkyrium_crystal"));
    }
}
