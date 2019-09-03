package org.valkyrienskies.mod.common.physmanagement.shipdata;

import net.minecraft.nbt.NBTTagCompound;
import org.valkyrienskies.mod.common.physmanagement.chunk.ShipChunkAllocator;

public class ImplValkyrienSkiesWorldData implements IValkyrienSkiesWorldData {

    private final ShipChunkAllocator shipChunkAllocator;
    private final QueryableShipData queryableShipData;

    public ImplValkyrienSkiesWorldData() {
        shipChunkAllocator = new ShipChunkAllocator();
        queryableShipData = new QueryableShipData();
    }

    @Override
    public NBTTagCompound writeToNBT() {
        NBTTagCompound toReturn = new NBTTagCompound();
        shipChunkAllocator.writeToNBT(toReturn);
        queryableShipData.writeToNBT(toReturn);
        return toReturn;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        shipChunkAllocator.readFromNBT(compound);
        queryableShipData.readFromNBT(compound);
    }

    @Override
    public ShipChunkAllocator getChunkAllocator() {
        return shipChunkAllocator;
    }

    @Override
    public QueryableShipData getQueryableShipData() {
        return queryableShipData;
    }
}
