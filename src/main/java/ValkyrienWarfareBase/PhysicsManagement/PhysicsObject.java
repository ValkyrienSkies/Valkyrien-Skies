package ValkyrienWarfareBase.PhysicsManagement;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ValkyrienWarfareBase.NBTUtils;
import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.ChunkManagement.ChunkSet;
import ValkyrienWarfareBase.Math.RotationMatrices;
import ValkyrienWarfareBase.Math.Vector;
import ValkyrienWarfareBase.Physics.BlockForce;
import ValkyrienWarfareBase.Physics.PhysicsCalculations;
import ValkyrienWarfareBase.PhysicsManagement.Network.PhysWrapperPositionMessage;
import ValkyrienWarfareBase.Relocation.ShipSpawnDetector;
import ValkyrienWarfareBase.Relocation.VWChunkCache;
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
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkCache;
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
	//Used for faster memory access to the Chunks this object 'owns'
	public Chunk[][] claimedChunks;
	//This handles sending packets to players involving block changes in the Ship space
	public PlayerChunkMapEntry[][] claimedChunksEntries;
	public ArrayList<EntityPlayerMP> watchingPlayers = new ArrayList<EntityPlayerMP>();
	public ArrayList<EntityPlayerMP> newWatchers = new ArrayList<EntityPlayerMP>();
	public VWChunkCache VKChunkCache;
	//It is from this position that the x,y,z coords in local are 0; and that the posX,
	//posY and posZ align with in the global coords
	public BlockPos refrenceBlockPos;
	public Vector centerCoord,lastTickCenterCoord;
	public CoordTransformObject coordTransform;
	public PhysObjectRenderManager renderer;
	public PhysicsCalculations physicsProcessor;
	public ArrayList<BlockPos> blockPositions = new ArrayList<BlockPos>();
	public AxisAlignedBB collisionBB = new AxisAlignedBB(0,0,0,0,0,0);
	
	public boolean doPhysics = true;
	
	public ChunkCache surroundingWorldChunksCache;
	
	private Field playersField = null;
	
	public PhysCollisionCallable collisionCallable = new PhysCollisionCallable(this);
	
	public PhysicsObject(PhysicsWrapperEntity host){
		wrapper = host;
		worldObj = host.worldObj;
		if(host.worldObj.isRemote){
			renderer = new PhysObjectRenderManager(this);
		}else{
			grabPlayerField();
		}
	}
	
	public void onSetBlockState(IBlockState oldState,IBlockState newState,BlockPos posAt){
		boolean isOldAir = oldState==null||oldState.getBlock().equals(Blocks.AIR);
		boolean isNewAir = newState==null||newState.getBlock().equals(Blocks.AIR);
		if(isOldAir&&isNewAir){
			blockPositions.remove(posAt);
		}
		
		if(!isOldAir&&isNewAir){
			blockPositions.remove(posAt);
		}
		
		if(isOldAir&&!isNewAir){
			blockPositions.add(posAt);
			int chunkX = (posAt.getX()>>4)-claimedChunks[0][0].xPosition;
			int chunkZ = (posAt.getZ()>>4)-claimedChunks[0][0].zPosition;
			ownedChunks.chunkOccupiedInLocal[chunkX][chunkZ] = true;
		}
		
		if(blockPositions.size()==0){
			destroy();
		}
		
		if(!worldObj.isRemote){
			if(physicsProcessor!=null){
				physicsProcessor.onSetBlockState(oldState, newState, posAt);
			}
		}else{
			renderer.markForUpdate();
		}
	}
	
	public void destroy(){
		wrapper.setDead();
		ArrayList<EntityPlayerMP> watchersCopy = (ArrayList<EntityPlayerMP>) watchingPlayers.clone();
		for(EntityPlayerMP wachingPlayer:watchersCopy){
			for(int x = ownedChunks.minX;x<=ownedChunks.maxX;x++){
				for(int z = ownedChunks.minZ;z<=ownedChunks.maxZ;z++){
					SPacketUnloadChunk unloadPacket = new SPacketUnloadChunk(x,z);
					((EntityPlayerMP)wachingPlayer).connection.sendPacket(unloadPacket);
				}
			}
			//NOTICE: This method isnt being called to avoid the watchingPlayers.remove(player) call, which is a waste of CPU time
			//onPlayerUntracking(wachingPlayer);
		}
		watchingPlayers.clear();
		ValkyrienWarfareMod.physicsManager.onShipUnload(wrapper);
	}
	
	public void claimNewChunks(){
		ownedChunks = ValkyrienWarfareMod.chunkManager.getManagerForWorld(wrapper.worldObj).getNextAvaliableChunkSet();
	}
	
	/*
	 * Generates the new chunks
	 */
	public void processChunkClaims(){
		claimedChunks = new Chunk[(ownedChunks.radius*2)+1][(ownedChunks.radius*2)+1];
		claimedChunksEntries = new PlayerChunkMapEntry[(ownedChunks.radius*2)+1][(ownedChunks.radius*2)+1];
		for(int x = ownedChunks.minX;x<=ownedChunks.maxX;x++){
			for(int z = ownedChunks.minZ;z<=ownedChunks.maxZ;z++){
				Chunk chunk = new Chunk(worldObj, x, z);
				injectChunkIntoWorld(chunk,x,z);
				claimedChunks[x-ownedChunks.minX][z-ownedChunks.minZ] = chunk;
			}
		}
		
		VKChunkCache = new VWChunkCache(worldObj, claimedChunks);
		int minChunkX = claimedChunks[0][0].xPosition;
		int minChunkZ = claimedChunks[0][0].zPosition;
		BlockPos centerInWorld = new BlockPos(wrapper.posX,wrapper.posY,wrapper.posZ);
		ShipSpawnDetector detector = new ShipSpawnDetector(centerInWorld, worldObj, 5000, true);
		MutableBlockPos pos = new MutableBlockPos();
		TIntIterator iter = detector.foundSet.iterator();
		refrenceBlockPos = getRegionCenter();
		centerCoord = new Vector(refrenceBlockPos.getX(),refrenceBlockPos.getY(),refrenceBlockPos.getZ());
		
		physicsProcessor = new PhysicsCalculations(this);
		BlockPos centerDifference = refrenceBlockPos.subtract(centerInWorld);
		while(iter.hasNext()){
			int i = iter.next();
			detector.setPosWithRespectTo(i, centerInWorld, pos);
			
			IBlockState state = detector.cache.getBlockState(pos);
			pos.setPos(pos.getX()+centerDifference.getX(), pos.getY()+centerDifference.getY(), pos.getZ()+centerDifference.getZ());
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
			worldObj.setBlockState(pos, Blocks.AIR.getDefaultState(),2);
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
		coordTransform = new CoordTransformObject(this);
		physicsProcessor.processInitialPhysicsData();
		physicsProcessor.updateCenterOfMass();
	}
	
	public void injectChunkIntoWorld(Chunk chunk,int x,int z){
		ChunkProviderServer provider = (ChunkProviderServer) worldObj.getChunkProvider();
		if(worldObj.isRemote){
			chunk.setChunkLoaded(true);
		}
		chunk.isModified = true;
		claimedChunks[x-ownedChunks.minX][z-ownedChunks.minZ] = chunk;
		provider.id2ChunkMap.put(ChunkPos.chunkXZ2Int(x, z), chunk);
		
		PlayerChunkMapEntry entry = new PlayerChunkMapEntry(((WorldServer)worldObj).getPlayerChunkMap(), x, z);
		entry.sentToPlayers = true;
		
		try{
			playersField.set(entry, watchingPlayers);
		}catch(Exception e){
			e.printStackTrace();
		}
		PlayerChunkMap map = ((WorldServer)worldObj).getPlayerChunkMap();
		map.addEntry(entry);
		long i = map.getIndex(x, z);
		map.playerInstances.put(i, entry);
		map.playerInstanceList.add(entry);

		claimedChunksEntries[x-ownedChunks.minX][z-ownedChunks.minZ] = entry;
		MinecraftForge.EVENT_BUS.post(new ChunkEvent.Load(chunk));
	}
	
	/**
	 * TODO: Add the methods that send the tileEntities in each given chunk
	 */
	public void preloadNewPlayers(){
		Set<EntityPlayerMP> newWatchers = getPlayersThatJustWatched();
//		for(EntityPlayerMP player:newWatchers){
//			if(!worldObj.isRemote){
//				for(PlayerChunkMapEntry[] entries:claimedChunksEntries){
//					for(PlayerChunkMapEntry entry:entries){
//						entry.addPlayer(player);
//					}
//				}
//			}
//		}
		for(Chunk[] chunkArray: claimedChunks){
			for(Chunk chunk: chunkArray){
				SPacketChunkData data = new SPacketChunkData(chunk, 65535);
				for(EntityPlayerMP player:newWatchers){
					player.connection.sendPacket(data);
					((WorldServer)worldObj).getEntityTracker().sendLeashedEntitiesInChunk(player, chunk);
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
		watchingPlayers.remove(untracking);
		for(int x = ownedChunks.minX;x<=ownedChunks.maxX;x++){
			for(int z = ownedChunks.minZ;z<=ownedChunks.maxZ;z++){
				SPacketUnloadChunk unloadPacket = new SPacketUnloadChunk(x,z);
				((EntityPlayerMP)untracking).connection.sendPacket(unloadPacket);
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
				provider.unload(claimedChunks[x-ownedChunks.minX][z-ownedChunks.minZ]);
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
	
	public void onPreTick(){
		
	}
	
	public void onTick(){
		if(worldObj.isRemote){
			wrapper.prevPitch = wrapper.pitch;
			wrapper.prevYaw = wrapper.yaw;
			wrapper.prevRoll = wrapper.roll;
			
			wrapper.lastTickPosX = wrapper.posX;
			wrapper.lastTickPosY = wrapper.posY;
			wrapper.lastTickPosZ = wrapper.posZ;
			
			lastTickCenterCoord = centerCoord;
			
			ShipTransformData toUse = coordTransform.stack.getDataForTick();
			
			if(toUse!=null){
				Vector CMDif = toUse.centerOfRotation.getSubtraction(centerCoord);
				RotationMatrices.applyTransform(coordTransform.lToWRotation, CMDif);
				
				wrapper.lastTickPosX-=CMDif.X;
				wrapper.lastTickPosY-=CMDif.Y;
				wrapper.lastTickPosZ-=CMDif.Z;
				toUse.applyToPhysObject(this);
			}

		}
		coordTransform.updateAllTransforms();
		
	}
	
	public void updateChunkCache(){
		BlockPos min = new BlockPos(collisionBB.minX,Math.max(collisionBB.minY,0),collisionBB.minZ);
		BlockPos max = new BlockPos(collisionBB.maxX,Math.min(collisionBB.maxY, 255),collisionBB.maxZ);
		surroundingWorldChunksCache = new ChunkCache(worldObj,min,max,0);
	}
	
	public void loadClaimedChunks(){
		claimedChunks = new Chunk[(ownedChunks.radius*2)+1][(ownedChunks.radius*2)+1];
		claimedChunksEntries = new PlayerChunkMapEntry[(ownedChunks.radius*2)+1][(ownedChunks.radius*2)+1];
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
		VKChunkCache = new VWChunkCache(worldObj, claimedChunks);
		refrenceBlockPos = getRegionCenter();
		coordTransform = new CoordTransformObject(this);
		if(!worldObj.isRemote){
			physicsProcessor = new PhysicsCalculations(this);
		}
		detectBlockPositions();
		coordTransform.updateAllTransforms();
	}

	//Generates the blockPos array; must be loaded DIRECTLY after the chunks are setup
	public void detectBlockPositions(){
//		int minChunkX = claimedChunks[0][0].xPosition;
//		int minChunkZ = claimedChunks[0][0].zPosition;
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
			            				if(storage.data.storage.getAt(y << 8 | z << 4 | x)!=ValkyrienWarfareMod.airStateIndex){
			            					BlockPos pos = new BlockPos(chunk.xPosition*16+x,index*16+y,chunk.zPosition*16+z);
			            					blockPositions.add(pos);
				            				if(!worldObj.isRemote){
			            						if(BlockForce.basicForces.isBlockProvidingForce(worldObj.getBlockState(pos), pos, worldObj)){
				            						physicsProcessor.activeForcePositions.add(pos);
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
		}
	}
	
	public boolean ownsChunk(int chunkX,int chunkZ){
		return ownedChunks.isChunkEnclosedInSet(chunkX,chunkZ);
	}
	
	private void grabPlayerField(){
		if(playersField==null){
			try{
				if(!ValkyrienWarfareMod.isObsfucated){
					playersField = PlayerChunkMapEntry.class.getDeclaredField("players");
				}else{
					playersField = PlayerChunkMapEntry.class.getDeclaredField("field_187283_c");
				}
				playersField.setAccessible(true);
			}catch(Exception e){
				e.printStackTrace();
				System.exit(0);
			}
		}
	}
	
	public void writeToNBTTag(NBTTagCompound compound){
		ownedChunks.writeToNBT(compound);
		NBTUtils.writeVectorToNBT("c", centerCoord, compound);
		compound.setDouble("pitch", wrapper.pitch);
		compound.setDouble("yaw", wrapper.yaw);
		compound.setDouble("roll", wrapper.roll);
		compound.setBoolean("doPhysics", doPhysics);
		for(int row = 0;row<ownedChunks.chunkOccupiedInLocal.length;row++){
			boolean[] curArray = ownedChunks.chunkOccupiedInLocal[row];
			for(int column = 0;column<curArray.length;column++){
				compound.setBoolean("CC:"+row+":"+column, curArray[column]);
			}
		}
		physicsProcessor.writeToNBTTag(compound);
	}
	
	public void readFromNBTTag(NBTTagCompound compound){
		ownedChunks = new ChunkSet(compound);
		lastTickCenterCoord = centerCoord = NBTUtils.readVectorFromNBT("c", compound);
		wrapper.pitch = compound.getDouble("pitch");
		wrapper.yaw = compound.getDouble("yaw");
		wrapper.roll = compound.getDouble("roll");
		doPhysics = compound.getBoolean("doPhysics");
		for(int row = 0;row<ownedChunks.chunkOccupiedInLocal.length;row++){
			boolean[] curArray = ownedChunks.chunkOccupiedInLocal[row];
			for(int column = 0;column<curArray.length;column++){
				curArray[column] = compound.getBoolean("CC:"+row+":"+column);
			}
		}
		loadClaimedChunks();
		physicsProcessor.readFromNBTTag(compound);
	}
	
	public void readSpawnData(ByteBuf additionalData){
		ownedChunks = new ChunkSet(additionalData.readInt(),additionalData.readInt(),additionalData.readInt());
		
		wrapper.posX = additionalData.readDouble();
		wrapper.posY = additionalData.readDouble();
		wrapper.posZ = additionalData.readDouble();
		
		wrapper.pitch = additionalData.readDouble();
		wrapper.yaw = additionalData.readDouble();
		wrapper.roll = additionalData.readDouble();
		
		wrapper.prevPitch = wrapper.pitch;
		wrapper.prevYaw = wrapper.yaw;
		wrapper.prevRoll = wrapper.roll;
		
		wrapper.lastTickPosX = wrapper.posX;
		wrapper.lastTickPosY = wrapper.posY;
		wrapper.lastTickPosZ = wrapper.posZ;
		
		centerCoord = new Vector(additionalData);
		for(boolean[] array:ownedChunks.chunkOccupiedInLocal){
			for(int i=0;i<array.length;i++){
				array[i] = additionalData.readBoolean();
			}
		}
		loadClaimedChunks();
		renderer.markForUpdate();
		renderer.updateOffsetPos(refrenceBlockPos);
		
		coordTransform.stack.pushMessage(new PhysWrapperPositionMessage(this));
	}
	
	public void writeSpawnData(ByteBuf buffer){
		buffer.writeInt(ownedChunks.centerX);
		buffer.writeInt(ownedChunks.centerZ);
		buffer.writeInt(ownedChunks.radius);
		
		buffer.writeDouble(wrapper.posX);
		buffer.writeDouble(wrapper.posY);
		buffer.writeDouble(wrapper.posZ);
		
		buffer.writeDouble(wrapper.pitch);
		buffer.writeDouble(wrapper.yaw);
		buffer.writeDouble(wrapper.roll);
		centerCoord.writeToByteBuf(buffer);
		for(boolean[] array:ownedChunks.chunkOccupiedInLocal){
			for(boolean b:array){
				buffer.writeBoolean(b);
			}
		}
	}
	
}
