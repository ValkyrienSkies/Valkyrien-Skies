package ValkyrienWarfareControl.TileEntity;

import ValkyrienWarfareBase.Math.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AntiGravEngineTileEntity extends TileEntity{

	public BlockPos controllerPos = BlockPos.ORIGIN;
	public boolean hasController = false;
	public double currentPower;
	public double maxPower = 1500D;
	public TileEntityHoverController controller;
	
	public AntiGravEngineTileEntity(){
		validate();
	}
	
	public Vector getForceOutput(World world, BlockPos pos, IBlockState state, PhysicsWrapperEntity shipEntity, double secondsToApply){
		if(controllerPos.equals(BlockPos.ORIGIN)){
			return null;
		}
		System.out.println("test");
		TileEntity tile = shipEntity.wrapping.surroundingWorldChunksCache.getTileEntity(controllerPos);
		controller = (TileEntityHoverController) tile;
		if(tile!=null){
			System.out.println("linked");
		}
		return null;
	}
	
	//TODO: Code way to add controllers
	public void setController(BlockPos newPos){
		controllerPos = newPos;
	}
	
	public void readFromNBT(NBTTagCompound compound){
		int controllerX = compound.getInteger("controllerPosX");
		int controllerY = compound.getInteger("controllerPosY");
		int controllerZ = compound.getInteger("controllerPosZ");
		hasController = compound.getBoolean("hasController");
		controllerPos = new BlockPos(controllerX,controllerY,controllerZ);
		maxPower = compound.getDouble("maxPower");
		currentPower = compound.getDouble("currentPower");
		super.readFromNBT(compound);
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound){
    	compound.setInteger("controllerPosX", controllerPos.getX());
    	compound.setInteger("controllerPosY", controllerPos.getY());
    	compound.setInteger("controllerPosZ", controllerPos.getZ());
    	compound.setBoolean("hasController", hasController);
    	compound.setDouble("maxPower", maxPower);
    	compound.setDouble("currentPower", currentPower);
        return super.writeToNBT(compound);
    }

}
