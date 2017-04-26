package ValkyrienWarfareBase.Capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class StorageAirshipCounter implements IStorage<IAirshipCounterCapability> {

	@Override
	public NBTBase writeNBT(Capability<IAirshipCounterCapability> capability, IAirshipCounterCapability instance, EnumFacing side) {
		return new NBTTagIntArray(new int[]{instance.getAirshipCount(), instance.getAirshipCountEver()});
	}

	@Override
	public void readNBT(Capability<IAirshipCounterCapability> capability, IAirshipCounterCapability instance, EnumFacing side, NBTBase nbt) {
		NBTTagIntArray tag = (NBTTagIntArray) nbt;
		instance.setAirshipCount(tag.getIntArray()[0]);
		instance.setAirshipCountEver(tag.getIntArray()[1]);
	}

}
