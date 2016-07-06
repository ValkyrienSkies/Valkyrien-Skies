package ValkyrienWarfareBase;

import ValkyrienWarfareBase.Physics.BlockMass;
import net.minecraft.init.Blocks;

public class BlockMassRegistration {
	
	public static BlockMass blockMass = BlockMass.basicMass;
	
	public static void registerVanillaBlocks(){
		blockMass.registerBlockMass(Blocks.AIR, 0D);
		blockMass.registerBlockMass(Blocks.PLANKS, 50D);
		blockMass.registerBlockMass(Blocks.IRON_BLOCK, 250D);
		blockMass.registerBlockMass(Blocks.BEDROCK, 2000D);
	}
	
}
