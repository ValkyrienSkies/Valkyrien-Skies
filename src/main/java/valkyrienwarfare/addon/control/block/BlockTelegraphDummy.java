package valkyrienwarfare.addon.control.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import valkyrienwarfare.addon.control.ValkyrienWarfareControl;
import valkyrienwarfare.addon.control.tileentity.TileEntityShipTelegraph;

public class BlockTelegraphDummy extends Block {

	public BlockTelegraphDummy(Material materialIn) {
		super(materialIn);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!worldIn.isRemote) {
			IBlockState belowState = worldIn.getBlockState(pos.down());
			belowState.getBlock().onBlockActivated(worldIn, pos.down(), belowState, playerIn, hand, side, hitX, hitY,
					hitZ);
		}
		return true;
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
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }
    
    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
    	super.breakBlock(worldIn, pos, state);
    	if (worldIn.getBlockState(pos.down()).getBlock() == ValkyrienWarfareControl.INSTANCE.vwControlBlocks.shipTelegraph) {
    		worldIn.setBlockToAir(pos.down());
    	}
    }
}
