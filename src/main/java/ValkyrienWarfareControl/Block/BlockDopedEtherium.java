package ValkyrienWarfareControl.Block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class BlockDopedEtherium extends Block {

    public BlockDopedEtherium(Material materialIn) {
        super(materialIn);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List itemInformation, boolean par4) {
        itemInformation.add(TextFormatting.ITALIC + "" + TextFormatting.BLUE + "Creates an upward force in any ship its placed in.");
    }

}
