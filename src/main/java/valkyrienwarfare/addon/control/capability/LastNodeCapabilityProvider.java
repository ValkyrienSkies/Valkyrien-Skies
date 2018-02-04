package valkyrienwarfare.addon.control.capability;

import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import valkyrienwarfare.addon.control.ValkyrienWarfareControl;

public class LastNodeCapabilityProvider implements ICapabilitySerializable<NBTTagIntArray> {

    ICapabilityLastRelay inst = ValkyrienWarfareControl.lastRelayCapability.getDefaultInstance();

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == ValkyrienWarfareControl.lastRelayCapability;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        return capability == ValkyrienWarfareControl.lastRelayCapability ? ValkyrienWarfareControl.lastRelayCapability.<T>cast(inst) : null;
    }

    @Override
    public NBTTagIntArray serializeNBT() {
        return (NBTTagIntArray) ValkyrienWarfareControl.lastRelayCapability.getStorage().writeNBT(ValkyrienWarfareControl.lastRelayCapability, inst, null);
    }

    @Override
    public void deserializeNBT(NBTTagIntArray nbt) {
        ValkyrienWarfareControl.lastRelayCapability.getStorage().readNBT(ValkyrienWarfareControl.lastRelayCapability, inst, null, nbt);
    }

}
