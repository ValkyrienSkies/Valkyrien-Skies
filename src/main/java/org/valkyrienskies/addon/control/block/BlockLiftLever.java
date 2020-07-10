package org.valkyrienskies.addon.control.block;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.valkyrienskies.addon.control.tileentity.TileEntityLiftLever;
import org.valkyrienskies.mod.common.block.BlockPilotableBasic;

public class BlockLiftLever extends BlockPilotableBasic {

    public BlockLiftLever() {
        super("lift_lever", Material.IRON, 5.0F);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player,
        List<String> itemInformation,
        ITooltipFlag advanced) {
        itemInformation
            .add(TextFormatting.BLUE + I18n.format("tooltip.vs_control.lift_lever"));
    }

    @Override
    @SuppressWarnings("deprecation")
    @Nonnull
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing,
        float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        EnumFacing facingHorizontal = placer.getHorizontalFacing();
        if (!placer.isSneaking()) {
            facingHorizontal = facingHorizontal.getOpposite();
        }
        return this.getDefaultState().withProperty(BlockHorizontal.FACING, facingHorizontal);
    }

    @Override
    @Nonnull
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BlockHorizontal.FACING);
    }

    @Override
    @SuppressWarnings("deprecation")
    @Nonnull
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing enumfacing = EnumFacing.byIndex(meta);
        if (enumfacing.getAxis() == EnumFacing.Axis.Y) {
            enumfacing = EnumFacing.NORTH;
        }
        return this.getDefaultState().withProperty(BlockHorizontal.FACING, enumfacing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(BlockHorizontal.FACING)
            .getIndex();
    }

    @Override
    @Nonnull
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public TileEntity createNewTileEntity(@Nonnull World worldIn, int meta) {
        return new TileEntityLiftLever();
    }

}
