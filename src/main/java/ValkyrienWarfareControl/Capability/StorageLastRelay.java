package ValkyrienWarfareControl.Capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class StorageLastRelay implements IStorage<ICapabilityLastRelay> {

	@Override
	public NBTBase writeNBT(Capability<ICapabilityLastRelay> capability, ICapabilityLastRelay instance, EnumFacing side) {
		int x=0,y=0,z=0;
		boolean hasLast = instance.hasLastRelay();

		if(hasLast){
			x = instance.getLastRelay().getX();
			y = instance.getLastRelay().getY();
			z = instance.getLastRelay().getZ();
		}

		return new NBTTagIntArray(new int[]{x,y,z});
	}

	@Override
	public void readNBT(Capability<ICapabilityLastRelay> capability, ICapabilityLastRelay instance, EnumFacing side, NBTBase nbt) {
		NBTTagIntArray tag = (NBTTagIntArray) nbt;
		int[] backingArray = tag.getIntArray();
		//If all these values are 0, then assume the blockPos was just null anyways
		if(!(backingArray[0] == 0 && backingArray[1] == 0 && backingArray[2] == 0)){
			BlockPos pos = new BlockPos(backingArray[0], backingArray[1], backingArray[2]);
			instance.setLastRelay(pos);
		}
	}

}
