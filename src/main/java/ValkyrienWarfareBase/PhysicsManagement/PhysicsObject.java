package ValkyrienWarfareBase.PhysicsManagement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.ChunkManagement.ChunkSet;
import ValkyrienWarfareBase.Coordinates.CoordTransformObject;
import ValkyrienWarfareBase.Relocation.ChunkCache;
import ValkyrienWarfareBase.Relocation.ShipBlockPosFinder;
import ValkyrienWarfareBase.Relocation.ShipSpawnDetector;
import ValkyrienWarfareBase.Render.PhysObjectRenderManager;
import gnu.trove.iterator.TIntIterator;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.network.play.server.SPacketUnloadChunk;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
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
	public ChunkCache chunkCache;
	//It is from this position that the x,y,z coords in local are 0; and that the posX,
	//posY and posZ align with in the global coords
	public BlockPos centerBlockPos;
	public CoordTransformObject coordTransform;
	public PhysObjectRenderManager renderer;
	public ArrayList<BlockPos> blockPositions = new ArrayList<BlockPos>();
	
	public PhysicsObject(PhysicsWrapperEntity host){
		wrapper = host;
		worldObj = host.worldObj;
		if(host.worldObj.isRemote){
			renderer = new PhysObjectRenderManager(this);
		}
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
				claimedChunks[x-ownedChunks.minX][z-ownedChunks.minZ] = chunk;
			}
		}
		chunkCache = new ChunkCache(worldObj, claimedChunks);
		BlockPos centerInWorld = new BlockPos(wrapper.posX,wrapper.posY,wrapper.posZ);
		ShipSpawnDetector detector = new ShipSpawnDetector(centerInWorld, worldObj, 5000, true);
		MutableBlockPos pos = new MutableBlockPos();
		TIntIterator iter = detector.foundSet.iterator();
		centerBlockPos = getRegionCenter();
		coordTransform = new CoordTransformObject(this);
		BlockPos centerDifference = centerBlockPos.subtract(centerInWorld);
		while(iter.hasNext()){
			int i = iter.next();
			detector.setPosWithRespectTo(i, centerInWorld, pos);
			
			IBlockState state = detector.cache.getBlockState(pos);
			pos.set(pos.getX()+centerDifference.getX(), pos.getY()+centerDifference.getY(), pos.getZ()+centerDifference.getZ());
//			System.out.println(pos);
			chunkCache.setBlockState(pos, state);
//			worldObj.setBlockState(pos, state);
		}
		iter = detector.foundSet.iterator();
		short[][] changes = new short[claimedChunks.length][claimedChunks[0].length];
		while(iter.hasNext()){
			int i = iter.next();
//			BlockPos respectTo = detector.getPosWithRespectTo(i, centerInWorld);
			detector.setPosWithRespectTo(i, centerInWorld, pos);
//			detector.cache.setBlockState(pos, Blocks.air.getDefaultState());
			//TODO: Get this to update on clientside as well, you bastard!
			worldObj.setBlockState(pos, Blocks.air.getDefaultState(),2);
		}
//		centerDifference = new BlockPos(claimedChunks[ownedChunks.radius+1][ownedChunks.radius+1].xPosition*16,128,claimedChunks[ownedChunks.radius+1][ownedChunks.radius+1].zPosition*16);
//		System.out.println(chunkCache.getBlockState(centerDifference).getBlock());
		
		detectBlockPositions();
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
	
	/**
	 * TODO: Add the methods that send the tileEntities in each given chunk
	 */
	public void preloadNewPlayers(){
		Set<EntityPlayerMP> newWatchers = getPlayersThatJustWatched();
		for(Chunk[] chunkArray: claimedChunks){
			for(Chunk chunk: chunkArray){
				SPacketChunkData data = new SPacketChunkData(chunk, true, 65535);
				for(EntityPlayerMP player:newWatchers){
					player.playerNetServerHandler.sendPacket(data);
				}
			}
		}
	}
	
	public BlockPos getRegionCenter(){
		return new BlockPos(claimedChunks[ownedChunks.radius+1][ownedChunks.radius+1].xPosition*16,128,claimedChunks[ownedChunks.radius+1][ownedChunks.radius+1].zPosition*16);
	}
	
	/**
	 * TODO: Make this further get the player to stop all further tracking of 
	 * thos physObject
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
		centerBlockPos = getRegionCenter();
		coordTransform = new CoordTransformObject(this);
		detectBlockPositions();
	}
	
	//Generates the blockPos array; must be loaded DIRECTLY after the chunks are setup
	public void detectBlockPositions(){
		ShipBlockPosFinder finder = new ShipBlockPosFinder(centerBlockPos, worldObj, 10000, true);
		TIntIterator iterator = finder.foundSet.iterator();
		int temp;
		while(iterator.hasNext()){
			temp = iterator.next();
			blockPositions.add(finder.getPosWithRespectTo(temp, centerBlockPos));
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
		loadClaimedChunks();
		renderer.markForUpdate();
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
