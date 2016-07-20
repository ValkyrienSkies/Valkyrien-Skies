package ValkyrienWarfareControl.TileEntity;

import ValkyrienWarfareBase.NBTUtils;
import ValkyrienWarfareBase.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AntiGravEngineTileEntity extends TileEntity{

	public BlockPos controllerPos = BlockPos.ORIGIN;
	public Vector angularThrust = new Vector();
	public Vector linearThrust = new Vector();
	public double maxThrust = 5000D;
	public TileEntityHoverController controller;
	
	private double idealY;
	
	public AntiGravEngineTileEntity(){
		validate();
	}
	
	public Vector getForceOutput(World world, BlockPos pos, IBlockState state, PhysicsWrapperEntity shipEntity, double secondsToApply){
		if(controllerPos.equals(BlockPos.ORIGIN)){
			return null;
		}
		controller = (TileEntityHoverController) shipEntity.wrapping.VKChunkCache.getTileEntity(controllerPos);
		if(controller!=null){
			return controller.getForceForEngine(this,world,pos,state,shipEntity.wrapping,secondsToApply);
		}
		return null;
	}
	
	public void setController(BlockPos newPos){
		controllerPos = newPos;
	}
	
	public void readFromNBT(NBTTagCompound compound){
		linearThrust = NBTUtils.readVectorFromNBT("linearThrust", compound);
		angularThrust = NBTUtils.readVectorFromNBT("angularThrust", compound);
		controllerPos = NBTUtils.readBlockPosFromNBT("controllerPos", compound);
		maxThrust = compound.getDouble("maxThrust");
		super.readFromNBT(compound);
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound){
    	NBTUtils.writeVectorToNBT("linearThrust", linearThrust, compound);
    	NBTUtils.writeVectorToNBT("angularThrust", angularThrust, compound);
    	NBTUtils.writeBlockPosToNBT("controllerPos", controllerPos, compound);
    	compound.setDouble("maxThrust", maxThrust);
        return super.writeToNBT(compound);
    }

}
