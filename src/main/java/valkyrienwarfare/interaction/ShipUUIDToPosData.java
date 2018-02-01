/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2017 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package valkyrienwarfare.interaction;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import valkyrienwarfare.NBTUtils;
import valkyrienwarfare.api.RotationMatrices;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.physicsmanagement.PhysicsWrapperEntity;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

public class ShipUUIDToPosData extends WorldSavedData {

    private static String key = "ShipUUIDToPosData";

    private HashMap<Long, ShipPositionData> dataMap = new HashMap<Long, ShipPositionData>();

    public ShipUUIDToPosData(String name) {
        super(name);
    }

    public ShipUUIDToPosData() {
        super(key);
    }

    public static ShipUUIDToPosData get(World world) {
        MapStorage storage = world.getPerWorldStorage();
        ShipUUIDToPosData data = (ShipUUIDToPosData) storage.getOrLoadData(ShipUUIDToPosData.class, key);
        if (data == null) {
            data = new ShipUUIDToPosData();
            world.setData(key, data);
        }
        return data;
    }

    public ShipPositionData getShipPositionData(UUID shipID) {
        return dataMap.get(shipID.getMostSignificantBits());
    }

    public ShipPositionData getShipPositionData(long mostSigBits) {
        return dataMap.get(mostSigBits);
    }

    public void updateShipPosition(PhysicsWrapperEntity wrapper) {
        UUID entityID = wrapper.getPersistentID();
        long key = entityID.getMostSignificantBits();
        ShipPositionData data = dataMap.get(key);
        if (data != null) {
            data.updateData(wrapper);
        } else {
            data = new ShipPositionData(wrapper);
            dataMap.put(key, data);
        }
        markDirty();
    }

    public void removeShipFromMap(PhysicsWrapperEntity wrapper) {
        UUID entityID = wrapper.getPersistentID();
        dataMap.remove(entityID);
        markDirty();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        ByteBuffer buffer = NBTUtils.getByteBuf("ShipPositionByteBuf", nbt);

        while (buffer.hasRemaining()) {
            long mostBits = buffer.getLong();

            ShipPositionData data = new ShipPositionData(buffer);
            dataMap.put(mostBits, data);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        Set<Entry<Long, ShipPositionData>> entries = dataMap.entrySet();

        //Each ship has 19 floats, and 1 long; that comes out (19 * 4 + 1 * 8) = 84 bytes per ship
        int byteArraySize = entries.size() * 84;

        ByteBuffer buffer = ByteBuffer.allocate(byteArraySize);

        for (Entry<Long, ShipPositionData> entry : entries) {
            long mostBits = entry.getKey();

            ShipPositionData posData = entry.getValue();

            buffer.putLong(mostBits);
            posData.writeToByteBuffer(buffer);
        }

        NBTUtils.setByteBuf("ShipPositionByteBuf", buffer, compound);

        return compound;
    }

    public Set<Long> getShipHalfUUIDsSet() {
        return dataMap.keySet();
    }

    public class ShipPositionData {
        public Vector shipPosition;
        public float[] lToWTransform;

        public ShipPositionData(PhysicsWrapperEntity wrapper) {
            shipPosition = new Vector(wrapper.posX, wrapper.posY, wrapper.posZ);
            lToWTransform = RotationMatrices.convertToFloat(wrapper.wrapping.coordTransform.lToWTransform);
        }

        public ShipPositionData(ByteBuffer buffer) {
            shipPosition = new Vector(buffer.getFloat(), buffer.getFloat(), buffer.getFloat());

            lToWTransform = new float[16];
            for (int i = 0; i < 16; i++) {
                lToWTransform[i] = buffer.getFloat();
            }
        }

        public void writeToByteBuffer(ByteBuffer buffer) {
            buffer.putFloat((float) shipPosition.X);
            buffer.putFloat((float) shipPosition.Y);
            buffer.putFloat((float) shipPosition.Z);

            for (int i = 0; i < 16; i++) {
                buffer.putFloat(lToWTransform[i]);
            }
        }

        public void updateData(PhysicsWrapperEntity wrapper) {
            shipPosition.X = wrapper.posX;
            shipPosition.Y = wrapper.posY;
            shipPosition.Z = wrapper.posZ;
            lToWTransform = RotationMatrices.convertToFloat(wrapper.wrapping.coordTransform.lToWTransform);
        }
    }

}
