package ValkyrienWarfareBase.Physics;

import java.util.HashMap;

import ValkyrienWarfareBase.Math.RotationMatrices;
import ValkyrienWarfareBase.Math.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsObject;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockForce {

	public static BlockForce basicForces = new BlockForce();
	
	public HashMap<Block,Force> blocksToForces = new HashMap<Block,Force>();
	
	public Vector getForceFromState(IBlockState state,BlockPos pos,World world,double secondsToApply,PhysicsObject obj){
		Block block = state.getBlock();
		if(block instanceof IBlockForceProvider){
			Vector forceVector = ((IBlockForceProvider)block).getBlockForce(world, pos, state, obj.wrapper, secondsToApply);
			if(forceVector==null){
				return null;
			}
			boolean isInLocal = ((IBlockForceProvider)block).isForceLocalCoords(world, pos, state,secondsToApply);
			if(isInLocal){
				RotationMatrices.applyTransform(obj.coordTransform.lToWRotation, forceVector);
			}
			return forceVector;
		}
		Force force = basicForces.blocksToForces.get(block);
		if(force!=null){
			return force.getProduct(secondsToApply);
		}else{
			return null;
		}
	}
	
	public boolean isBlockProvidingForce(IBlockState state,BlockPos pos,World world){
		Block block = state.getBlock();
		return basicForces.blocksToForces.containsKey(block) || block instanceof IBlockForceProvider;
	}
	
	public static void registerBlockForce(Block block,Vector forceVec,boolean isLocal){
		Force force = new Force(forceVec.X,forceVec.Y,forceVec.Z,isLocal);
		basicForces.blocksToForces.put(block, force);
	}
	
}
