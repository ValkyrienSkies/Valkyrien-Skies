package ValkyrienWarfareControl.Block;

import ValkyrienWarfareBase.Math.Vector;
import ValkyrienWarfareBase.Physics.IBlockForceProvider;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsObject;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareControl.TileEntity.AntiGravEngineTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockAntiGravEngine extends Block implements ITileEntityProvider,IBlockForceProvider{

	public BlockAntiGravEngine(Material materialIn) {
		super(materialIn);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new AntiGravEngineTileEntity();
	}

	@Override
	public Vector getBlockForce(World world, BlockPos pos, IBlockState state, Entity shipEntity, double secondsToApply) {
		PhysicsWrapperEntity wrapper = (PhysicsWrapperEntity) shipEntity;
		PhysicsObject obj = wrapper.wrapping;
		
		AntiGravEngineTileEntity engineTile = (AntiGravEngineTileEntity) obj.surroundingWorldChunksCache.getTileEntity(pos);
		if(engineTile==null){
			return null;
		}
		return engineTile.getForceOutput(world, pos, state, wrapper, secondsToApply);
	}

	@Override
	public boolean isForceLocalCoords(World world, BlockPos pos, IBlockState state, double secondsToApply) {
		return false;
	}

}
