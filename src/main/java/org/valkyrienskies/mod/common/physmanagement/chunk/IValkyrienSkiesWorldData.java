package org.valkyrienskies.mod.common.physmanagement.chunk;

import net.minecraft.nbt.NBTTagCompound;
import org.valkyrienskies.mod.common.physmanagement.interaction.QueryableShipData;

public interface IValkyrienSkiesWorldData {

    NBTTagCompound writeToNBT();

    void readFromNBT(NBTTagCompound compound);

    ShipChunkAllocator getChunkAllocator();

    QueryableShipData getQueryableShipData();
}
