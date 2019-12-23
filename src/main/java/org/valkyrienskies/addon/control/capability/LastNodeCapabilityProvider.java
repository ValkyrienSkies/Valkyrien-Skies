package org.valkyrienskies.addon.control.capability;

import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import org.valkyrienskies.addon.control.ValkyrienSkiesControl;

public class LastNodeCapabilityProvider implements ICapabilitySerializable<NBTTagIntArray> {

    private ICapabilityLastRelay inst = ValkyrienSkiesControl.lastRelayCapability
        .getDefaultInstance();

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == ValkyrienSkiesControl.lastRelayCapability;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        return capability == ValkyrienSkiesControl.lastRelayCapability
            ? ValkyrienSkiesControl.lastRelayCapability.cast(inst) : null;
    }

    @Override
    public NBTTagIntArray serializeNBT() {
        return (NBTTagIntArray) ValkyrienSkiesControl.lastRelayCapability.getStorage()
            .writeNBT(ValkyrienSkiesControl.lastRelayCapability, inst, null);
    }

    @Override
    public void deserializeNBT(NBTTagIntArray nbt) {
        ValkyrienSkiesControl.lastRelayCapability.getStorage()
            .readNBT(ValkyrienSkiesControl.lastRelayCapability, inst, null, nbt);
    }

}
