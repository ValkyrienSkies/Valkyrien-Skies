package org.valkyrienskies.addon.control.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.valkyrienskies.addon.control.util.BaseItem;

public class ItemPhysicsCore extends BaseItem {

    public ItemPhysicsCore() {
        super("physics_core", true);
        this.setMaxStackSize(1);
        this.setMaxDamage(80);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player,
        List<String> itemInformation,
        ITooltipFlag advanced) {
        itemInformation.add(TextFormatting.BLUE + "" + TextFormatting.ITALIC + I18n.format("tooltip.vs_control.physics_core"));
    }
}
