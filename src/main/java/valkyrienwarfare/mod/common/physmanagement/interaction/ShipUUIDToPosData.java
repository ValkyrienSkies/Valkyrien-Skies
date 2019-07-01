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

package valkyrienwarfare.mod.common.physmanagement.interaction;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import valkyrienwarfare.api.TransformType;
import valkyrienwarfare.mod.common.math.RotationMatrices;
import valkyrienwarfare.mod.common.math.Vector;
import valkyrienwarfare.mod.common.physics.management.PhysicsWrapperEntity;
import valkyrienwarfare.mod.common.util.NBTUtils;

import java.nio.ByteBuffer;
import java.util.UUID;

public class ShipUUIDToPosData extends WorldSavedData {

    public static final String SHIP_UUID_TO_POS_DATA_KEY = "ShipUUIDToPosData";
    private final TLongObjectMap<ShipPositionData> dataMap = new TLongObjectHashMap<>();

    public ShipUUIDToPosData(String name) {
        super(name);
    }

    public ShipUUIDToPosData() {
        this(SHIP_UUID_TO_POS_DATA_KEY);
    }

    public static ShipUUIDToPosData getShipUUIDDataForWorld(World world) {
        MapStorage storage = world.getPerWorldStorage();
        ShipUUIDToPosData data = (ShipUUIDToPosData) storage.getOrLoadData(ShipUUIDToPosData.class, SHIP_UUID_TO_POS_DATA_KEY);
        if (data == null) {
            data = new ShipUUIDToPosData();
            world.setData(SHIP_UUID_TO_POS_DATA_KEY, data);
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
        // TODO: Check if this is safe / correct.
        dataMap.remove(entityID.getMostSignificantBits());
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
        // Each ship has 19 floats, and 1 long; that comes out (19 * 4 + 1 * 8) = 84
        // bytes per ship
        int byteArraySize = dataMap.size() * 84;
        ByteBuffer buffer = ByteBuffer.allocate(byteArraySize);
        dataMap.forEachEntry((k, v) -> {
            buffer.putLong(k);
            v.writeToByteBuffer(buffer);
            return true;
        });
        NBTUtils.setByteBuf("ShipPositionByteBuf", buffer, compound);
        return compound;
    }

    public class ShipPositionData {

        private final Vector shipPosition;
        private final float[] lToWTransform;

        private ShipPositionData(PhysicsWrapperEntity wrapper) {
            shipPosition = new Vector(wrapper.posX, wrapper.posY, wrapper.posZ);
            lToWTransform = RotationMatrices.convertToFloat(wrapper.getPhysicsObject().getShipTransformationManager().getCurrentTickTransform().getInternalMatrix(TransformType.SUBSPACE_TO_GLOBAL));
        }

        private ShipPositionData(ByteBuffer buffer) {
            shipPosition = new Vector(buffer.getFloat(), buffer.getFloat(), buffer.getFloat());
            lToWTransform = new float[16];
            for (int i = 0; i < 16; i++) {
                lToWTransform[i] = buffer.getFloat();
            }
        }

        private void writeToByteBuffer(ByteBuffer buffer) {
            buffer.putFloat((float) shipPosition.X);
            buffer.putFloat((float) shipPosition.Y);
            buffer.putFloat((float) shipPosition.Z);
            for (int i = 0; i < 16; i++) {
                buffer.putFloat(lToWTransform[i]);
            }
        }

        private void updateData(PhysicsWrapperEntity wrapper) {
            shipPosition.X = wrapper.posX;
            shipPosition.Y = wrapper.posY;
            shipPosition.Z = wrapper.posZ;
            RotationMatrices.convertToFloat(wrapper.getPhysicsObject().getShipTransformationManager().getCurrentTickTransform().getInternalMatrix(TransformType.SUBSPACE_TO_GLOBAL), lToWTransform);
        }

        public double getPosX() {
            return shipPosition.X;
        }

        public double getPosY() {
            return shipPosition.Y;
        }

        public double getPosZ() {
            return shipPosition.Z;
        }

        // Returns a copy of of the lToWTransform as a double array.
        public double[] getLToWTransform() {
            return RotationMatrices.convertToDouble(lToWTransform);
        }
    }

}
