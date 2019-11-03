package org.valkyrienskies.mod.common.capability.framework;

import javax.annotation.Nullable;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

/**
 * Does nothing, used for capabilities that aren't meant to be persistent.
 */
public class VSDefaultCapabilityTransientStorage<K> implements Capability.IStorage<K> {

    @Nullable
    @Override
    public NBTBase writeNBT(Capability<K> capability, K instance, EnumFacing side) {
        return null;
    }

    @Override
    public void readNBT(Capability<K> capability, K instance, EnumFacing side, NBTBase nbt) {}
}
