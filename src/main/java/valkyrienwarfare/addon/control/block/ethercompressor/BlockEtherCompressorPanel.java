package valkyrienwarfare.addon.control.block.ethercompressor;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import valkyrienwarfare.addon.control.tileentity.TileEntityEtherCompressorPanel;
import valkyrienwarfare.deprecated_api.IBlockForceProvider;
import valkyrienwarfare.math.Vector;

public class BlockEtherCompressorPanel extends BlockForceProviderBasic {

	public BlockEtherCompressorPanel(Material materialIn) {
		super(materialIn);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityEtherCompressorPanel(new Vector(0, 1, 0), 100000D);
	}

	@Override
	public boolean shouldLocalForceBeRotated(World world, BlockPos pos, IBlockState state, double secondsToApply) {
		return false;
	}

}
