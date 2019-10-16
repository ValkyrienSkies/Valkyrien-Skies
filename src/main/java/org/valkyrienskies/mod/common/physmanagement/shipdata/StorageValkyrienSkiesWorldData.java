package org.valkyrienskies.mod.common.physmanagement.shipdata;

import javax.annotation.Nullable;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public class StorageValkyrienSkiesWorldData implements
    Capability.IStorage<IValkyrienWorldDataCapability> {

    @Nullable
    @Override
    public NBTBase writeNBT(Capability<IValkyrienWorldDataCapability> capability,
        IValkyrienWorldDataCapability instance, EnumFacing side) {
        return instance.writeToNBT();
    }

    @Override
    public void readNBT(Capability<IValkyrienWorldDataCapability> capability,
        IValkyrienWorldDataCapability instance, EnumFacing side, NBTBase nbt) {
        instance.readFromNBT((NBTTagCompound) nbt);
    }
}
