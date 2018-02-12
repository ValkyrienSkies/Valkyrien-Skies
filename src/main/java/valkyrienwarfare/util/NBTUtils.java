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

package valkyrienwarfare.util;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import valkyrienwarfare.api.Vector;

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

    public static final void writeBlockPosArrayListToNBT(String name, ArrayList<BlockPos> posArray,
            NBTTagCompound compound) {
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

    public static final void writeEntityPositionMapToNBT(String name, Map<Integer, Vector> entityLocalPositions,
            NBTTagCompound compound) {
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

    public static final Map<Integer, Vector> readEntityPositionMap(String name, NBTTagCompound compound) {
        int[] entityIds = compound.getIntArray(name + "keys");

        double[] entityX = toDoubleArray(compound.getByteArray(name + "valX"));
        double[] entityY = toDoubleArray(compound.getByteArray(name + "valY"));
        double[] entityZ = toDoubleArray(compound.getByteArray(name + "valZ"));

        Map<Integer, Vector> toReturn = new HashMap<Integer, Vector>(entityIds.length + 1);

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

    public static void setByteBuf(String name, ByteBuffer buffer, NBTTagCompound compound) {
        byte[] bytes = buffer.array();
        compound.setByteArray(name, bytes);
    }

    public static ByteBuffer getByteBuf(String name, NBTTagCompound compound) {
        byte[] bytes = compound.getByteArray(name);
        ByteBuffer toReturn = ByteBuffer.wrap(bytes);
        return toReturn;
    }

}
