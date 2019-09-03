package org.valkyrienskies.mod.common.physmanagement.shipdata;

import net.minecraft.nbt.NBTTagCompound;
import org.valkyrienskies.mod.common.physmanagement.chunk.ShipChunkAllocator;

public interface IValkyrienSkiesWorldData {

    NBTTagCompound writeToNBT();

    void readFromNBT(NBTTagCompound compound);

    ShipChunkAllocator getChunkAllocator();

    QueryableShipData getQueryableShipData();

}
