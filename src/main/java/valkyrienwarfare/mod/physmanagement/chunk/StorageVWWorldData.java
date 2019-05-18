package valkyrienwarfare.mod.physmanagement.chunk;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class StorageVWWorldData implements Capability.IStorage<IVWWorldDataCapability> {
    @Nullable
    @Override
    public NBTBase writeNBT(Capability<IVWWorldDataCapability> capability, IVWWorldDataCapability instance, EnumFacing side) {
        return instance.writeToNBT();
    }

    @Override
    public void readNBT(Capability<IVWWorldDataCapability> capability, IVWWorldDataCapability instance, EnumFacing side, NBTBase nbt) {
        instance.readFromNBT((NBTTagCompound) nbt);
    }
}
