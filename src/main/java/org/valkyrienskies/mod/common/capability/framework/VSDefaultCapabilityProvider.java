package org.valkyrienskies.mod.common.capability.framework;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

@ParametersAreNonnullByDefault
public class VSDefaultCapabilityProvider<K> implements ICapabilitySerializable<NBTBase> {

    private final Capability<K> thisCapability;
    private final K inst;

    public VSDefaultCapabilityProvider(Capability<K> capability) {
        this.thisCapability = capability;
        this.inst = capability.getDefaultInstance();
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == thisCapability;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == thisCapability ? thisCapability.cast(inst) : null;
    }

    @Override
    public NBTBase serializeNBT() {
        return thisCapability.getStorage().writeNBT(thisCapability, inst, null);
    }

    @Override
    public void deserializeNBT(NBTBase nbt) {
        thisCapability.getStorage().readNBT(thisCapability, inst, null, nbt);
    }
}
