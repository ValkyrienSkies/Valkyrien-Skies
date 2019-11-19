package org.valkyrienskies.mod.common.capability.framework;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

@ParametersAreNonnullByDefault
public class VSDefaultCapabilityProviderTransient<K> implements ICapabilityProvider {

    private final Capability<K> thisCapability;
    private final K inst;

    public VSDefaultCapabilityProviderTransient(Capability<K> capability) {
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
