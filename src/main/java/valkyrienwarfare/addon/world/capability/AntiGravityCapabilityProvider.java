package valkyrienwarfare.addon.world.capability;

import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import valkyrienwarfare.addon.world.ValkyrienWarfareWorld;

public class AntiGravityCapabilityProvider implements ICapabilitySerializable<NBTTagDouble> {

    private ICapabilityAntiGravity inst = ValkyrienWarfareWorld.ANTI_GRAVITY_CAPABILITY.getDefaultInstance();
    
    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == ValkyrienWarfareWorld.ANTI_GRAVITY_CAPABILITY;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        return capability == ValkyrienWarfareWorld.ANTI_GRAVITY_CAPABILITY ? ValkyrienWarfareWorld.ANTI_GRAVITY_CAPABILITY.cast(inst) : null;
    }

    @Override
    public NBTTagDouble serializeNBT() {
        return (NBTTagDouble) ValkyrienWarfareWorld.ANTI_GRAVITY_CAPABILITY.getStorage().writeNBT(ValkyrienWarfareWorld.ANTI_GRAVITY_CAPABILITY, inst, null);
    }

    @Override
    public void deserializeNBT(NBTTagDouble nbt) {
        ValkyrienWarfareWorld.ANTI_GRAVITY_CAPABILITY.getStorage().readNBT(ValkyrienWarfareWorld.ANTI_GRAVITY_CAPABILITY, inst, null, nbt);
    }

}
