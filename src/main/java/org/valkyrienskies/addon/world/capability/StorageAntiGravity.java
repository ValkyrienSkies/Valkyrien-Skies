package org.valkyrienskies.addon.world.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class StorageAntiGravity implements IStorage<ICapabilityAntiGravity> {
    @Override
    public NBTBase writeNBT(Capability<ICapabilityAntiGravity> capability,
        ICapabilityAntiGravity instance,
        EnumFacing side) {
        return new NBTTagDouble(instance.getAntiGravity());
    }

    @Override
    public void readNBT(Capability<ICapabilityAntiGravity> capability,
        ICapabilityAntiGravity instance, EnumFacing side,
        NBTBase nbt) {
        NBTTagDouble tagDouble = (NBTTagDouble) nbt;
        instance.setAntiGravity(tagDouble.getDouble());
    }

}
