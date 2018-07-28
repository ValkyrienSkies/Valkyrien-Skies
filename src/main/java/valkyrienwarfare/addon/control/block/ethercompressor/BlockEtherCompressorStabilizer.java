package valkyrienwarfare.addon.control.block.ethercompressor;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import valkyrienwarfare.addon.control.block.BlockForceProviderBasic;
import valkyrienwarfare.addon.control.tileentity.TileEntityEtherCompressorStabilizer;
import valkyrienwarfare.math.Vector;

public class BlockEtherCompressorStabilizer extends BlockForceProviderBasic {

	private final double enginePower;
	
	public BlockEtherCompressorStabilizer(Material materialIn) {
		super(materialIn);
		this.enginePower = 8000D;
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityEtherCompressorStabilizer(new Vector(0, 1, 0), this.enginePower);
	}

	@Override
	public boolean shouldLocalForceBeRotated(World world, BlockPos pos, IBlockState state, double secondsToApply) {
		return false;
	}

}
