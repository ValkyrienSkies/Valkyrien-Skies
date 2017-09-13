package valkyrienwarfare;

import valkyrienwarfare.api.Vector;
import valkyrienwarfare.physics.BlockForce;
import valkyrienwarfare.physics.BlockMass;
import valkyrienwarfare.addon.control.ValkyrienWarfareControl;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

import java.util.ArrayList;

public class BlockPhysicsRegistration {
	
	public static BlockMass blockMass = BlockMass.basicMass;
	public static BlockForce blockForces = BlockForce.basicForces;
	public static ArrayList<Block> blocksToNotPhysicise = new ArrayList<>();
	
	public static void registerCustomBlockMasses() {
		BlockMass.registerBlockMass(Blocks.AIR, 0D);
		BlockMass.registerBlockMass(Blocks.FIRE, 0D);
		BlockMass.registerBlockMass(Blocks.FLOWING_WATER, 0D);
		BlockMass.registerBlockMass(Blocks.FLOWING_LAVA, 0D);
		BlockMass.registerBlockMass(Blocks.WATER, 0D);
		BlockMass.registerBlockMass(Blocks.LAVA, 0D);
		// blockMass.registerBlockMass(Blocks.WOOL, 10D);
		// blockMass.registerBlockMass(Blocks.PLANKS, 50D);
		// blockMass.registerBlockMass(Blocks.SAND, 120D);
		// blockMass.registerBlockMass(Blocks.COBBLESTONE, 180D);
		// blockMass.registerBlockMass(Blocks.STONE, 180D);
		// blockMass.registerBlockMass(Blocks.IRON_BLOCK, 250D);
		// blockMass.registerBlockMass(Blocks.OBSIDIAN, 500D);
		BlockMass.registerBlockMass(Blocks.BEDROCK, 5000D);
	}
	
	public static void registerVanillaBlockForces() {
		BlockForce.registerBlockForce(ValkyrienWarfareControl.INSTANCE.dopedEtherium, new Vector(0, 10000D, 0), false);
	}
	
	public static void registerBlocksToNotPhysicise() {
		blocksToNotPhysicise.add(Blocks.AIR);
		blocksToNotPhysicise.add(Blocks.WATER);
		blocksToNotPhysicise.add(Blocks.FLOWING_WATER);
		blocksToNotPhysicise.add(Blocks.LAVA);
		blocksToNotPhysicise.add(Blocks.FLOWING_LAVA);
	}
}
