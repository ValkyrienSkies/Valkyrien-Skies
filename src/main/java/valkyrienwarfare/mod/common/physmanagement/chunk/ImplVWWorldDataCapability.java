package valkyrienwarfare.mod.common.physmanagement.chunk;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ImplVWWorldDataCapability implements IVWWorldDataCapability {

    // The world this capability belongs to.
    private World world;
    private final ShipChunkAllocator shipChunkAllocator = new ShipChunkAllocator();

    @Override
    public NBTTagCompound writeToNBT() {
        NBTTagCompound toReturn = new NBTTagCompound();
        shipChunkAllocator.writeToNBT(toReturn);
        return toReturn;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        // System.out.println("Reading cap crap from disk!");
        shipChunkAllocator.readFromNBT(compound);
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
}
