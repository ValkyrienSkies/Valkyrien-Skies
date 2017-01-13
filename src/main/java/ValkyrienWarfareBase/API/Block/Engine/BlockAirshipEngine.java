package ValkyrienWarfareBase.API.Block.Engine;

import ValkyrienWarfareBase.API.IBlockForceProvider;
import ValkyrienWarfareBase.API.Vector;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * All engines should extend this class, that way other kinds of engines can be made without making tons of new classes for them. Only engines that add new functionality should have their own class.
 */
public abstract class BlockAirshipEngine extends Block implements IBlockForceProvider {

	public double enginePower = 4000D;

	public static final PropertyDirection FACING = PropertyDirection.create("facing");

	public BlockAirshipEngine(Material materialIn, double enginePower) {
		super(materialIn);
		this.enginePower = enginePower;
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		EnumFacing facing = BlockPistonBase.getFacingFromEntity(pos, placer);
		if (placer.isSneaking()) {
			facing = facing.getOpposite();
		}
		worldIn.setBlockState(pos, state.withProperty(FACING, facing), 2);
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
	public Vector getBlockForce(World world, BlockPos pos, IBlockState state, Entity shipEntity, double secondsToApply) {
		EnumFacing enumfacing = (EnumFacing) state.getValue(FACING);
		Vector acting = new Vector(0, 0, 0);
		if (!world.isBlockPowered(pos)) {
			return acting;
		}
		double power = this.getEnginePower(world, pos, state, shipEntity) * secondsToApply;
		switch (enumfacing) {
		case DOWN:
			acting = new Vector(0, power, 0);
			break;
		case UP:
			acting = new Vector(0, -power, 0);
			break;
		case EAST:
			acting = new Vector(-power, 0, 0);
			break;
		case NORTH:
			acting = new Vector(0, 0, power);
			break;
		case WEST:
			acting = new Vector(power, 0, 0);
			break;
		case SOUTH:
			acting = new Vector(0, 0, -power);
			break;
		}
		return acting;
	}

	@Override
	public boolean isForceLocalCoords(World world, BlockPos pos, IBlockState state, double secondsToApply) {
		return true;
	}

	@Override
	public Vector getBlockForcePosition(World world, BlockPos pos, IBlockState state, Entity shipEntity, double secondsToApply) {
		return null;
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

	@Override
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
		return true;
	}

	/**
	 * Used for calculating force applied to the airship by an engine. Override this in your subclasses to make engines that are more dynamic than simply being faster engines.
	 * 
	 * @return
	 */
	public double getEnginePower(World world, BlockPos pos, IBlockState state, Entity shipEntity) {
		return this.enginePower;
	}

}
