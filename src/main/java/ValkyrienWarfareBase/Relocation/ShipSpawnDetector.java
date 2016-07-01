package ValkyrienWarfareBase.Relocation;

import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ShipSpawnDetector extends SpatialDetector{

	public ShipSpawnDetector(BlockPos start, World worldIn, int maximum, boolean checkCorners) {
		super(start, worldIn, maximum, checkCorners);
	}

	@Override
	public boolean isValidExpansion(int x, int y, int z) {
		return !cache.getBlockState(x,y,z).getBlock().equals(Blocks.AIR);
	}

}
