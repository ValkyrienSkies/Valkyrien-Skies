package org.valkyrienskies.addon.control.block;

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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.valkyrienskies.addon.control.block.torque.TileEntityRotationAxle;

public class BlockRotationAxle extends BlockRotatedPillar implements ITileEntityProvider {

    public BlockRotationAxle() {
        super("rotation_axle", Material.IRON, 0.0F, true);
        this.setHardness(6.0F);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player,
        List<String> itemInformation,
        ITooltipFlag advanced) {
        itemInformation
            .add(TextFormatting.BLUE + I18n.format("tooltip.vs_control.rotation_axle"));
    }

    @Override
    public boolean rotateBlock(World world, BlockPos pos, EnumFacing axisFacing) {
        boolean result = super.rotateBlock(world, pos, axisFacing);
        if (result) {
            TileEntity tileEntityAxle = world.getTileEntity(pos);
            if (tileEntityAxle instanceof TileEntityRotationAxle) {
                ((TileEntityRotationAxle) tileEntityAxle).setAxleAxis(axisFacing.getAxis());
            }
        }
        return result;
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

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        IBlockState state = getStateFromMeta(meta);
        return new TileEntityRotationAxle(state.getValue(AXIS));
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        super.breakBlock(worldIn, pos, state);
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile != null) {
            tile.invalidate();
        }
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        switch (state.getValue(AXIS)) {
            case X:
                return new AxisAlignedBB(0, .4, .4, 1, .6, .6);
            case Y:
                return new AxisAlignedBB(.4, 0, .4, .6, 1, .6);
            case Z:
                return new AxisAlignedBB(.4, .4, 0, .6, .6, 1);
            default:
                return FULL_BLOCK_AABB;
        }
    }
}
