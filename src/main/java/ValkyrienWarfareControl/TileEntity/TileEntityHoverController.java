package ValkyrienWarfareControl.TileEntity;

import java.util.ArrayList;

import ValkyrienWarfareBase.NBTUtils;
import ValkyrienWarfareBase.Math.Vector;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class TileEntityHoverController extends TileEntity{

	public ArrayList<BlockPos> enginePositions = new ArrayList<BlockPos>();
	public double idealHeight;
	
	public Vector normalVector = new Vector(0D,1D,0D);
	
	public TileEntityHoverController(){
		validate();
	}
	
	public void readFromNBT(NBTTagCompound compound){
		NBTUtils.writeBlockPosArrayListToNBT("enginePositions", enginePositions, compound);
		NBTUtils.writeVectorToNBT("normalVector", normalVector, compound);
		idealHeight = compound.getDouble("idealHeight");
		super.readFromNBT(compound);
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound){
    	enginePositions = NBTUtils.readBlockPosArrayListFromNBT("enginePositions", compound);
    	normalVector = NBTUtils.readVectorFromNBT("normalVector", compound);
    	compound.setDouble("idealHeight", idealHeight);
    	return super.writeToNBT(compound);
    }
	
}
