package ValkyrienWarfareBase.Physics;

import java.util.HashMap;

import ValkyrienWarfareBase.Math.Vector;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockForce {

	public static BlockForce basicForces = new BlockForce();
	
	public HashMap<Block,Force> blocksToForces = new HashMap<Block,Force>();
	
	public Force getForceFromState(IBlockState state,BlockPos pos,World world){
		Block block = state.getBlock();
		if(block instanceof IBlockForceProvider){
			Vector forceVector = ((IBlockForceProvider)block).getBlockForce(world, pos, state);
			boolean isInLocal = ((IBlockForceProvider)block).isForceLocalCoords(world, pos, state);
			Force toReturn = new Force(forceVector,isInLocal);
			return toReturn;
		}
		return basicForces.blocksToForces.get(block);
	}
	
	public boolean isBlockProvidingForce(IBlockState state,BlockPos pos,World world){
		Block block = state.getBlock();
		return basicForces.blocksToForces.containsKey(block) || block instanceof IBlockForceProvider;
	}
	
	public static void registerBlockForce(Block block,Vector forceVec,boolean isLocal){
		Force force = new Force(forceVec,isLocal);
		basicForces.blocksToForces.put(block, force);
	}
	
}
