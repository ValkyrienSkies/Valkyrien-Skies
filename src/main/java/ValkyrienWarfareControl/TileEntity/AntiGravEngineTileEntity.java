package ValkyrienWarfareControl.TileEntity;

import ValkyrienWarfareBase.NBTUtils;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareControl.Block.BlockHovercraftController;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AntiGravEngineTileEntity extends TileEntity {

	public BlockPos controllerPos = BlockPos.ORIGIN;
	public Vector angularThrust = new Vector();
	public Vector linearThrust = new Vector();
	public double maxThrust = 25000D;

	private double idealY;

	public AntiGravEngineTileEntity() {
		validate();
	}

	public Vector getForceOutput(World world, BlockPos pos, IBlockState state, PhysicsWrapperEntity shipEntity, double secondsToApply) {
		if (controllerPos.equals(BlockPos.ORIGIN)) {
			return null;
		}
		if (!shipEntity.wrapping.ownsChunk(controllerPos.getX() >> 4, controllerPos.getZ() >> 4)) {
			return null;
		}
		IBlockState controllerState = shipEntity.wrapping.VKChunkCache.getBlockState(controllerPos);
		TileEntity tileEnt = shipEntity.wrapping.VKChunkCache.getTileEntity(controllerPos);
		if (!(controllerState.getBlock() instanceof BlockHovercraftController)) {
			if (tileEnt instanceof TileEntityHoverController) {
				tileEnt.invalidate();
			}
			return null;
		}
		if (!(tileEnt instanceof TileEntityHoverController)) {
			return null;
		}
		TileEntityHoverController controller = (TileEntityHoverController) tileEnt;
		return controller.getForceForEngine(this, world, pos, state, shipEntity.wrapping, secondsToApply);
	}

	public void setController(BlockPos newPos) {
		controllerPos = newPos;
		markDirty();
	}

	public void readFromNBT(NBTTagCompound compound) {
		linearThrust = NBTUtils.readVectorFromNBT("linearThrust", compound);
		angularThrust = NBTUtils.readVectorFromNBT("angularThrust", compound);
		controllerPos = NBTUtils.readBlockPosFromNBT("controllerPos", compound);
		maxThrust = compound.getDouble("maxThrust");
		super.readFromNBT(compound);
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTUtils.writeVectorToNBT("linearThrust", linearThrust, compound);
		NBTUtils.writeVectorToNBT("angularThrust", angularThrust, compound);
		NBTUtils.writeBlockPosToNBT("controllerPos", controllerPos, compound);
		compound.setDouble("maxThrust", maxThrust);
		return super.writeToNBT(compound);
	}

}
