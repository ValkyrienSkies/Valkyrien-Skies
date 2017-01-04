package ValkyrienWarfareBase.Physics;

import java.util.HashMap;

import ValkyrienWarfareBase.API.IBlockMassProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockMass {

	public static BlockMass basicMass = new BlockMass();
	// 80N, Something like ~ 20lbs
	private final static double defaultMass = 80D;
	public HashMap<Block, Double> blockToMass = new HashMap<Block, Double>();

	public double getMassFromState(IBlockState state, BlockPos pos, World world) {
		Block block = state.getBlock();
		if (block instanceof IBlockMassProvider) {
			return ((IBlockMassProvider) block).getBlockMass(world, pos, state);
		} else {
			Double fromMap = blockToMass.get(block);
			if (fromMap != null) {
				return fromMap.doubleValue();
			} else {
				Double newMass = generateMassForBlock(block);
				blockToMass.put(block, newMass);
				return newMass;
			}
		}
	}

	public Double generateMassForBlock(Block block) {
		if (block instanceof BlockLiquid) {
			return 0D;
		}
		Material material = block.blockMaterial;

		double hardness = block.blockHardness;
		double resistance = block.blockResistance;

		return hardness * 50D + 2 * Math.pow(resistance, 1 / 4);
	}

	public static void registerBlockMass(Block block, double mass) {
		basicMass.blockToMass.put(block, mass);
	}

}
