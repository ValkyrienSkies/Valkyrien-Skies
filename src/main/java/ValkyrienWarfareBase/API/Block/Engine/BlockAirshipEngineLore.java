package ValkyrienWarfareBase.API.Block.Engine;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

/**
 * The same as a normal engine, but says speed in the tooltip
 */
public abstract class BlockAirshipEngineLore extends BlockAirshipEngine {

    String[] lore;

    public BlockAirshipEngineLore(Material materialIn, double enginePower) {
        super(materialIn, enginePower);
        lore = new String[]{"" + TextFormatting.GRAY + TextFormatting.ITALIC + TextFormatting.BOLD + "Force:", "  " + this.getEnginePowerTooltip() + " Newtons"};
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List itemInformation, boolean par4) {
        for (String s : lore) {
            itemInformation.add(s);
        }
    }

    public abstract String getEnginePowerTooltip();
}
