package ValkyrienWarfareBase.API.Block.EtherCompressor;

import ValkyrienWarfareBase.API.IBlockForceProvider;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsObject;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class BlockEtherCompressor extends Block implements ITileEntityProvider, IBlockForceProvider {
	
	public double enginePower = 25000D;
	
	public BlockEtherCompressor(Material materialIn, double enginePower) {
		super(materialIn);
		this.enginePower = enginePower;
	}

	@Override
	public Vector getBlockForce(World world, BlockPos pos, IBlockState state, Entity shipEntity, double secondsToApply) {
		PhysicsWrapperEntity wrapper = (PhysicsWrapperEntity) shipEntity;
		PhysicsObject obj = wrapper.wrapping;
		IBlockState controllerState = obj.VKChunkCache.getBlockState(pos);
		TileEntity worldTile = obj.VKChunkCache.getTileEntity(pos);
		if (worldTile == null) {
			return null;
		}
		if (worldTile instanceof TileEntityEtherCompressor) {
			TileEntityEtherCompressor engineTile = (TileEntityEtherCompressor) worldTile;
			return engineTile.getForceOutput(world, pos, state, wrapper, secondsToApply);
		}
		return null;
	}

	@Override
	public boolean isForceLocalCoords(World world, BlockPos pos, IBlockState state, double secondsToApply) {
		return false;
	}

	@Override
	public Vector getBlockForcePosition(World world, BlockPos pos, IBlockState state, Entity shipEntity, double secondsToApply) {
		return null;
	}

}
