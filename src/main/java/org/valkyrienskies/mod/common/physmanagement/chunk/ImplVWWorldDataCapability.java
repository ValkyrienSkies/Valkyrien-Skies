package org.valkyrienskies.mod.common.physmanagement.chunk;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.physmanagement.interaction.QueryableShipData;

public class ImplVWWorldDataCapability implements IVWWorldDataCapability {

    // The world this capability belongs to.
    private World world;
    private final ShipChunkAllocator shipChunkAllocator;
    private final QueryableShipData queryableShipData;

    public ImplVWWorldDataCapability() {
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
    public IVWWorldDataCapability setWorld(World world) {
        this.world = world;
        return this;
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
