package ValkyrienWarfareBase.PhysicsManagement;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.ChunkManagement.ChunkSet;
import ValkyrienWarfareBase.ChunkManagement.PhysicsChunkManager;
import net.minecraft.nbt.NBTTagCompound;

public class PhysicsObject {

	public ChunkSet ownedChunks;
	public PhysicsWrapperEntity wrapper;
	
	public PhysicsObject(PhysicsWrapperEntity host){
		wrapper = host;
	}
	
	public void generateNewChunks(){
		ownedChunks = ValkyrienWarfareMod.chunkManager.getManagerForWorld(wrapper.worldObj).getNextAvaliableChunkSet();
	}
	
	public void writeToNBTTag(NBTTagCompound compound){
		ownedChunks.writeToNBT(compound);
	}
	
	public void readFromNBTTag(NBTTagCompound compound){
		ownedChunks = new ChunkSet(compound);
	}
	
}
