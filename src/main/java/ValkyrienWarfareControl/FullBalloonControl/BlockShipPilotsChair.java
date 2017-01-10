package ValkyrienWarfareControl.FullBalloonControl;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockShipPilotsChair extends Block implements ITileEntityProvider {

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
					Vector localMountPos = new Vector(pos.getX() + .5D, pos.getY() + .5D, pos.getZ() + .5D);
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

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new PilotsChairTileEntity();
	}

}
