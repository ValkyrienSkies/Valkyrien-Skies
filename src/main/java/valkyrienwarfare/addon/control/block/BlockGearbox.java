package valkyrienwarfare.addon.control.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import valkyrienwarfare.addon.control.tileentity.TileEntityGearbox;

public class BlockGearbox extends Block implements ITileEntityProvider {
	
	public BlockGearbox(Material materialIn) {
		super(materialIn);
	}
	
    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        EnumFacing facingHorizontal = placer.getHorizontalFacing();
        if (!placer.isSneaking()) {
            facingHorizontal = facingHorizontal.getOpposite();
        }
        return this.getDefaultState().withProperty(BlockHorizontal.FACING, facingHorizontal);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[]{BlockHorizontal.FACING});
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing enumfacing = EnumFacing.getFront(meta);
        if (enumfacing.getAxis() == EnumFacing.Axis.Y) {
            enumfacing = EnumFacing.NORTH;
        }
        return this.getDefaultState().withProperty(BlockHorizontal.FACING, enumfacing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int i = ((EnumFacing) state.getValue(BlockHorizontal.FACING)).getIndex();
        return i;
    }

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityGearbox();
	}

    @Override
    public BlockRenderLayer getBlockLayer() {
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
