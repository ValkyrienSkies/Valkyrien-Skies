package ValkyrienWarfareBase.PhysCollision;

import net.minecraft.block.state.IBlockState;

/**
 * Given the sets of inputs, this class decides which blocks should be rammed, and which blocks shouldn't
 * @author thebest108
 *
 */
public class BlockRammingManager {

	//If either block broke, only apply 20% of the collision
	public static double collisionImpulseAfterRamming = .20D;
	public static double minimumVelocityToApply = 3.0D;


	public static void processBlockRamming(double collisionSpeed, IBlockState inLocalState, IBlockState inWorldState, NestedBoolean didBlockBreakInShip, NestedBoolean didBlockBreakInWorld){
		if(Math.abs(collisionSpeed) > 3.0D){
			double shipBlockHardness = inLocalState.getBlock().blockResistance;//inLocalState.getBlockHardness(worldObj, inLocalPos);
			double worldBlockHardness = inWorldState.getBlock().blockResistance;//inWorldState.getBlockHardness(worldObj, inWorldPos);

			double hardnessRatio = Math.pow( worldBlockHardness / shipBlockHardness, Math.abs(collisionSpeed) / 2.5D);

			if(worldBlockHardness == -1){
				worldBlockHardness = 100D;
			}

			if(shipBlockHardness == -1){
				shipBlockHardness = 100D;
			}

			if(hardnessRatio < .01D){
				didBlockBreakInWorld.setValue(true);
			}
			if(hardnessRatio > 100D){
				didBlockBreakInShip.setValue(true);
			}

		}
	}


	public static final class NestedBoolean{

		private boolean value;

		public NestedBoolean(boolean value){
			this.value = value;
		}

		public boolean getValue(){
			return value;
		}

		public void setValue(boolean value){
			this.value = value;
		}
	}

}
