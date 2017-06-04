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
	private final static double defaultMass = 350D;
	public HashMap<Block, Double> blockToMass = new HashMap<Block, Double>();
	public HashMap<Material, Double> materialMass = new HashMap<Material, Double>();

	public BlockMass(){
		generateMaterialMasses();
	}

	private void generateMaterialMasses(){
		materialMass.put(Material.AIR, 0D);
		materialMass.put(Material.ANVIL, 500D);
		materialMass.put(Material.BARRIER, 0D);
		materialMass.put(Material.CACTUS, 35D);
		materialMass.put(Material.CAKE, 20D);
		materialMass.put(Material.CARPET, 10D);
		materialMass.put(Material.CIRCUITS, 35D);
		materialMass.put(Material.CLAY, 100D);
		materialMass.put(Material.CLOTH, 50D);
		materialMass.put(Material.CORAL, 150D);
		materialMass.put(Material.CRAFTED_SNOW, 45D);
		materialMass.put(Material.DRAGON_EGG, 50D);
		materialMass.put(Material.FIRE, 0D);
		materialMass.put(Material.GLASS, 60D);
		materialMass.put(Material.GOURD, 100D);
		materialMass.put(Material.GRASS, 65D);
		materialMass.put(Material.GROUND, 150D);
		materialMass.put(Material.ICE, 150D);
		materialMass.put(Material.IRON, 650D);
		materialMass.put(Material.LAVA, 0D);
		materialMass.put(Material.LEAVES, 20D);
		materialMass.put(Material.PACKED_ICE, 80D);
		materialMass.put(Material.PISTON, 30D);
		materialMass.put(Material.PLANTS, 35D);
		materialMass.put(Material.PORTAL, 0D);
		materialMass.put(Material.REDSTONE_LIGHT, 20D);
		materialMass.put(Material.ROCK, 500D);
		materialMass.put(Material.SAND, 150D);
		materialMass.put(Material.SNOW, 20D);
		materialMass.put(Material.SPONGE, 50D);
		materialMass.put(Material.STRUCTURE_VOID, 0D);
		materialMass.put(Material.TNT, 80D);
		materialMass.put(Material.VINE, 10D);
		materialMass.put(Material.WATER, 0D);
		materialMass.put(Material.WEB, 20D);
		materialMass.put(Material.WOOD, 150D);
	}

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

	public double getMassFromMaterial(Material material){
		Double mass = materialMass.get(material);
		if(mass == null){
			mass = defaultMass;
			materialMass.put(material, mass);
		}
		return mass;
	}

	public Double generateMassForBlock(Block block) {
		if (block instanceof BlockLiquid) {
			return 0D;
		}
		Material material = block.blockMaterial;

		return getMassFromMaterial(material);
		//Old formula
//		double hardness = block.blockHardness;
//		double resistance = block.blockResistance;
//		return hardness * 50D + 2 * Math.pow(resistance, 1 / 4);
	}



	public static void registerBlockMass(Block block, double mass) {
		basicMass.blockToMass.put(block, mass);
	}

}
