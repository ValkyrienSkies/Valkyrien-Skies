package ValkyrienWarfareControl.Block;

import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.PhysicsManagement.ShipType;
import ValkyrienWarfareBase.Relocation.DetectorManager;
import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareControl.TileEntity.TileEntityZepplinController;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockAirshipController_Zepplin extends Block implements ITileEntityProvider {

	public static final PropertyDirection FACING = BlockHorizontal.FACING;

	public BlockAirshipController_Zepplin(Material materialIn) {
		super(materialIn);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!worldIn.isRemote) {
			PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(worldIn, pos);
//			if(wrapper.wrapping.shipType != )
			TileEntityZepplinController tileEnt = (TileEntityZepplinController) worldIn.getTileEntity(pos);
			if (wrapper != null) {
				//Disassemble Ship
				if (tileEnt != null) {
					tileEnt.setPilotEntity(playerIn);
				}
			} else {
				PhysicsWrapperEntity newEntity = new PhysicsWrapperEntity(worldIn, pos.getX(), pos.getY(), pos.getZ(), playerIn, DetectorManager.DetectorIDs.ShipSpawnerGeneral.ordinal(), ShipType.Zepplin);


				//NO
//				newEntity.wrapping.pilotingController.setPilotEntity((EntityPlayerMP) playerIn, true);

//				newEntity.wrapping.doPhysics = true;

				worldIn.spawnEntity(newEntity);

//				SetZepplinPilotMessage message = new SetZepplinPilotMessage(newEntity, playerIn);
				//TODO: Change this
//				ValkyrienWarfareControlMod.INSTANCE.controlNetwork.sendToAll(message);
				//Create Ship
			}
			return true;
		}
		return false;
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		EnumFacing facingHorizontal = placer.getHorizontalFacing();

		if (!placer.isSneaking()) {
			facingHorizontal = facingHorizontal.getOpposite();
		}

		return this.getDefaultState().withProperty(FACING, facingHorizontal);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		EnumFacing enumfacing = EnumFacing.getFront(meta);
		if (enumfacing.getAxis() == EnumFacing.Axis.Y) {
			enumfacing = EnumFacing.NORTH;
		}
		return this.getDefaultState().withProperty(FACING, enumfacing);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		int i = state.getValue(FACING).getIndex();
		return i;
	}

	@Override
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.SOLID;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return true;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityZepplinController();
	}

}
