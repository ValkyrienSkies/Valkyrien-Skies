package ValkyrienWarfareBase.Relocation;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ShipSpawnDetector extends SpatialDetector{
	
	private static final ArrayList<Block> blackList = new ArrayList<Block>();
	
	static{
		blackList.add(Blocks.AIR);
		blackList.add(Blocks.DIRT);
		blackList.add(Blocks.GRASS);
		blackList.add(Blocks.STONE);
		blackList.add(Blocks.TALLGRASS);
//		blackList.add(Blocks.LEAVES);
//		blackList.add(Blocks.LEAVES2);
		blackList.add(Blocks.WATER);
		blackList.add(Blocks.FLOWING_WATER);
		blackList.add(Blocks.SAND);
		blackList.add(Blocks.SANDSTONE);
		blackList.add(Blocks.GRAVEL);
		blackList.add(Blocks.ICE);
		blackList.add(Blocks.SNOW);
		blackList.add(Blocks.SNOW_LAYER);
		blackList.add(Blocks.LAVA);
		blackList.add(Blocks.FLOWING_LAVA);
		blackList.add(Blocks.GRASS_PATH);
		blackList.add(Blocks.BEDROCK);
	}

	public ShipSpawnDetector(BlockPos start, World worldIn, int maximum, boolean checkCorners) {
		super(start, worldIn, maximum, checkCorners);
	}

	@Override
	public boolean isValidExpansion(int x, int y, int z) {//
		return !blackList.contains(cache.getBlockState(x,y,z).getBlock());
	}

}
