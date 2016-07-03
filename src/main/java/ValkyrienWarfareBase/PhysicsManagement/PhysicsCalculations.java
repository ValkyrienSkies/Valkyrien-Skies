package ValkyrienWarfareBase.PhysicsManagement;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

public class PhysicsCalculations {

	public PhysicsObject parent;
	
	public PhysicsCalculations(PhysicsObject toProcess){
		parent = toProcess;
	}
	
	public void onSetBlockState(IBlockState oldState,IBlockState newState,BlockPos posAt){
		
	}
}
