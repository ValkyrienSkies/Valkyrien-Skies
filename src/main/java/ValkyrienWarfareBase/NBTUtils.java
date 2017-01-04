package ValkyrienWarfareBase;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import ValkyrienWarfareBase.API.Vector;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class NBTUtils {

	public static final void writeBlockPosToNBT(String name, BlockPos pos, NBTTagCompound compound) {
		compound.setInteger(name + "X", pos.getX());
		compound.setInteger(name + "Y", pos.getY());
		compound.setInteger(name + "Z", pos.getZ());
	}

	public static final BlockPos readBlockPosFromNBT(String name, NBTTagCompound compound) {
		int x = compound.getInteger(name + "X");
		int y = compound.getInteger(name + "Y");
		int z = compound.getInteger(name + "Z");
		return new BlockPos(x, y, z);
	}

	public static final void writeBlockPosArrayListToNBT(String name, ArrayList<BlockPos> posArray, NBTTagCompound compound) {
		int[] xArray = new int[posArray.size()];
		int[] yArray = new int[posArray.size()];
		int[] zArray = new int[posArray.size()];
		for (int i = 0; i < posArray.size(); i++) {
			BlockPos pos = posArray.get(i);
			xArray[i] = pos.getX();
			yArray[i] = pos.getY();
			zArray[i] = pos.getZ();
		}
		compound.setIntArray(name + "xArray", xArray);
		compound.setIntArray(name + "yArray", yArray);
		compound.setIntArray(name + "zArray", zArray);
	}

	public static final ArrayList<BlockPos> readBlockPosArrayListFromNBT(String name, NBTTagCompound compound) {
		int[] xArray = compound.getIntArray(name + "xArray");
		int[] yArray = compound.getIntArray(name + "yArray");
		int[] zArray = compound.getIntArray(name + "zArray");
		ArrayList<BlockPos> posArray = new ArrayList<BlockPos>(xArray.length + 10);
		for (int i = 0; i < xArray.length; i++) {
			BlockPos pos = new BlockPos(xArray[i], yArray[i], zArray[i]);
			posArray.add(pos);
		}
		return posArray;
	}

	public static final void write3x3MatrixToNBT(String name, double[] matrix, NBTTagCompound compound) {
		for (int i = 0; i < 9; i++) {
			compound.setDouble(name + i, matrix[i]);
		}
	}

	public static final double[] read3x3MatrixFromNBT(String name, NBTTagCompound compound) {
		double[] matrix = new double[9];
		for (int i = 0; i < 9; i++) {
			matrix[i] = compound.getDouble(name + i);
		}
		return matrix;
	}

	public static final void writeVectorToNBT(String name, Vector vector, NBTTagCompound compound) {
		compound.setDouble(name + "X", vector.X);
		compound.setDouble(name + "Y", vector.Y);
		compound.setDouble(name + "Z", vector.Z);
	}

	public static final Vector readVectorFromNBT(String name, NBTTagCompound compound) {
		Vector vector = new Vector();
		vector.X = compound.getDouble(name + "X");
		vector.Y = compound.getDouble(name + "Y");
		vector.Z = compound.getDouble(name + "Z");
		return vector;
	}

	public static final void writeEntityPositionHashMapToNBT(String name, HashMap<Integer, Vector> entityLocalPositions, NBTTagCompound compound) {
		int[] entityIds = new int[entityLocalPositions.size()];
		double[] entityX = new double[entityLocalPositions.size()];
		double[] entityY = new double[entityLocalPositions.size()];
		double[] entityZ = new double[entityLocalPositions.size()];

		Iterator<Entry<Integer, Vector>> inputs = entityLocalPositions.entrySet().iterator();

		int cont = 0;
		while (inputs.hasNext()) {
			Entry<Integer, Vector> currentEntry = inputs.next();
			entityIds[cont] = currentEntry.getKey();
			Vector vec = currentEntry.getValue();
			entityX[cont] = vec.X;
			entityY[cont] = vec.Y;
			entityZ[cont] = vec.Z;
			cont++;
		}

		compound.setIntArray(name + "keys", entityIds);

		compound.setByteArray(name + "valX", toByteArray(entityX));
		compound.setByteArray(name + "valY", toByteArray(entityY));
		compound.setByteArray(name + "valZ", toByteArray(entityZ));
	}

	public static final HashMap<Integer, Vector> readEntityPositionMap(String name, NBTTagCompound compound) {
		int[] entityIds = compound.getIntArray(name + "keys");

		double[] entityX = toDoubleArray(compound.getByteArray(name + "valX"));
		double[] entityY = toDoubleArray(compound.getByteArray(name + "valY"));
		double[] entityZ = toDoubleArray(compound.getByteArray(name + "valZ"));

		HashMap<Integer, Vector> toReturn = new HashMap<Integer, Vector>(entityIds.length + 1);

		for (int i = 0; i < entityIds.length; i++) {
			toReturn.put(entityIds[i], new Vector(entityX[i], entityY[i], entityZ[i]));
		}

		return toReturn;
	}

	public static byte[] toByteArray(double[] doubleArray) {
		int times = Double.SIZE / Byte.SIZE;
		byte[] bytes = new byte[doubleArray.length * times];
		for (int i = 0; i < doubleArray.length; i++) {
			ByteBuffer.wrap(bytes, i * times, times).putDouble(doubleArray[i]);
		}
		return bytes;
	}

	public static double[] toDoubleArray(byte[] byteArray) {
		int times = Double.SIZE / Byte.SIZE;
		double[] doubles = new double[byteArray.length / times];
		for (int i = 0; i < doubles.length; i++) {
			doubles[i] = ByteBuffer.wrap(byteArray, i * times, times).getDouble();
		}
		return doubles;
	}

	public static byte[] toByteArray(int[] intArray) {
		int times = Integer.SIZE / Byte.SIZE;
		byte[] bytes = new byte[intArray.length * times];
		for (int i = 0; i < intArray.length; i++) {
			ByteBuffer.wrap(bytes, i * times, times).putInt(intArray[i]);
		}
		return bytes;
	}

	public static int[] toIntArray(byte[] byteArray) {
		int times = Integer.SIZE / Byte.SIZE;
		int[] doubles = new int[byteArray.length / times];
		for (int i = 0; i < doubles.length; i++) {
			doubles[i] = ByteBuffer.wrap(byteArray, i * times, times).getInt();
		}
		return doubles;
	}

}
