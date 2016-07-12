package ValkyrienWarfareBase.Physics;

import ValkyrienWarfareBase.Math.Vector;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IBlockForceProvider {

	//Multiply your power usage values or whatever by the secondsToApply, otherwise you'll have issues (Example: <0,400,0> -(.01)->  <0,40,0>
	Vector getBlockForce(World world,BlockPos pos,IBlockState state,double secondsToApply);
	
	boolean isForceLocalCoords(World world,BlockPos pos,IBlockState state,double secondsToApply);
	
}
