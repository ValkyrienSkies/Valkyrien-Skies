package org.valkyrienskies.mod.common.capability.framework;

import javax.annotation.Nullable;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public class VSDefaultCapabilityStorage<K extends VSDefaultCapability> implements Capability.IStorage<K> {

    @Nullable
    @Override
    public NBTBase writeNBT(Capability<K> capability, K instance, EnumFacing side) {
        return instance.writeNBT(side);
    }

    @Override
    public void readNBT(Capability<K> capability, K instance, EnumFacing side, NBTBase nbt) {
        instance.readNBT(nbt, side);
    }
}
