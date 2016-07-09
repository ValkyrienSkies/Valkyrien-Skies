package ValkyrienWarfareBase;

import ValkyrienWarfareBase.Physics.BlockForce;
import ValkyrienWarfareBase.Physics.BlockMass;
import net.minecraft.init.Blocks;

public class BlockPhysicsRegistration {
	
	public static BlockMass blockMass = BlockMass.basicMass;
	public static BlockForce blockForces = BlockForce.basicFoces;
	
	public static void registerVanillaBlocksMass(){
		blockMass.registerBlockMass(Blocks.AIR, 0D);
		blockMass.registerBlockMass(Blocks.PLANKS, 50D);
		blockMass.registerBlockMass(Blocks.IRON_BLOCK, 250D);
		blockMass.registerBlockMass(Blocks.BEDROCK, 2000D);
	}
	
}
