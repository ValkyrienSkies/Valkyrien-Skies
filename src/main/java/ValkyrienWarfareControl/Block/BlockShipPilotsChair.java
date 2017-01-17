package ValkyrienWarfareControl.Block;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareControl.TileEntity.PilotsChairTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.network.play.server.SPacketPlayerPosLook.EnumFlags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockShipPilotsChair extends Block implements ITileEntityProvider {

	public static final PropertyDirection FACING = PropertyDirection.create("facing");
	
	public BlockShipPilotsChair(Material materialIn) {
		super(materialIn);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!worldIn.isRemote) {
			PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(worldIn, pos);
			if (wrapper != null) {
				if (playerIn.getLowestRidingEntity() != wrapper.getLowestRidingEntity()) {
					Vector playerPos = new Vector(playerIn);

					wrapper.wrapping.coordTransform.fromLocalToGlobal(playerPos);

					playerIn.posX = playerPos.X;
					playerIn.posY = playerPos.Y;
					playerIn.posZ = playerPos.Z;

					playerIn.startRiding(wrapper);
					Vector localMountPos = getPlayerMountOffset(state, pos);
					wrapper.wrapping.fixEntity(playerIn, localMountPos);
					
					wrapper.wrapping.pilotingController.setPilotEntity((EntityPlayerMP) playerIn, false);
					
					wrapper.wrapping.coordTransform.fromGlobalToLocal(playerPos);

					playerIn.posX = playerPos.X;
					playerIn.posY = playerPos.Y;
					playerIn.posZ = playerPos.Z;

					return true;
				}
			}
		}

		return false;
	}

	public static double getChairYaw(IBlockState state, BlockPos pos){
		EnumFacing enumFace = state.getValue(BlockShipPilotsChair.FACING);

		double chairYaw = -enumFace.getHorizontalAngle() - 90;
		
		return chairYaw;
	} 

	private Vector getPlayerMountOffset(IBlockState state, BlockPos pos){
		EnumFacing facing = (EnumFacing)state.getValue(FACING);
		switch (facing){
	      case NORTH:
	    	  return new Vector(pos.getX() + .5D, pos.getY(), pos.getZ() + .6D);
	      case SOUTH:
	    	  return new Vector(pos.getX() + .5D, pos.getY(), pos.getZ() + .4D);
	      case WEST:
	    	  return new Vector(pos.getX() + .6D, pos.getY(), pos.getZ() + .5D);
	      case EAST:
	    	  return new Vector(pos.getX() + .4D, pos.getY(), pos.getZ() + .5D);
	      default: 
	    	  return new Vector(pos.getX() + .5D, pos.getY() + .5D, pos.getZ() + .5D);
	    }
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new PilotsChairTileEntity();
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		EnumFacing facing = placer.getHorizontalFacing().getOpposite();
		if (placer.isSneaking()) {
			facing = facing.getOpposite();
		}
		try{
			worldIn.setBlockState(pos, state.withProperty(FACING, facing), 2);
		}catch(Exception e){
			ValkyrienWarfareMod.VWLogger.log(Level.SEVERE, "Chair didn't place right");
		}
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { FACING });
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(FACING, EnumFacing.getFront(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		int i = ((EnumFacing) state.getValue(FACING)).getIndex();
		return i;
	}
	
	@Override
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

}