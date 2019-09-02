package org.valkyrienskies.mod.common.physmanagement.chunk;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.physmanagement.interaction.QueryableShipData;

public interface IVWWorldDataCapability {

    NBTTagCompound writeToNBT();

    void readFromNBT(NBTTagCompound compound);

    /**
     * Sets the world object of this capability.
     */
    IVWWorldDataCapability setWorld(World world);

    ShipChunkAllocator getChunkAllocator();

    QueryableShipData getQueryableShipData();
}
