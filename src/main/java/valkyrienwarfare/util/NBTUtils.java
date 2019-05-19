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

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import valkyrienwarfare.api.TransformType;
import valkyrienwarfare.math.Vector;
import valkyrienwarfare.mod.coordinates.ShipTransform;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * NBTUtils is filled with helper methods for saving and loading different
 * objects from NBTTagCompound.
 *
 * @author thebest108
 */
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
        ArrayList<BlockPos> posArray = new ArrayList<>(xArray.length + 10);
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

    public static final void writeEntityPositionMapToNBT(String name, TIntObjectMap<Vector> entityLocalPositions,
                                                         NBTTagCompound compound) {
        int[] ids = new int[entityLocalPositions.size()];
        double[] x = new double[entityLocalPositions.size()];
        double[] y = new double[entityLocalPositions.size()];
        double[] z = new double[entityLocalPositions.size()];

        AtomicInteger cont = new AtomicInteger(0);
        entityLocalPositions.forEachEntry((k, v) -> {
            int i = cont.getAndIncrement();
            ids[i] = k;
            x[i] = v.X;
            y[i] = v.Y;
            z[i] = v.Z;
            return true;
        });

        compound.setIntArray(name + "keys", ids);
        compound.setByteArray(name + "valX", toByteArray(x));
        compound.setByteArray(name + "valY", toByteArray(y));
        compound.setByteArray(name + "valZ", toByteArray(z));
    }

    public static final TIntObjectMap<Vector> readEntityPositionMap(String name, NBTTagCompound compound) {
        int[] entityIds = compound.getIntArray(name + "keys");

        double[] entityX = toDoubleArray(compound.getByteArray(name + "valX"));
        double[] entityY = toDoubleArray(compound.getByteArray(name + "valY"));
        double[] entityZ = toDoubleArray(compound.getByteArray(name + "valZ"));

        TIntObjectMap<Vector> toReturn = new TIntObjectHashMap<>(entityIds.length + 1);

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

    public static void setByteBuf(String name, ByteBuffer buffer, NBTTagCompound compound) {
        byte[] bytes = buffer.array();
        compound.setByteArray(name, bytes);
    }

    public static ByteBuffer getByteBuf(String name, NBTTagCompound compound) {
        return ByteBuffer.wrap(compound.getByteArray(name));
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
        AxisAlignedBB aabb = new AxisAlignedBB(compound.getDouble(name + "minX"), compound.getDouble(name + "minY"),
                compound.getDouble(name + "minZ"), compound.getDouble(name + "maxX"), compound.getDouble(name + "maxY"),
                compound.getDouble(name + "maxZ"));
        return aabb;
    }

    public static void writeShipTransformToNBT(String name, ShipTransform shipTransform, NBTTagCompound compound) {
        double[] localToGlobalInternalArray = shipTransform.getInternalMatrix(TransformType.SUBSPACE_TO_GLOBAL);
        byte[] localToGlobalAsBytes = toByteArray(localToGlobalInternalArray);
        compound.setByteArray("vw_ST_" + name, localToGlobalAsBytes);
    }

    /**
     * @param name
     * @param compound
     * @return Returns null if there was an error loading the ShipTransform.
     * Otherwise the proper ShipTransform is returned.
     */
    @Nullable
    public static ShipTransform readShipTransformFromNBT(String name, NBTTagCompound compound) {
        byte[] localToGlobalAsBytes = compound.getByteArray("vw_ST_" + name);
        if (localToGlobalAsBytes.length == 0) {
            System.err.println(
                    "Loading from the ShipTransform has failed, now we are forced to fallback on Vanilla MC positions. This probably won't go well at all!");
            return null;
        }
        double[] localToGlobalInternalArray = toDoubleArray(localToGlobalAsBytes);
        return new ShipTransform(localToGlobalInternalArray);
    }

}
