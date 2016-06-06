package ValkyrienWarfareBase.PhysicsManagement;

import java.util.HashSet;
import java.util.Set;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.ChunkManagement.ChunkSet;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.network.play.server.SPacketUnloadChunk;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;

public class PhysicsObject {

	public ChunkSet ownedChunks;
	public World worldObj;
	public PhysicsWrapperEntity wrapper;
	public double pitch,yaw,roll;
	//Used for faster memory access to the Chunks this object 'owns'
	public Chunk[][] claimedChunks;
	public HashSet<EntityPlayerMP> watchingPlayers = new HashSet<EntityPlayerMP>();
	public HashSet<EntityPlayerMP> newWatchers = new HashSet<EntityPlayerMP>();
	
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
				injectChunkIntoWorld(chunk,x,z);
			}
		}
	}
	
	public void injectChunkIntoWorld(Chunk chunk,int x,int z){
		ChunkProviderServer provider = (ChunkProviderServer) worldObj.getChunkProvider();
		chunk.setChunkLoaded(true);
		chunk.isModified = true;
		claimedChunks[x-ownedChunks.minX][z-ownedChunks.minZ] = chunk;
		provider.id2ChunkMap.add(ChunkCoordIntPair.chunkXZ2Int(x, z), chunk);
		provider.loadedChunks.add(chunk);
		MinecraftForge.EVENT_BUS.post(new ChunkEvent.Load(chunk));
	}
	
	public void preloadNewPlayers(){
		Set<EntityPlayerMP> newWatchers = getPlayersThatJustWatched();
//		System.out.println(newWatchers.size());
		for(Chunk[] chunkArray: claimedChunks){
			for(Chunk chunk: chunkArray){
				SPacketChunkData data = new SPacketChunkData(chunk, true, 0);
				for(EntityPlayerMP player:newWatchers){
					player.playerNetServerHandler.sendPacket(data);
				}
			}
		}
	}
	
	/**
	 * TODO: Make this further get the player to stop all further tracking of this 
	 * object
	 * @param EntityPlayer that stopped tracking
	 */
	public void onPlayerUntracking(EntityPlayer untracking){
//		System.out.println(untracking.getDisplayNameString()+" has stopped tracking this entity");
		watchingPlayers.remove(untracking);
		for(int x = ownedChunks.minX;x<=ownedChunks.maxX;x++){
			for(int z = ownedChunks.minZ;z<=ownedChunks.maxZ;z++){
				SPacketUnloadChunk unloadPacket = new SPacketUnloadChunk(x,z);
				((EntityPlayerMP)untracking).playerNetServerHandler.sendPacket(unloadPacket);
			}
		}
	}
	
	private Set getPlayersThatJustWatched(){
		HashSet newPlayers = new HashSet();
		for(Object o:((WorldServer)worldObj).getEntityTracker().getTrackingPlayers(wrapper)){
			EntityPlayerMP player = (EntityPlayerMP) o;
			if(!watchingPlayers.contains(player)){
				newPlayers.add(player);
				watchingPlayers.add(player);
			}
		}
		return newPlayers;
	}
	
	public void loadClaimedChunks(){
		claimedChunks = new Chunk[(ownedChunks.radius*2)+1][(ownedChunks.radius*2)+1];
		for(int x = ownedChunks.minX;x<=ownedChunks.maxX;x++){
			for(int z = ownedChunks.minZ;z<=ownedChunks.maxZ;z++){
				Chunk chunk = worldObj.getChunkFromChunkCoords(x, z);
				if(chunk==null){
					System.out.println("Just a loaded a null chunk");
					chunk = new Chunk(worldObj,x,z);
					injectChunkIntoWorld(chunk,x,z);
				}
				claimedChunks[x-ownedChunks.minX][z-ownedChunks.minZ] = chunk;
			}
		}
	}
	
	public void writeToNBTTag(NBTTagCompound compound){
		ownedChunks.writeToNBT(compound);
	}
	
	public void readFromNBTTag(NBTTagCompound compound){
		ownedChunks = new ChunkSet(compound);
		loadClaimedChunks();
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
