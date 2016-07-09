package ValkyrienWarfareBase.Physics;

import ValkyrienWarfareBase.Math.Vector;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IBlockForceProvider {

	Vector getBlockForce(World world,BlockPos pos,IBlockState state);
	
	boolean isForceLocalCoords(World world,BlockPos pos,IBlockState state);
	
}
