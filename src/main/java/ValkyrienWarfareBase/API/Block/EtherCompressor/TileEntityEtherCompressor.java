package ValkyrienWarfareBase.API.Block.EtherCompressor;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsObject;
import ValkyrienWarfareControl.Block.BlockHovercraftController;
import ValkyrienWarfareControl.NodeNetwork.BasicForceNodeTileEntity;
import ValkyrienWarfareControl.TileEntity.TileEntityHoverController;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public abstract class TileEntityEtherCompressor extends BasicForceNodeTileEntity {

	//TODO: This is all temporary
	private BlockPos controllerPos;
	public Vector linearThrust = new Vector();
	public Vector angularThrust = new Vector();

	public TileEntityEtherCompressor() {
		validate();
	}

	public TileEntityEtherCompressor(Vector normalForceVector, double power) {
		super(normalForceVector, false, power);
		validate();
	}

	public BlockPos getControllerPos() {
		return controllerPos;
	}

	public void setControllerPos(BlockPos toSet) {
		controllerPos = toSet;
		this.markDirty();
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		int controllerPosX = compound.getInteger("controllerPosX");
		int controllerPosY = compound.getInteger("controllerPosY");
		int controllerPosZ = compound.getInteger("controllerPosZ");
		controllerPos = new BlockPos(controllerPosX, controllerPosY, controllerPosZ);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagCompound toReturn = super.writeToNBT(compound);
		if(controllerPos != null){
			toReturn.setInteger("controllerPosX", controllerPos.getX());
			toReturn.setInteger("controllerPosY", controllerPos.getY());
			toReturn.setInteger("controllerPosZ", controllerPos.getZ());
		}
		return toReturn;
	}

	//TODO: Remove this as soon as you can!
	@Override
	public Vector getForceOutputUnoriented(double secondsToApply) {
		if(controllerPos == null){
			return new Vector();
		}

		TileEntity controllerTile = worldObj.getTileEntity(controllerPos);

		if(controllerTile != null){

			if(controllerTile instanceof TileEntityHoverController){
				TileEntityHoverController controller = (TileEntityHoverController) controllerTile;

				PhysicsObject physObj = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(worldObj, pos).wrapping;

				Vector notToReturn = controller.getForceForEngine(this, worldObj, getPos(), worldObj.getBlockState(pos), physObj, secondsToApply);

				this.currentThrust = notToReturn.length() / secondsToApply;

//				System.out.println(currentThrust);

			}
		}
		return super.getForceOutputUnoriented(secondsToApply);
	}

}
