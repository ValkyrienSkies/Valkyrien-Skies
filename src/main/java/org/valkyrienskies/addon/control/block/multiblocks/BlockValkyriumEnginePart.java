package org.valkyrienskies.addon.control.block.multiblocks;

import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.valkyrienskies.addon.control.util.BaseBlock;

public class BlockValkyriumEnginePart extends BaseBlock implements ITileEntityProvider {

    public BlockValkyriumEnginePart() {
        super("valkyrium_engine_part", Material.IRON, 0.0F, true);
        this.setHardness(6.0F);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player,
        List<String> itemInformation,
        ITooltipFlag advanced) {
        itemInformation.add(TextFormatting.BLUE + I18n.format("tooltip.vs_control.valkyrium_engine_part"));
        itemInformation.add(TextFormatting.BLUE + "" + TextFormatting.ITALIC + I18n.format("tooltip.vs_control.wrench_usage"));
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityValkyriumEnginePart();
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TileEntityValkyriumEnginePart) {
            ((TileEntityValkyriumEnginePart) tile).disassembleMultiblock();
        }
        super.breakBlock(worldIn, pos, state);
    }
}
