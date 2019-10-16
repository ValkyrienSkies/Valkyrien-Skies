package org.valkyrienskies.mod.common.physmanagement.shipdata;

import net.minecraft.nbt.NBTTagCompound;
import org.valkyrienskies.mod.common.physmanagement.chunk.ShipChunkAllocator;

public class ImplValkyrienWorldDataCapability implements IValkyrienWorldDataCapability {

    private final ShipChunkAllocator shipChunkAllocator;

    public ImplValkyrienWorldDataCapability() {
        shipChunkAllocator = new ShipChunkAllocator();
    }

    @Override
    public NBTTagCompound writeToNBT() {
        NBTTagCompound toReturn = new NBTTagCompound();
        shipChunkAllocator.writeToNBT(toReturn);
        return toReturn;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        shipChunkAllocator.readFromNBT(compound);
    }

    @Override
    public ShipChunkAllocator getChunkAllocator() {
        return shipChunkAllocator;
    }

}
