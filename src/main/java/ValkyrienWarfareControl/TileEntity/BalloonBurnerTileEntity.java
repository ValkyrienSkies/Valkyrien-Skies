package ValkyrienWarfareControl.TileEntity;

import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareControl.Balloon.BalloonProcessor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BalloonBurnerTileEntity extends TileEntity{

	public BalloonProcessor balloon;
	
	public BalloonBurnerTileEntity(){
		validate();
	}
	
	public Vector getBlockForcePosition(World world, BlockPos pos, IBlockState state, Entity shipEntity, double secondsToApply) {
		if(balloon==null){
			PhysicsWrapperEntity wrapper = (PhysicsWrapperEntity) shipEntity;
			balloon = wrapper.wrapping.balloonManager.getProcessorAbovePos(pos);
		}else{
//			System.out.println("I've got a balloon!");
		}
		return null;
	}
	
	public Vector getBlockForce(World world, BlockPos pos, IBlockState state, Entity shipEntity, double secondsToApply) {
		return null;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound){
		super.readFromNBT(compound);
    }

	@Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound){
        return super.writeToNBT(compound);
    }
	
}