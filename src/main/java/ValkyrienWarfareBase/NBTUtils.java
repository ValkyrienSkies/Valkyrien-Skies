package ValkyrienWarfareBase;

import ValkyrienWarfareBase.Math.Vector;
import net.minecraft.nbt.NBTTagCompound;

public class NBTUtils {

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
