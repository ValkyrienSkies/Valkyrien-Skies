package ValkyrienWarfareBase;

import java.util.ArrayList;

import ValkyrienWarfareBase.Math.Vector;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class NBTUtils {

	public static final void writeBlockPosToNBT(String name,BlockPos pos,NBTTagCompound compound){
		compound.setInteger(name+"X", pos.getX());
		compound.setInteger(name+"Y", pos.getY());
		compound.setInteger(name+"Z", pos.getZ());
	}
	
	public static final BlockPos readBlockPosFromNBT(String name,NBTTagCompound compound){
		int x = compound.getInteger(name+"X");
		int y = compound.getInteger(name+"Y");
		int z = compound.getInteger(name+"Z");
		return new BlockPos(x,y,z);
	}
	
	public static final void writeBlockPosArrayListToNBT(String name,ArrayList<BlockPos> posArray,NBTTagCompound compound){
		int[] xArray = new int[posArray.size()];
		int[] yArray = new int[posArray.size()];
		int[] zArray = new int[posArray.size()];
		for(int i=0;i<posArray.size();i++){
			BlockPos pos = posArray.get(i);
			xArray[i] = pos.getX();
			yArray[i] = pos.getY();
			zArray[i] = pos.getZ();
		}
		compound.setIntArray(name+"xArray", xArray);
		compound.setIntArray(name+"yArray", yArray);
		compound.setIntArray(name+"zArray", zArray);
	}
	
	public static final ArrayList<BlockPos> readBlockPosArrayListFromNBT(String name,NBTTagCompound compound){
		int[] xArray = compound.getIntArray(name+"xArray");
		int[] yArray = compound.getIntArray(name+"yArray");
		int[] zArray = compound.getIntArray(name+"zArray");
		ArrayList<BlockPos> posArray = new ArrayList<BlockPos>(xArray.length+10);
		for(int i=0;i<xArray.length;i++){
			BlockPos pos = new BlockPos(xArray[i],yArray[i],zArray[i]);
			posArray.add(pos);
		}
		return posArray;
	}
	
   	public static final void write3x3MatrixToNBT(String name,double[] matrix,NBTTagCompound compound){
   		for(int i=0;i<9;i++){
   			compound.setDouble(name+i, matrix[i]);
   		}
   	}
   	
   	public static final double[] read3x3MatrixFromNBT(String name,NBTTagCompound compound){
   		double[] matrix = new double[9];
   		for(int i=0;i<9;i++){
   			matrix[i] = compound.getDouble(name+i);
   		}
   		return matrix;
   	}
   	
   	public static final void writeVectorToNBT(String name,Vector vector,NBTTagCompound compound){
   		compound.setDouble(name+"X", vector.X);
   		compound.setDouble(name+"Y", vector.Y);
   		compound.setDouble(name+"Z", vector.Z);
   	}
   	
   	public static final Vector readVectorFromNBT(String name,NBTTagCompound compound){
   		Vector vector = new Vector();
   		vector.X = compound.getDouble(name+"X");
   		vector.Y = compound.getDouble(name+"Y");
   		vector.Z = compound.getDouble(name+"Z");
   		return vector;
   	}
}
