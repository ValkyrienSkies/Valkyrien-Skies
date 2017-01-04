package ValkyrienWarfareBase.Physics;

import java.util.HashMap;

import ValkyrienWarfareBase.API.IBlockForceProvider;
import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsObject;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockForce {

	public static BlockForce basicForces = new BlockForce();

	public HashMap<Block, Force> blocksToForces = new HashMap<Block, Force>();

	public Vector getForceFromState(IBlockState state, BlockPos pos, World world, double secondsToApply, PhysicsObject obj) {
		Block block = state.getBlock();
		if (block instanceof IBlockForceProvider) {
			Vector forceVector = ((IBlockForceProvider) block).getBlockForce(world, pos, state, obj.wrapper, secondsToApply);
			if (forceVector == null) {
				return null;
			}
			boolean isInLocal = ((IBlockForceProvider) block).isForceLocalCoords(world, pos, state, secondsToApply);
			if (isInLocal) {
				RotationMatrices.applyTransform(obj.coordTransform.lToWRotation, forceVector);
			}
			return forceVector;
		}
		Force force = basicForces.blocksToForces.get(block);
		if (force != null) {
			return force.getProduct(secondsToApply);
		} else {
			return null;
		}
	}

	public void getForceFromState(IBlockState state, BlockPos pos, World world, double secondsToApply, PhysicsObject obj, Vector toSet) {
		Block block = state.getBlock();
		if (block instanceof IBlockForceProvider) {
			Vector forceVector = ((IBlockForceProvider) block).getBlockForce(world, pos, state, obj.wrapper, secondsToApply);
			if (forceVector == null) {
				toSet.zero();
				return;
			}
			boolean isInLocal = ((IBlockForceProvider) block).isForceLocalCoords(world, pos, state, secondsToApply);
			if (isInLocal) {
				RotationMatrices.applyTransform(obj.coordTransform.lToWRotation, forceVector);
			}
			toSet.X = forceVector.X;
			toSet.Y = forceVector.Y;
			toSet.Z = forceVector.Z;
			return;
		}
		Force force = basicForces.blocksToForces.get(block);
		if (force != null) {
			toSet.X = force.X * secondsToApply;
			toSet.Y = force.Y * secondsToApply;
			toSet.Z = force.Z * secondsToApply;
		} else {
			toSet.zero();
		}
	}

	public boolean isBlockProvidingForce(IBlockState state, BlockPos pos, World world) {
		Block block = state.getBlock();
		return basicForces.blocksToForces.containsKey(block) || block instanceof IBlockForceProvider;
	}

	public static void registerBlockForce(Block block, Vector forceVec, boolean isLocal) {
		Force force = new Force(forceVec.X, forceVec.Y, forceVec.Z, isLocal);
		basicForces.blocksToForces.put(block, force);
	}

}
