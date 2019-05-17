package valkyrienwarfare.fixes;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ImplVWWorldDataCapability implements IVWWorldDataCapability {

    private World world;

    @Override
    public NBTTagCompound writeToNBT() {
        NBTTagCompound toReturn = new NBTTagCompound();
        // TODO: Actually write something here
        return toReturn;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        // System.out.println("Reading cap crap from disk!");
    }

    @Override
    public IVWWorldDataCapability setWorld(World world) {
        this.world = world;
        return this;
    }
}
