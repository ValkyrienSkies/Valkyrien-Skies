package org.valkyrienskies.mod.common.util;

import java.nio.ByteBuffer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.joml.Matrix3d;
import org.joml.Matrix3dc;
import org.valkyrienskies.mod.common.math.Vector;

/**
 * ValkyrienNBTUtils is filled with helper methods for saving and loading different objects from
 * NBTTagCompound.
 *
 * @author thebest108
 */
public class ValkyrienNBTUtils {

    public static void writeBlockPosToNBT(String name, BlockPos pos,
        NBTTagCompound compound) {
        compound.setInteger(name + "X", pos.getX());
        compound.setInteger(name + "Y", pos.getY());
        compound.setInteger(name + "Z", pos.getZ());
    }

    public static BlockPos readBlockPosFromNBT(String name, NBTTagCompound compound) {
        int x = compound.getInteger(name + "X");
        int y = compound.getInteger(name + "Y");
        int z = compound.getInteger(name + "Z");
        return new BlockPos(x, y, z);
    }

    public static void write3x3MatrixToNBT(String name, Matrix3dc matrix3,
        NBTTagCompound compound) {
        double[] matrix = new double[9];
        matrix3.get(matrix);
        for (int i = 0; i < 9; i++) {
            compound.setDouble(name + i, matrix[i]);
        }
    }

    public static Matrix3dc read3x3MatrixFromNBT(String name, NBTTagCompound compound) {
        double[] matrix = new double[9];
        for (int i = 0; i < 9; i++) {
            matrix[i] = compound.getDouble(name + i);
        }
        return new Matrix3d().set(matrix);
    }

    public static void writeVectorToNBT(String name, Vector vector, NBTTagCompound compound) {
        compound.setDouble(name + "X", vector.x);
        compound.setDouble(name + "Y", vector.y);
        compound.setDouble(name + "Z", vector.z);
    }

    public static Vector readVectorFromNBT(String name, NBTTagCompound compound) {
        Vector vector = new Vector();
        vector.x = compound.getDouble(name + "X");
        vector.y = compound.getDouble(name + "Y");
        vector.z = compound.getDouble(name + "Z");
        return vector;
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

    public static void writeAABBToNBT(String name, AxisAlignedBB aabb, NBTTagCompound compound) {
        compound.setDouble(name + "minX", aabb.minX);
        compound.setDouble(name + "minY", aabb.minY);
        compound.setDouble(name + "minZ", aabb.minZ);
        compound.setDouble(name + "maxX", aabb.maxX);
        compound.setDouble(name + "maxY", aabb.maxY);
        compound.setDouble(name + "maxZ", aabb.maxZ);
    }

    public static AxisAlignedBB readAABBFromNBT(String name, NBTTagCompound compound) {
        AxisAlignedBB aabb = new AxisAlignedBB(compound.getDouble(name + "minX"),
            compound.getDouble(name + "minY"),
            compound.getDouble(name + "minZ"), compound.getDouble(name + "maxX"),
            compound.getDouble(name + "maxY"),
            compound.getDouble(name + "maxZ"));
        return aabb;
    }

}
