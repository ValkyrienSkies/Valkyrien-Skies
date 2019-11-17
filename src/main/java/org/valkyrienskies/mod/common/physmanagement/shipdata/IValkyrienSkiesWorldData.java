package org.valkyrienskies.mod.common.physmanagement.shipdata;

import net.minecraft.nbt.NBTTagCompound;
import org.valkyrienskies.mod.common.physmanagement.chunk.ShipChunkAllocator;

/**
 * This is a capability attached to the world that provides the world's {@link ShipChunkAllocator}
 * and the world's {@link QueryableShipData}.
 */
public interface IValkyrienSkiesWorldData {

    NBTTagCompound writeToNBT();

    void readFromNBT(NBTTagCompound compound);

    ShipChunkAllocator getChunkAllocator();

    QueryableShipData getQueryableShipData();

}
