/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2018 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package org.valkyrienskies.mod.common.util;

import java.nio.ByteBuffer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.valkyrienskies.mod.common.math.Vector;

/**
 * ValkyrienNBTUtils is filled with helper methods for saving and loading different objects from
 * NBTTagCompound.
 *
 * @author thebest108
 */
public class ValkyrienNBTUtils {

    public static final void writeBlockPosToNBT(String name, BlockPos pos,
        NBTTagCompound compound) {
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

    public static final void write3x3MatrixToNBT(String name, double[] matrix,
        NBTTagCompound compound) {
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
