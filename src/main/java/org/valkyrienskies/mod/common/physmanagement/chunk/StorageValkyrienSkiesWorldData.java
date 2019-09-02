package org.valkyrienskies.mod.common.physmanagement.chunk;

import javax.annotation.Nullable;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public class StorageValkyrienSkiesWorldData implements
    Capability.IStorage<IValkyrienSkiesWorldData> {

    @Nullable
    @Override
    public NBTBase writeNBT(Capability<IValkyrienSkiesWorldData> capability,
        IValkyrienSkiesWorldData instance, EnumFacing side) {
        return instance.writeToNBT();
    }

    @Override
    public void readNBT(Capability<IValkyrienSkiesWorldData> capability,
        IValkyrienSkiesWorldData instance, EnumFacing side, NBTBase nbt) {
        instance.readFromNBT((NBTTagCompound) nbt);
    }
}
