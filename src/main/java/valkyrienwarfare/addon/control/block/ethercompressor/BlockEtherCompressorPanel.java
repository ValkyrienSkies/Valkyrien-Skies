package valkyrienwarfare.addon.control.block.ethercompressor;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import valkyrienwarfare.addon.control.block.BlockForceProviderBasic;
import valkyrienwarfare.addon.control.piloting.ITileEntityPilotable;
import valkyrienwarfare.addon.control.tileentity.TileEntityEtherCompressorPanel;
import valkyrienwarfare.deprecated_api.IBlockForceProvider;
import valkyrienwarfare.math.Vector;

public class BlockEtherCompressorPanel extends BlockForceProviderBasic {

	public BlockEtherCompressorPanel(Material materialIn) {
		super(materialIn);
	}
	
	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		TileEntity tile = worldIn.getTileEntity(pos);
	    if (tile != null && tile instanceof TileEntityEtherCompressorPanel) {
	    	TileEntityEtherCompressorPanel multiBlock = (TileEntityEtherCompressorPanel) tile;
	        if (multiBlock.isPartOfMultiBlock()) {
	            if (multiBlock.isMultiBlockMaster()) {
	            	System.out.println("Testing master");
	                if (!multiBlock.checkIfMultiBlockFormed())
	                    multiBlock.resetStructure();
	            } else {
	            	if (!multiBlock.checkForMaster()) {
                        multiBlock.reset();
                        System.out.println("Queueing update");
                        worldIn.neighborChanged(pos.east(), blockIn, pos);
                        worldIn.neighborChanged(pos.west(), blockIn, pos);
                        worldIn.neighborChanged(pos.north(), blockIn, pos);
                        worldIn.neighborChanged(pos.south(), blockIn, pos);
                        worldIn.neighborChanged(pos.up(), blockIn, pos);
                        worldIn.neighborChanged(pos.down(), blockIn, pos);
                    }
	            }
	        }
	    }
	    super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
    }
	
	@Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote && false) {
            TileEntity tileIn = worldIn.getTileEntity(pos);
            if (tileIn instanceof TileEntityEtherCompressorPanel) {
                System.out.println(((TileEntityEtherCompressorPanel) tileIn).isPartOfMultiBlock());
            }
        }
        return false;
    }
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityEtherCompressorPanel(new Vector(0, 1, 0), 50000D);
	}

	@Override
	public boolean shouldLocalForceBeRotated(World world, BlockPos pos, IBlockState state, double secondsToApply) {
		return false;
	}

}
