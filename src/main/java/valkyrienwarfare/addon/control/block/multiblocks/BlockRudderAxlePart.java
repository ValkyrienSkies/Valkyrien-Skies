package valkyrienwarfare.addon.control.block.multiblocks;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import valkyrienwarfare.deprecated_api.IBlockForceProvider;
import valkyrienwarfare.math.Vector;

public class BlockRudderAxlePart extends Block implements ITileEntityProvider, IBlockForceProvider {

	public BlockRudderAxlePart(Material materialIn) {
		super(materialIn);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityRudderAxlePart();
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
    
    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
    	TileEntity tile = worldIn.getTileEntity(pos);
    	if (tile instanceof TileEntityRudderAxlePart) {
    		TileEntityRudderAxlePart.class.cast(tile).dissembleMultiblock();
    	}
    	super.breakBlock(worldIn, pos, state);
    }

	@Override
	public Vector getBlockForceInShipSpace(World world, BlockPos pos, IBlockState state, Entity shipEntity,
			double secondsToApply) {
		return null;
	}

	@Override
	public boolean shouldLocalForceBeRotated(World world, BlockPos pos, IBlockState state, double secondsToApply) {
		return false;
	}

}
