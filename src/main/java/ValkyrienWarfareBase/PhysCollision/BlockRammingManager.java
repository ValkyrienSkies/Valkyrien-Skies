package ValkyrienWarfareBase.PhysCollision;

import ValkyrienWarfareBase.Physics.BlockMass;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

/**
 * Given the sets of inputs, this class decides which blocks should be rammed, and which blocks shouldn't
 * @author thebest108
 *
 */
public class BlockRammingManager {

	//If either block broke, only apply 20% of the collision
	public static double collisionImpulseAfterRamming = .20D;
	public static double minimumVelocityToApply = 3.0D;


	/**
	 * Returns percentage of power to apply collision
	 * @param collisionSpeed
	 * @param inLocalState
	 * @param inWorldState
	 * @param didBlockBreakInShip
	 * @param didBlockBreakInWorld
	 * @return
	 */
	public static double processBlockRamming(PhysicsWrapperEntity wrapper, double collisionSpeed, IBlockState inLocalState, IBlockState inWorldState, BlockPos inLocal, BlockPos inWorld, NestedBoolean didBlockBreakInShip, NestedBoolean didBlockBreakInWorld){
		if(Math.abs(collisionSpeed) > 4.0D){
			double shipBlockHardness = inLocalState.getBlock().blockResistance;//inLocalState.getBlockHardness(worldObj, inLocalPos);
			double worldBlockHardness = inWorldState.getBlock().blockResistance;//inWorldState.getBlockHardness(worldObj, inWorldPos);

			double hardnessRatio = Math.pow( worldBlockHardness / shipBlockHardness, Math.abs(collisionSpeed) / 2.5D);

			if(worldBlockHardness == -1){
				worldBlockHardness = 100D;
			}

			if(shipBlockHardness == -1){
				shipBlockHardness = 100D;
			}

			double arbitraryScale = 2.4D;

			if(hardnessRatio < .01D){
				didBlockBreakInWorld.setValue(true);
				double shipBlockMass = BlockMass.basicMass.getMassFromState(inLocalState, inLocal, wrapper.worldObj);
				double worldBlockMass = BlockMass.basicMass.getMassFromState(inWorldState, inWorld, wrapper.worldObj);
//				return worldBlockMass / shipBlockMass;
				return Math.pow(worldBlockMass / worldBlockMass, arbitraryScale);//wrapper.wrapping.physicsProcessor.mass;
			}
			if(hardnessRatio > 100D){
				didBlockBreakInShip.setValue(true);
				double shipBlockMass = BlockMass.basicMass.getMassFromState(inLocalState, inLocal, wrapper.worldObj);
				double worldBlockMass = BlockMass.basicMass.getMassFromState(inWorldState, inWorld, wrapper.worldObj);
//				return shipBlockMass / worldBlockMass;
				return Math.pow(shipBlockMass / worldBlockMass, arbitraryScale);//wrapper.wrapping.physicsProcessor.mass;
			}

		}

		return 1;
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
