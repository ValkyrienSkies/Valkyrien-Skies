package org.valkyrienskies.addon.world.capability;

import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import org.valkyrienskies.addon.world.ValkyrienSkiesWorld;

public class AntiGravityCapabilityProvider implements ICapabilitySerializable<NBTTagDouble> {

    private ICapabilityAntiGravity inst = ValkyrienSkiesWorld.ANTI_GRAVITY_CAPABILITY.getDefaultInstance();

    public AntiGravityCapabilityProvider(double multiplier) {
        super();
        inst.setMultiplier(multiplier);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == ValkyrienSkiesWorld.ANTI_GRAVITY_CAPABILITY;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        return capability == ValkyrienSkiesWorld.ANTI_GRAVITY_CAPABILITY
            ? ValkyrienSkiesWorld.ANTI_GRAVITY_CAPABILITY.cast(inst) : null;
    }

    @Override
    public NBTTagDouble serializeNBT() {
        return (NBTTagDouble) ValkyrienSkiesWorld.ANTI_GRAVITY_CAPABILITY.getStorage()
            .writeNBT(ValkyrienSkiesWorld.ANTI_GRAVITY_CAPABILITY, inst, null);
    }

    @Override
    public void deserializeNBT(NBTTagDouble nbt) {
        ValkyrienSkiesWorld.ANTI_GRAVITY_CAPABILITY.getStorage()
            .readNBT(ValkyrienSkiesWorld.ANTI_GRAVITY_CAPABILITY, inst, null, nbt);
    }

}
