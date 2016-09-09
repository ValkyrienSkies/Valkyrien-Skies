package ValkyrienWarfareControl.TileEntity;

import ValkyrienWarfareBase.PhysicsManagement.PhysicsObject;
import ValkyrienWarfareControl.Balloon.BalloonProcessor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class BalloonBurnerTileEntity extends TileEntity{

	public PhysicsObject parentObject;
	public BalloonProcessor balloon;
	
	public BalloonBurnerTileEntity(){
		validate();
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