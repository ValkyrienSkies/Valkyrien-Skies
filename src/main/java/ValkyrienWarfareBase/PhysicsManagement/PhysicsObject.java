package ValkyrienWarfareBase.PhysicsManagement;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.ChunkManagement.ChunkSet;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class PhysicsObject {

	public ChunkSet ownedChunks;
	public World worldObj;
	public PhysicsWrapperEntity wrapper;
	public double pitch,yaw,roll;
	//Used for faster memory access to the Chunks this object 'owns'
	public Chunk[][] claimedChunks;
	
	public PhysicsObject(PhysicsWrapperEntity host){
		wrapper = host;
		worldObj = host.worldObj;
	}
	
	public void claimNewChunks(){
		ownedChunks = ValkyrienWarfareMod.chunkManager.getManagerForWorld(wrapper.worldObj).getNextAvaliableChunkSet();
	}
	
	/*
	 * Generates the new chunks
	 */
	public void processChunkClaims(){
		claimedChunks = new Chunk[(ownedChunks.radius*2)+1][(ownedChunks.radius*2)+1];
		for(int x = ownedChunks.minX;x<=ownedChunks.maxX;x++){
			for(int z = ownedChunks.minZ;z<=ownedChunks.maxZ;z++){
				Chunk chunk = new Chunk(worldObj, x, z);
				claimedChunks[x-ownedChunks.minX][z-ownedChunks.minZ] = chunk;
			}
		}
	}
	
	public void loadClaimedChunks(){
		claimedChunks = new Chunk[(ownedChunks.radius*2)+1][(ownedChunks.radius*2)+1];
		for(int x = ownedChunks.minX;x<=ownedChunks.maxX;x++){
			for(int z = ownedChunks.minZ;z<=ownedChunks.maxZ;z++){
				claimedChunks[x-ownedChunks.minX][z-ownedChunks.minZ] = worldObj.getChunkFromChunkCoords(x, z);
			}
		}
	}
	
	public void writeToNBTTag(NBTTagCompound compound){
		ownedChunks.writeToNBT(compound);
	}
	
	public void readFromNBTTag(NBTTagCompound compound){
		ownedChunks = new ChunkSet(compound);
	}
	
	public void readSpawnData(ByteBuf additionalData){
		ownedChunks = new ChunkSet(additionalData.readInt(),additionalData.readInt(),additionalData.readInt());
		pitch = additionalData.readDouble();
		yaw = additionalData.readDouble();
		roll = additionalData.readDouble();
	}
	
	public void writeSpawnData(ByteBuf buffer){
		buffer.writeInt(ownedChunks.centerX);
		buffer.writeInt(ownedChunks.centerZ);
		buffer.writeInt(ownedChunks.radius);
		buffer.writeDouble(pitch);
		buffer.writeDouble(yaw);
		buffer.writeDouble(roll);
	}
	
}
