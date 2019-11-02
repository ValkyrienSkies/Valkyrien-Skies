package org.valkyrienskies.mod.common.capability.framework;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;

public class VSDefaultCapabilityProviderNoNBT<K> implements ICapabilityProvider {

    private final Capability<K> thisCapability;
    private final K inst;

    public VSDefaultCapabilityProviderNoNBT(Capability<K> capability) {
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
}
