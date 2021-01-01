package org.valkyrienskies.mod.common.block;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.valkyrienskies.mod.common.tileentity.TileEntityWaterPump;
import org.valkyrienskies.mod.common.util.BaseBlock;

import javax.annotation.Nullable;
import java.util.List;

public class BlockWaterPump extends BaseBlock implements ITileEntityProvider {

    public BlockWaterPump() {
        super("vs_water_pump", Material.IRON, 0, true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player,
                               List<String> itemInformation, ITooltipFlag advanced) {
        itemInformation.add(TextFormatting.ITALIC + "" + TextFormatting.BLUE + I18n
                .format("tooltip.valkyrienskies.vs_water_pump"));
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return new TileEntityWaterPump();
    }
}
