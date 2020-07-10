package org.valkyrienskies.addon.control.block;

import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
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
import org.valkyrienskies.addon.control.tileentity.TileEntityGearbox;
import org.valkyrienskies.addon.control.util.BaseBlock;

/**
 * Class for the Gearbox block. Note that the FACING property is only used to determine the initial
 * orientation of the gearbox. Afterwards the value stored by the tile tile entity dominates.
 */
public class BlockGearbox extends BaseBlock implements ITileEntityProvider {

    public BlockGearbox() {
        super("gearbox", Material.IRON, 0.0F, true);
        this.setHardness(6.0F);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player,
        List<String> itemInformation,
        ITooltipFlag advanced) {
        itemInformation.add(TextFormatting.BLUE + I18n.format("tooltip.vs_control.gearbox_1"));
        itemInformation.add(TextFormatting.BLUE + I18n.format("tooltip.vs_control.gearbox_2"));
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing,
        float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        EnumFacing facingHorizontal = placer.getHorizontalFacing();
        if (!placer.isSneaking()) {
            facingHorizontal = facingHorizontal.getOpposite();
        }
        return this.getDefaultState().withProperty(BlockHorizontal.FACING, facingHorizontal);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BlockHorizontal.FACING);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing enumfacing = EnumFacing.byIndex(meta);
        if (enumfacing.getAxis() == EnumFacing.Axis.Y) {
            enumfacing = EnumFacing.NORTH;
        }
        return this.getDefaultState().withProperty(BlockHorizontal.FACING, enumfacing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int i = state.getValue(BlockHorizontal.FACING)
            .getIndex();
        return i;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        EnumFacing facing = EnumFacing.byIndex(meta);
        return new TileEntityGearbox(facing);
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
}
