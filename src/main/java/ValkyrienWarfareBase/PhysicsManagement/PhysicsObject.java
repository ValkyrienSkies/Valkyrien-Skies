package ValkyrienWarfareBase.PhysicsManagement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.ChunkManagement.ChunkSet;
import ValkyrienWarfareBase.Coordinates.CoordTransformObject;
import ValkyrienWarfareBase.Math.Vector;
import ValkyrienWarfareBase.Relocation.ChunkCache;
import ValkyrienWarfareBase.Relocation.ShipSpawnDetector;
import ValkyrienWarfareBase.Render.PhysObjectRenderManager;
import gnu.trove.iterator.TIntIterator;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.network.play.server.SPacketUnloadChunk;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
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
	public BlockPos refrenceBlockPos;
	public Vector centerCoord;
	public CoordTransformObject coordTransform;
	public PhysObjectRenderManager renderer;
	public ArrayList<BlockPos> blockPositions = new ArrayList<BlockPos>();
	public AxisAlignedBB collisionBB = new AxisAlignedBB(0,0,0,0,0,0);
	//	public Ticket chunkLoadingTicket;
	
	public PhysicsObject(PhysicsWrapperEntity host){
		wrapper = host;
		worldObj = host.worldObj;
		if(host.worldObj.isRemote){
			renderer = new PhysObjectRenderManager(this);
		}else{
//			chunkLoadingTicket = ForgeChunkManager.requestTicket(ValkyrienWarfareMod.instance, worldObj, ForgeChunkManager.Type.NORMAL);
		}
//		yaw = 45D;
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
		int minChunkX = claimedChunks[0][0].xPosition;
		int minChunkZ = claimedChunks[0][0].zPosition;
		BlockPos centerInWorld = new BlockPos(wrapper.posX,wrapper.posY,wrapper.posZ);
		ShipSpawnDetector detector = new ShipSpawnDetector(centerInWorld, worldObj, 5000, true);
		MutableBlockPos pos = new MutableBlockPos();
		TIntIterator iter = detector.foundSet.iterator();
		refrenceBlockPos = getRegionCenter();
		centerCoord = new Vector(refrenceBlockPos.getX(),refrenceBlockPos.getY(),refrenceBlockPos.getZ());
		coordTransform = new CoordTransformObject(this);
		BlockPos centerDifference = refrenceBlockPos.subtract(centerInWorld);
		while(iter.hasNext()){
			int i = iter.next();
			detector.setPosWithRespectTo(i, centerInWorld, pos);
			
			IBlockState state = detector.cache.getBlockState(pos);
			pos.set(pos.getX()+centerDifference.getX(), pos.getY()+centerDifference.getY(), pos.getZ()+centerDifference.getZ());
//			System.out.println(pos);
			ownedChunks.chunkOccupiedInLocal[(pos.getX()>>4)-minChunkX][(pos.getZ()>>4)-minChunkZ] = true;
			
			Chunk chunkToSet = claimedChunks[(pos.getX()>>4)-minChunkX][(pos.getZ()>>4)-minChunkZ];
			int storageIndex = pos.getY() >> 4;
			
			if (chunkToSet.storageArrays[storageIndex] == chunkToSet.NULL_BLOCK_STORAGE)
            {
				chunkToSet.storageArrays[storageIndex] = new ExtendedBlockStorage(storageIndex << 4, true);
            }

			chunkToSet.storageArrays[storageIndex].set(pos.getX()& 15, pos.getY()& 15, pos.getZ()& 15, state);
//			chunkCache.setBlockState(pos, state);
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
		
		for(int x = ownedChunks.minX;x<=ownedChunks.maxX;x++){
			for(int z = ownedChunks.minZ;z<=ownedChunks.maxZ;z++){
				claimedChunks[x-ownedChunks.minX][z-ownedChunks.minZ].isTerrainPopulated = true;
				claimedChunks[x-ownedChunks.minX][z-ownedChunks.minZ].generateSkylightMap();
//				claimedChunks[x-ownedChunks.minX][z-ownedChunks.minZ].checkLight();
			}
		}
		
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
//		ForgeChunkManager.forceChunk(chunkLoadingTicket, chunk.getChunkCoordIntPair());
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
					((WorldServer)worldObj).getEntityTracker().sendLeashedEntitiesInChunk(player, chunk);
				}
			}
		}
		
		int minX,maxX,minZ,maxZ;
		minX = claimedChunks[0][0].xPosition*16;
		minZ = claimedChunks[0][0].zPosition*16;
		maxX = claimedChunks[claimedChunks.length-1][claimedChunks[claimedChunks.length-1].length-1].xPosition*16+15;
		maxZ = claimedChunks[claimedChunks.length-1][claimedChunks[claimedChunks.length-1].length-1].zPosition*16+15;
		
		List<TileEntity> wholeList = Lists.newArrayList(((WorldServer) worldObj).getTileEntitiesIn(minX,0,minZ,maxX,256,maxZ));
		
		for(TileEntity tileentity:wholeList){
			Packet<?> packet = tileentity.getDescriptionPacket();
			if(packet!=null){
				for(EntityPlayerMP player:newWatchers){
					player.playerNetServerHandler.sendPacket(packet);
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
	
	/**
	 * Called when this entity has been unloaded from the world
	 */
	public void onThisUnload(){
		if(!worldObj.isRemote){
			unloadShipChunksFromWorld();
		}
	}
	
	public void unloadShipChunksFromWorld(){
		ChunkProviderServer provider = (ChunkProviderServer) worldObj.getChunkProvider();
		for(int x = ownedChunks.minX;x<=ownedChunks.maxX;x++){
			for(int z = ownedChunks.minZ;z<=ownedChunks.maxZ;z++){
				provider.dropChunk(x, z);
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
	
	public void onTick(){
		//Move xyz here
		yaw = 0D;
		//Update coordinate transforms
		coordTransform.updateTransforms();
		if(!worldObj.isRemote){
			for(int x = ownedChunks.minX;x<=ownedChunks.maxX;x++){
				for(int z = ownedChunks.minZ;z<=ownedChunks.maxZ;z++){
					//TODO: WORLD CHUNKS NOT LOADING CORRECTLY!!!
//					System.out.println(((ChunkProviderServer)worldObj.getChunkProvider()).chunkExists(x, z));
					
				}
			}
		}
	}
	
	public void loadClaimedChunks(){
		claimedChunks = new Chunk[(ownedChunks.radius*2)+1][(ownedChunks.radius*2)+1];
		for(int x = ownedChunks.minX;x<=ownedChunks.maxX;x++){
			for(int z = ownedChunks.minZ;z<=ownedChunks.maxZ;z++){
				Chunk chunk = worldObj.getChunkFromChunkCoords(x, z);
				if(chunk==null){
					System.out.println("Just a loaded a null chunk");
					chunk = new Chunk(worldObj,x,z);
				}
				//Do this to get it re-integrated into the world
				if(!worldObj.isRemote){
					injectChunkIntoWorld(chunk,x,z);
				}
				claimedChunks[x-ownedChunks.minX][z-ownedChunks.minZ] = chunk;
			}
		}
		refrenceBlockPos = getRegionCenter();
		coordTransform = new CoordTransformObject(this);
		detectBlockPositions();
		coordTransform.updateTransforms();
	}
	
	//Generates the blockPos array; must be loaded DIRECTLY after the chunks are setup
	public void detectBlockPositions(){
		int minChunkX = claimedChunks[0][0].xPosition;
		int minChunkZ = claimedChunks[0][0].zPosition;
		int chunkX,chunkZ,index,x,y,z;
		Chunk chunk;
		ExtendedBlockStorage storage;
		for(chunkX = claimedChunks.length-1;chunkX>-1;chunkX--){
			for(chunkZ = claimedChunks[0].length-1;chunkZ>-1;chunkZ--){
				chunk = claimedChunks[chunkX][chunkZ];
				if(chunk!=null&&ownedChunks.chunkOccupiedInLocal[chunkX][chunkZ]){
					for(index=0;index<16;index++){
			        	storage = chunk.getBlockStorageArray()[index];
			        	if(storage!=null){
			        		for(y=0;y<16;y++){
			        			for(x=0;x<16;x++){
			        				for(z=0;z<16;z++){
			            				if(storage.data.bits.func_188142_a(y << 8 | z << 4 | x)!=ValkyrienWarfareMod.airStateIndex){
			            					blockPositions.add(new BlockPos(chunk.xPosition*16+x,index*16+y,chunk.zPosition*16+z));
			            				}
			            			}
			        			}
			        		}
			        	}
			        }
				}
			}
		}
	}
	
	public void writeToNBTTag(NBTTagCompound compound){
		ownedChunks.writeToNBT(compound);
		compound.setDouble("cX", centerCoord.X);
		compound.setDouble("cY", centerCoord.Y);
		compound.setDouble("cZ", centerCoord.Z);
		for(int row = 0;row<ownedChunks.chunkOccupiedInLocal.length;row++){
			boolean[] curArray = ownedChunks.chunkOccupiedInLocal[row];
			for(int column = 0;column<curArray.length;column++){
				compound.setBoolean("CC:"+row+":"+column, curArray[column]);
			}
		}
	}
	
	public void readFromNBTTag(NBTTagCompound compound){
		ownedChunks = new ChunkSet(compound);
		centerCoord = new Vector(compound.getDouble("cX"),compound.getDouble("cY"),compound.getDouble("cZ"));
		for(int row = 0;row<ownedChunks.chunkOccupiedInLocal.length;row++){
			boolean[] curArray = ownedChunks.chunkOccupiedInLocal[row];
			for(int column = 0;column<curArray.length;column++){
				curArray[column] = compound.getBoolean("CC:"+row+":"+column);
			}
		}
		loadClaimedChunks();
	}
	
	public void readSpawnData(ByteBuf additionalData){
		ownedChunks = new ChunkSet(additionalData.readInt(),additionalData.readInt(),additionalData.readInt());
		pitch = additionalData.readDouble();
		yaw = additionalData.readDouble();
		roll = additionalData.readDouble();
		centerCoord = new Vector(additionalData);
		for(boolean[] array:ownedChunks.chunkOccupiedInLocal){
			for(int i=0;i<array.length;i++){
				array[i] = additionalData.readBoolean();
			}
		}
		loadClaimedChunks();
		renderer.markForUpdate();
		renderer.updateOffsetPos(refrenceBlockPos);
	}
	
	public void writeSpawnData(ByteBuf buffer){
		buffer.writeInt(ownedChunks.centerX);
		buffer.writeInt(ownedChunks.centerZ);
		buffer.writeInt(ownedChunks.radius);
		buffer.writeDouble(pitch);
		buffer.writeDouble(yaw);
		buffer.writeDouble(roll);
		centerCoord.writeToByteBuf(buffer);
		for(boolean[] array:ownedChunks.chunkOccupiedInLocal){
			for(boolean b:array){
				buffer.writeBoolean(b);
			}
		}
	}
	
}
