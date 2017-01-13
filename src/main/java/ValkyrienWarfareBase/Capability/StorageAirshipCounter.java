package ValkyrienWarfareBase.Capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class StorageAirshipCounter implements IStorage<IAirshipCounterCapability> {

	@Override
	public NBTBase writeNBT(Capability<IAirshipCounterCapability> capability, IAirshipCounterCapability instance, EnumFacing side) {
		return new NBTTagInt(instance.getAirshipCount());
	}

	@Override
	public void readNBT(Capability<IAirshipCounterCapability> capability, IAirshipCounterCapability instance, EnumFacing side, NBTBase nbt) {
		instance.setAirshipCount(((NBTPrimitive) nbt).getInt());
	}

}
