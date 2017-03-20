package ValkyrienWarfareBase.PhysicsManagement;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import ValkyrienWarfareBase.BlockPhysicsRegistration;
import ValkyrienWarfareBase.NBTUtils;
import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.API.EnumChangeOwnerResult;
import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.ChunkManagement.ChunkSet;
import ValkyrienWarfareBase.CoreMod.ValkyrienWarfarePlugin;
import ValkyrienWarfareBase.Physics.BlockForce;
import ValkyrienWarfareBase.Physics.PhysicsCalculations;
import ValkyrienWarfareBase.Physics.PhysicsQueuedForce;
import ValkyrienWarfareBase.PhysicsManagement.Network.PhysWrapperPositionMessage;
import ValkyrienWarfareBase.Relocation.DetectorManager;
import ValkyrienWarfareBase.Relocation.SpatialDetector;
import ValkyrienWarfareBase.Relocation.VWChunkCache;
import ValkyrienWarfareBase.Render.PhysObjectRenderManager;
import ValkyrienWarfareControl.ValkyrienWarfareControlMod;
import ValkyrienWarfareControl.Balloon.ShipBalloonManager;
import ValkyrienWarfareControl.Network.EntityFixMessage;
import ValkyrienWarfareControl.Piloting.ShipPilotingController;
import gnu.trove.iterator.TIntIterator;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.network.play.server.SPacketUnloadChunk;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class PhysicsObject {

	public World worldObj;
	public PhysicsWrapperEntity wrapper;
	// This handles sending packets to players involving block changes in the Ship space
	public ArrayList<EntityPlayerMP> watchingPlayers = new ArrayList<EntityPlayerMP>();
	public ArrayList<EntityPlayerMP> newWatchers = new ArrayList<EntityPlayerMP>();

	// It is from this position that the x,y,z coords in local are 0; and that the posX,
	// posY and posZ align with in the global coords
	public BlockPos refrenceBlockPos;
	public Vector centerCoord, lastTickCenterCoord;
	public CoordTransformObject coordTransform;
	public PhysObjectRenderManager renderer;
	public PhysicsCalculations physicsProcessor;
	public ArrayList<BlockPos> blockPositions = new ArrayList<BlockPos>();
	public AxisAlignedBB collisionBB = PhysicsWrapperEntity.ZERO_AABB;

	public ArrayList<PhysicsQueuedForce> queuedPhysForces = new ArrayList<PhysicsQueuedForce>();
	public ArrayList<BlockPos> explodedPositionsThisTick = new ArrayList<BlockPos>();
	public boolean doPhysics = true;
	public boolean fromSplit = false;

	// The closest Chunks to the Ship cached in here
	public ChunkCache surroundingWorldChunksCache;
	public String creator;

	private static Field playersField = null;

	public PhysCollisionCallable collisionCallable = new PhysCollisionCallable(this);

	public int lastMessageTick;
	public int detectorID;

	public boolean blocksChanged = false;

	// TODO: Make for re-organizing these to make Ship sizes Dynamic
	public ChunkSet ownedChunks;
	// Used for faster memory access to the Chunks this object 'owns'
	public Chunk[][] claimedChunks;
	public VWChunkCache VKChunkCache;
	// Some badly written mods use these Maps to determine who to send packets to, so we need to manually fill them with nearby players
	public PlayerChunkMapEntry[][] claimedChunksEntries;

	public ShipBalloonManager balloonManager;

	public HashMap<Integer, Vector> entityLocalPositions = new HashMap<Integer, Vector>();

	public ShipPilotingController pilotingController;

	public ArrayList<String> allowedUsers = new ArrayList<String>();
	//This is used to delay mountEntity() operations by 1 tick
	public ArrayList<Entity> queuedEntitiesToMount = new ArrayList<Entity>();

	public PhysicsObject(PhysicsWrapperEntity host) {
		wrapper = host;
		worldObj = host.worldObj;
		if (host.worldObj.isRemote) {
			renderer = new PhysObjectRenderManager(this);
		} else {
			balloonManager = new ShipBalloonManager(this);
			pilotingController = new ShipPilotingController(this);
			if (playersField == null) {
				grabPlayerField();
			}
		}
	}

	public void onSetBlockState(IBlockState oldState, IBlockState newState, BlockPos posAt) {
		//If the block here is not to be physicsed, just treat it like you'd treat AIR blocks.
		boolean oldStateOnBlackList = false, newStateOnBlackList = false;

		if(oldState != null && BlockPhysicsRegistration.blocksToNotPhysicise.contains(oldState.getBlock())){
			oldState = Blocks.AIR.getDefaultState();
			oldStateOnBlackList = true;
		}
		if(newState != null && BlockPhysicsRegistration.blocksToNotPhysicise.contains(newState.getBlock())){
			newState = Blocks.AIR.getDefaultState();
			newStateOnBlackList = true;
		}

		boolean isOldAir = oldState == null || oldState.getBlock().equals(Blocks.AIR);
		boolean isNewAir = newState == null || newState.getBlock().equals(Blocks.AIR);

		if (!ownedChunks.isChunkEnclosedInMaxSet(posAt.getX() >> 4, posAt.getZ() >> 4)) {
			return;
		}

		if (!ownedChunks.isChunkEnclosedInSet(posAt.getX() >> 4, posAt.getZ() >> 4)) {
			return;
		}

		blocksChanged = true;

		if (isNewAir) {
			blockPositions.remove(posAt);
			if (!worldObj.isRemote) {
				balloonManager.onBlockPositionRemoved(posAt);
			}
		}

		if ((isOldAir && !isNewAir)) {
			blockPositions.add(posAt);
			if (!worldObj.isRemote) {
				balloonManager.onBlockPositionAdded(posAt);
			}
			int chunkX = (posAt.getX() >> 4) - claimedChunks[0][0].xPosition;
			int chunkZ = (posAt.getZ() >> 4) - claimedChunks[0][0].zPosition;
			ownedChunks.chunkOccupiedInLocal[chunkX][chunkZ] = true;
		}

		if (blockPositions.isEmpty()) {
			try{
				if (!worldObj.isRemote) {
					if (creator != null) {
						EntityPlayer player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(UUID.fromString(creator));
						if (player != null) {
							player.getCapability(ValkyrienWarfareMod.airshipCounter, null).onLose();
						} else {
							try {
								File f = new File(DimensionManager.getCurrentSaveRootDirectory(), "playerdata/" + creator + ".dat");
								NBTTagCompound tag = CompressedStreamTools.read(f);
								NBTTagCompound capsTag = tag.getCompoundTag("ForgeCaps");
								capsTag.setInteger("valkyrienwarfare:IAirshipCounter", capsTag.getInteger("valkyrienwarfare:IAirshipCounter") - 1);
								CompressedStreamTools.safeWrite(tag, f);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						ValkyrienWarfareMod.chunkManager.getManagerForWorld(worldObj).data.avalibleChunkKeys.add(ownedChunks.centerX);
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}

			destroy();
		}

		if (!worldObj.isRemote) {
			if (physicsProcessor != null) {
				physicsProcessor.onSetBlockState(oldState, newState, posAt);
			}
		}

		System.out.println(blockPositions.size() + ":" + wrapper.isDead);
	}

	public void destroy() {
		wrapper.setDead();
		ArrayList<EntityPlayerMP> watchersCopy = (ArrayList<EntityPlayerMP>) watchingPlayers.clone();
		for (EntityPlayerMP wachingPlayer : watchersCopy) {
			for (int x = ownedChunks.minX; x <= ownedChunks.maxX; x++) {
				for (int z = ownedChunks.minZ; z <= ownedChunks.maxZ; z++) {
					SPacketUnloadChunk unloadPacket = new SPacketUnloadChunk(x, z);
					((EntityPlayerMP) wachingPlayer).connection.sendPacket(unloadPacket);
				}
			}
			// NOTICE: This method isnt being called to avoid the watchingPlayers.remove(player) call, which is a waste of CPU time
			// onPlayerUntracking(wachingPlayer);
		}
		watchingPlayers.clear();
		ValkyrienWarfareMod.physicsManager.onShipUnload(wrapper);
	}

	public void claimNewChunks(int radius) {
		ownedChunks = ValkyrienWarfareMod.chunkManager.getManagerForWorld(wrapper.worldObj).getNextAvaliableChunkSet(radius);
	}

	/*
	 * Generates the new chunks
	 */
	public void processChunkClaims(EntityPlayer player) {
		BlockPos centerInWorld = new BlockPos(wrapper.posX, wrapper.posY, wrapper.posZ);
		SpatialDetector detector = DetectorManager.getDetectorFor(detectorID, centerInWorld, worldObj, ValkyrienWarfareMod.maxShipSize + 1, true);
		if (detector.foundSet.size() > ValkyrienWarfareMod.maxShipSize || detector.cleanHouse) {
			if (player != null) {
				player.addChatComponentMessage(new TextComponentString("Ship construction canceled because its exceeding the ship size limit (Raise with /physSettings maxShipSize <number>) ; Or because it's attatched to bedrock)"));
			}
			wrapper.setDead();
			return;
		}
		MutableBlockPos pos = new MutableBlockPos();
		TIntIterator iter = detector.foundSet.iterator();

		int radiusNeeded = 1;

		while (iter.hasNext()) {
			int i = iter.next();
			detector.setPosWithRespectTo(i, BlockPos.ORIGIN, pos);

			int xRad = Math.abs(pos.getX() >> 4);
			int zRad = Math.abs(pos.getZ() >> 4);

			radiusNeeded = Math.max(Math.max(zRad, xRad), radiusNeeded + 1);
		}

//		radiusNeeded = Math.max(radiusNeeded, 5);

		iter = detector.foundSet.iterator();

		radiusNeeded = Math.min(radiusNeeded, ValkyrienWarfareMod.chunkManager.getManagerForWorld(wrapper.worldObj).maxChunkRadius);

		// System.out.println(radiusNeeded);

		claimNewChunks(radiusNeeded);

		claimedChunks = new Chunk[(ownedChunks.radius * 2) + 1][(ownedChunks.radius * 2) + 1];
		claimedChunksEntries = new PlayerChunkMapEntry[(ownedChunks.radius * 2) + 1][(ownedChunks.radius * 2) + 1];
		for (int x = ownedChunks.minX; x <= ownedChunks.maxX; x++) {
			for (int z = ownedChunks.minZ; z <= ownedChunks.maxZ; z++) {
				Chunk chunk = new Chunk(worldObj, x, z);
				injectChunkIntoWorld(chunk, x, z, true);
				claimedChunks[x - ownedChunks.minX][z - ownedChunks.minZ] = chunk;
			}
		}

		//Prevents weird shit from spawning at the edges of a ship
		replaceOuterChunksWithAir();

		VKChunkCache = new VWChunkCache(worldObj, claimedChunks);
		int minChunkX = claimedChunks[0][0].xPosition;
		int minChunkZ = claimedChunks[0][0].zPosition;

		refrenceBlockPos = getRegionCenter();
		centerCoord = new Vector(refrenceBlockPos.getX(), refrenceBlockPos.getY(), refrenceBlockPos.getZ());

		physicsProcessor = new PhysicsCalculations(this);
		BlockPos centerDifference = refrenceBlockPos.subtract(centerInWorld);
		while (iter.hasNext()) {
			int i = iter.next();
			detector.setPosWithRespectTo(i, centerInWorld, pos);

			IBlockState state = detector.cache.getBlockState(pos);

			TileEntity worldTile = detector.cache.getTileEntity(pos);

			pos.setPos(pos.getX() + centerDifference.getX(), pos.getY() + centerDifference.getY(), pos.getZ() + centerDifference.getZ());
			// System.out.println(pos);
			ownedChunks.chunkOccupiedInLocal[(pos.getX() >> 4) - minChunkX][(pos.getZ() >> 4) - minChunkZ] = true;

			Chunk chunkToSet = claimedChunks[(pos.getX() >> 4) - minChunkX][(pos.getZ() >> 4) - minChunkZ];
			int storageIndex = pos.getY() >> 4;

			if (chunkToSet.storageArrays[storageIndex] == chunkToSet.NULL_BLOCK_STORAGE) {
				chunkToSet.storageArrays[storageIndex] = new ExtendedBlockStorage(storageIndex << 4, true);
			}

			chunkToSet.storageArrays[storageIndex].set(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15, state);

			if (worldTile != null) {
				NBTTagCompound tileEntNBT = new NBTTagCompound();
				tileEntNBT = worldTile.writeToNBT(tileEntNBT);
				// Change the Block position to be inside of the Ship
				tileEntNBT.setInteger("x", pos.getX());
				tileEntNBT.setInteger("y", pos.getY());
				tileEntNBT.setInteger("z", pos.getZ());

				//Fuck this old code
//				TileEntity newInstace = VKChunkCache.getTileEntity(pos);
//				newInstace.readFromNBT(tileEntNBT);

				TileEntity newInstance = TileEntity.create(worldObj, tileEntNBT);
				newInstance.validate();

				Class tileClass = newInstance.getClass();
				Field[] fields = tileClass.getDeclaredFields();
				for (Field field : fields) {
					try {
						field.setAccessible(true);
						Object o = field.get(newInstance);
						if (o != null) {
							if (o instanceof BlockPos) {
								BlockPos inTilePos = (BlockPos) o;
								int hash = detector.getHashWithRespectTo(inTilePos.getX(), inTilePos.getY(), inTilePos.getZ(), detector.firstBlock);
								if (detector.foundSet.contains(hash)) {
									if (!(o instanceof MutableBlockPos)) {
										inTilePos = inTilePos.add(centerDifference.getX(), centerDifference.getY(), centerDifference.getZ());
										field.set(newInstance, inTilePos);
									} else {
										MutableBlockPos mutable = (MutableBlockPos) o;
										mutable.setPos(inTilePos.getX() + centerDifference.getX(), inTilePos.getY() + centerDifference.getY(), inTilePos.getZ() + centerDifference.getZ());
									}
								}
							}
						}
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}

				newInstance.markDirty();
			}
			// chunkCache.setBlockState(pos, state);
			// worldObj.setBlockState(pos, state);
		}
		iter = detector.foundSet.iterator();
		short[][] changes = new short[claimedChunks.length][claimedChunks[0].length];
		while (iter.hasNext()) {
			int i = iter.next();
			// BlockPos respectTo = detector.getPosWithRespectTo(i, centerInWorld);
			detector.setPosWithRespectTo(i, centerInWorld, pos);
			// detector.cache.setBlockState(pos, Blocks.air.getDefaultState());
			// TODO: Get this to update on clientside as well, you bastard!
			worldObj.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
		}
		// centerDifference = new BlockPos(claimedChunks[ownedChunks.radius+1][ownedChunks.radius+1].xPosition*16,128,claimedChunks[ownedChunks.radius+1][ownedChunks.radius+1].zPosition*16);
		// System.out.println(chunkCache.getBlockState(centerDifference).getBlock());

		for (int x = ownedChunks.minX; x <= ownedChunks.maxX; x++) {
			for (int z = ownedChunks.minZ; z <= ownedChunks.maxZ; z++) {
				claimedChunks[x - ownedChunks.minX][z - ownedChunks.minZ].isTerrainPopulated = true;
//				claimedChunks[x - ownedChunks.minX][z - ownedChunks.minZ].generateSkylightMap();
				// claimedChunks[x-ownedChunks.minX][z-ownedChunks.minZ].checkLight();
			}
		}

		detectBlockPositions();

		//TODO: This fixes the lighting, but it adds lag; maybe remove this
		for (int x = ownedChunks.minX; x <= ownedChunks.maxX; x++) {
			for (int z = ownedChunks.minZ; z <= ownedChunks.maxZ; z++) {
//				claimedChunks[x - ownedChunks.minX][z - ownedChunks.minZ].isTerrainPopulated = true;
				claimedChunks[x - ownedChunks.minX][z - ownedChunks.minZ].generateSkylightMap();
				claimedChunks[x-ownedChunks.minX][z-ownedChunks.minZ].checkLight();
			}
		}

		coordTransform = new CoordTransformObject(this);
		physicsProcessor.processInitialPhysicsData();
		physicsProcessor.updateParentCenterOfMass();
	}

	public void injectChunkIntoWorld(Chunk chunk, int x, int z, boolean putInId2ChunkMap) {
		ChunkProviderServer provider = (ChunkProviderServer) worldObj.getChunkProvider();
		//TileEntities will break if you don't do this
		chunk.isChunkLoaded = true;
		chunk.isModified = true;
		claimedChunks[x - ownedChunks.minX][z - ownedChunks.minZ] = chunk;

		if(putInId2ChunkMap){
			provider.id2ChunkMap.put(ChunkPos.chunkXZ2Int(x, z), chunk);
		}

		PlayerChunkMapEntry entry = new PlayerChunkMapEntry(((WorldServer) worldObj).getPlayerChunkMap(), x, z);
		entry.sentToPlayers = true;
		entry.chunk = chunk;

		try {
			playersField.set(entry, watchingPlayers);
		} catch (Exception e) {
			e.printStackTrace();
		}
		PlayerChunkMap map = ((WorldServer) worldObj).getPlayerChunkMap();
		map.addEntry(entry);
		long i = map.getIndex(x, z);
		map.playerInstances.put(i, entry);
		map.playerInstanceList.add(entry);

		claimedChunksEntries[x - ownedChunks.minX][z - ownedChunks.minZ] = entry;
//		MinecraftForge.EVENT_BUS.post(new ChunkEvent.Load(chunk));
	}

	//Experimental, could fix issues with random shit generating inside of Ships
	private void replaceOuterChunksWithAir(){
		for (int x = ownedChunks.minX - 1; x <= ownedChunks.maxX + 1; x++) {
			for (int z = ownedChunks.minZ -1; z <= ownedChunks.maxZ + 1; z++) {
				if(x == ownedChunks.minX -1 || x == ownedChunks.maxX +1 || z == ownedChunks.minZ -1 || z == ownedChunks.maxZ + 1){
					//This is satisfied for the chunks surrounding a Ship, do fill it with empty space
					Chunk chunk = new Chunk(worldObj, x, z);
					ChunkProviderServer provider = (ChunkProviderServer) worldObj.getChunkProvider();
					chunk.isModified = true;
					provider.id2ChunkMap.put(ChunkPos.chunkXZ2Int(x, z), chunk);
				}
			}
		}
	}

	/**
	 * TODO: Add the methods that send the tileEntities in each given chunk
	 */
	public void preloadNewPlayers() {
		Set<EntityPlayerMP> newWatchers = getPlayersThatJustWatched();
		// for(EntityPlayerMP player:newWatchers){
		// if(!worldObj.isRemote){
		// for(PlayerChunkMapEntry[] entries:claimedChunksEntries){
		// for(PlayerChunkMapEntry entry:entries){
		// entry.addPlayer(player);
		// }
		// }
		// }
		// }
		for (Chunk[] chunkArray : claimedChunks) {
			for (Chunk chunk : chunkArray) {
				SPacketChunkData data = new SPacketChunkData(chunk, 65535);
				for (EntityPlayerMP player : newWatchers) {
					player.connection.sendPacket(data);
					((WorldServer) worldObj).getEntityTracker().sendLeashedEntitiesInChunk(player, chunk);
				}
			}
		}
	}

	public BlockPos getRegionCenter() {
		return new BlockPos((claimedChunks[ownedChunks.radius + 1][ownedChunks.radius + 1].xPosition * 16) - 8, 127, (claimedChunks[ownedChunks.radius + 1][ownedChunks.radius + 1].zPosition * 16) - 8);
	}

	/**
	 * TODO: Make this further get the player to stop all further tracking of thos physObject
	 *
	 * @param EntityPlayer
	 *            that stopped tracking
	 */
	public void onPlayerUntracking(EntityPlayer untracking) {
		watchingPlayers.remove(untracking);
		for (int x = ownedChunks.minX; x <= ownedChunks.maxX; x++) {
			for (int z = ownedChunks.minZ; z <= ownedChunks.maxZ; z++) {
				SPacketUnloadChunk unloadPacket = new SPacketUnloadChunk(x, z);
				((EntityPlayerMP) untracking).connection.sendPacket(unloadPacket);
			}
		}
	}

	/**
	 * Called when this entity has been unloaded from the world
	 */
	public void onThisUnload() {
		if (!worldObj.isRemote) {
			unloadShipChunksFromWorld();
		}else{
			renderer.killRenderers();
		}
	}

	public void unloadShipChunksFromWorld() {
		ChunkProviderServer provider = (ChunkProviderServer) worldObj.getChunkProvider();
		for (int x = ownedChunks.minX; x <= ownedChunks.maxX; x++) {
			for (int z = ownedChunks.minZ; z <= ownedChunks.maxZ; z++) {
				provider.unload(claimedChunks[x - ownedChunks.minX][z - ownedChunks.minZ]);
			}
		}
	}

	private Set getPlayersThatJustWatched() {
		HashSet newPlayers = new HashSet();
		for (Object o : ((WorldServer) worldObj).getEntityTracker().getTrackingPlayers(wrapper)) {
			EntityPlayerMP player = (EntityPlayerMP) o;
			if (!watchingPlayers.contains(player)) {
				newPlayers.add(player);
				watchingPlayers.add(player);
			}
		}
		return newPlayers;
	}

	public void onTick() {
		if (!worldObj.isRemote) {
			balloonManager.onPostTick();
			for(Entity e:queuedEntitiesToMount){
				if(e != null){
					e.startRiding(this.wrapper);
				}
			}
			queuedEntitiesToMount.clear();
		}
	}

	public void queueForce(PhysicsQueuedForce toQueue) {
		queuedPhysForces.add(toQueue);
	}

	public void onPostTick() {
		tickQueuedForces();

		explodedPositionsThisTick.clear();
	}

	// Returns true if splitting happened
	/*
	 * public boolean processPotentialSplitting(){ if(blocksChanged){ blocksChanged = false; }else{ return false; }
	 *
	 * ArrayList<BlockPos> dirtyBlockPositions = new ArrayList(blockPositions); if(dirtyBlockPositions.size()==0){ return false; }
	 *
	 * boolean hasSplit = false;
	 *
	 * while(dirtyBlockPositions.size()!=0){ BlockPos pos = dirtyBlockPositions.get(0); SpatialDetector firstDet = new ShipBlockPosFinder(pos, worldObj, dirtyBlockPositions.size(), true);
	 *
	 * if(firstDet.foundSet.size()!=dirtyBlockPositions.size()){ //Set Y to 300 to prevent picking up extra blocks PhysicsWrapperEntity newSplit = new PhysicsWrapperEntity(worldObj,wrapper.posX,300,wrapper.posZ, null,DetectorManager.DetectorIDs.BlockPosFinder.ordinal()); newSplit.yaw = wrapper.yaw; newSplit.pitch = wrapper.pitch; newSplit.roll = wrapper.roll; newSplit.posX = wrapper.posX; newSplit.posY = wrapper.posY; newSplit.posZ = wrapper.posZ; TIntIterator iter = firstDet.foundSet.iterator();
	 *
	 * BlockPos oldBlockCenter = this.getRegionCenter(); BlockPos newBlockCenter = newSplit.wrapping.getRegionCenter(); BlockPos centerDif = newBlockCenter.subtract(oldBlockCenter);
	 *
	 * ValkyrienWarfareMod.physicsManager.onShipLoad(newSplit);
	 *
	 * newSplit.wrapping.fromSplit = true;
	 *
	 * while(iter.hasNext()){ int hash = iter.next(); BlockPos fromHash = SpatialDetector.getPosWithRespectTo(hash, pos); dirtyBlockPositions.remove(fromHash); CallRunner.onSetBlockState(worldObj, fromHash.add(centerDif), VKChunkCache.getBlockState(fromHash), 3); CallRunner.onSetBlockState(worldObj, fromHash, Blocks.AIR.getDefaultState(), 2); }
	 *
	 * newSplit.wrapping.centerCoord = new Vector(centerCoord); newSplit.wrapping.centerCoord.X+=centerDif.getX(); newSplit.wrapping.centerCoord.Y+=centerDif.getY(); newSplit.wrapping.centerCoord.Z+=centerDif.getZ(); newSplit.wrapping.coordTransform.lToWRotation = coordTransform.lToWRotation; newSplit.wrapping.physicsProcessor.updateCenterOfMass(); newSplit.wrapping.coordTransform.updateAllTransforms();
	 *
	 * //TODO: THIS MATH IS NOT EVEN REMOTELY CORRECT!!!!! //Also the moment of inertia is wrong too newSplit.wrapping.physicsProcessor.linearMomentum = new Vector(physicsProcessor.linearMomentum); newSplit.wrapping.physicsProcessor.angularVelocity = new Vector(physicsProcessor.angularVelocity);
	 *
	 * worldObj.spawnEntityInWorld(newSplit);
	 *
	 * hasSplit = true; }else{ dirtyBlockPositions.clear(); }
	 *
	 * }
	 *
	 * return hasSplit; }
	 */

	public void tickQueuedForces() {
		for (int i = 0; i < queuedPhysForces.size(); i++) {
			PhysicsQueuedForce queue = queuedPhysForces.get(i);
			if (queue.ticksToApply <= 0) {
				queuedPhysForces.remove(i);
				i--;
			}
			queue.ticksToApply--;
		}
	}

	public void onPostTickClient() {
		wrapper.prevPitch = wrapper.pitch;
		wrapper.prevYaw = wrapper.yaw;
		wrapper.prevRoll = wrapper.roll;

		wrapper.lastTickPosX = wrapper.posX;
		wrapper.lastTickPosY = wrapper.posY;
		wrapper.lastTickPosZ = wrapper.posZ;

		lastTickCenterCoord = centerCoord;

		ShipTransformData toUse = coordTransform.stack.getDataForTick(lastMessageTick);

		if (toUse != null) {
			lastMessageTick = toUse.relativeTick;

			Vector CMDif = toUse.centerOfRotation.getSubtraction(centerCoord);
			RotationMatrices.applyTransform(coordTransform.lToWRotation, CMDif);

			wrapper.lastTickPosX -= CMDif.X;
			wrapper.lastTickPosY -= CMDif.Y;
			wrapper.lastTickPosZ -= CMDif.Z;
			toUse.applyToPhysObject(this);
		}
		coordTransform.setPrevMatrices();
		coordTransform.updateAllTransforms();
	}

	public void updateChunkCache() {
		BlockPos min = new BlockPos(collisionBB.minX, Math.max(collisionBB.minY, 0), collisionBB.minZ);
		BlockPos max = new BlockPos(collisionBB.maxX, Math.min(collisionBB.maxY, 255), collisionBB.maxZ);
		surroundingWorldChunksCache = new ChunkCache(worldObj, min, max, 0);
	}

	public void loadClaimedChunks() {
		claimedChunks = new Chunk[(ownedChunks.radius * 2) + 1][(ownedChunks.radius * 2) + 1];
		claimedChunksEntries = new PlayerChunkMapEntry[(ownedChunks.radius * 2) + 1][(ownedChunks.radius * 2) + 1];
		for (int x = ownedChunks.minX; x <= ownedChunks.maxX; x++) {
			for (int z = ownedChunks.minZ; z <= ownedChunks.maxZ; z++) {
				Chunk chunk = worldObj.getChunkFromChunkCoords(x, z);
				if (chunk == null) {
					System.out.println("Just a loaded a null chunk");
					chunk = new Chunk(worldObj, x, z);
				}
				// Do this to get it re-integrated into the world
				if (!worldObj.isRemote) {
					injectChunkIntoWorld(chunk, x, z, false);
				}
				claimedChunks[x - ownedChunks.minX][z - ownedChunks.minZ] = chunk;
			}
		}
		VKChunkCache = new VWChunkCache(worldObj, claimedChunks);
		refrenceBlockPos = getRegionCenter();
		coordTransform = new CoordTransformObject(this);
		if (!worldObj.isRemote) {
			physicsProcessor = new PhysicsCalculations(this);
		}
		detectBlockPositions();
		coordTransform.updateAllTransforms();
	}

	// Generates the blockPos array; must be loaded DIRECTLY after the chunks are setup
	public void detectBlockPositions() {
		// int minChunkX = claimedChunks[0][0].xPosition;
		// int minChunkZ = claimedChunks[0][0].zPosition;
		int chunkX, chunkZ, index, x, y, z;
		Chunk chunk;
		ExtendedBlockStorage storage;
		for (chunkX = claimedChunks.length - 1; chunkX > -1; chunkX--) {
			for (chunkZ = claimedChunks[0].length - 1; chunkZ > -1; chunkZ--) {
				chunk = claimedChunks[chunkX][chunkZ];
				if (chunk != null && ownedChunks.chunkOccupiedInLocal[chunkX][chunkZ]) {
					for (index = 0; index < 16; index++) {
						storage = chunk.getBlockStorageArray()[index];
						if (storage != null) {
							for (y = 0; y < 16; y++) {
								for (x = 0; x < 16; x++) {
									for (z = 0; z < 16; z++) {
										if (storage.data.storage.getAt(y << 8 | z << 4 | x) != ValkyrienWarfareMod.airStateIndex) {
											BlockPos pos = new BlockPos(chunk.xPosition * 16 + x, index * 16 + y, chunk.zPosition * 16 + z);
											blockPositions.add(pos);
											if (!worldObj.isRemote) {
												if (BlockForce.basicForces.isBlockProvidingForce(worldObj.getBlockState(pos), pos, worldObj)) {
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

	public boolean ownsChunk(int chunkX, int chunkZ) {
		return ownedChunks.isChunkEnclosedInSet(chunkX, chunkZ);
	}

	private static final void grabPlayerField() {
		if (playersField == null) {
			try {
				if (!ValkyrienWarfarePlugin.isObfuscatedEnvironment) {
					playersField = PlayerChunkMapEntry.class.getDeclaredField("players");
				} else {
					playersField = PlayerChunkMapEntry.class.getDeclaredField("field_187283_c");
				}
				playersField.setAccessible(true);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}
		}
	}

	public void queueEntityForMounting(Entity toMount){
		queuedEntitiesToMount.add(toMount);
	}

	/**
	 * ONLY USE THESE 2 METHODS TO EVER ADD/REMOVE ENTITIES, OTHERWISE YOU'LL RUIN EVERYTHING!
	 *
	 * @param toFix
	 * @param posInLocal
	 */
	public void fixEntity(Entity toFix, Vector posInLocal) {
		EntityFixMessage entityFixingMessage = new EntityFixMessage(wrapper, toFix, true, posInLocal);
		for (EntityPlayerMP watcher : watchingPlayers) {
			ValkyrienWarfareControlMod.controlNetwork.sendTo(entityFixingMessage, watcher);
		}
		entityLocalPositions.put(toFix.getPersistentID().hashCode(), posInLocal);
	}

	/**
	 * ONLY USE THESE 2 METHODS TO EVER ADD/REMOVE ENTITIES, OTHERWISE YOU'LL RUIN EVERYTHING!
	 *
	 * @param toFix
	 * @param posInLocal
	 */
	public void unFixEntity(Entity toUnfix) {
		EntityFixMessage entityUnfixingMessage = new EntityFixMessage(wrapper, toUnfix, false, null);
		for (EntityPlayerMP watcher : watchingPlayers) {
			ValkyrienWarfareControlMod.controlNetwork.sendTo(entityUnfixingMessage, watcher);
		}
		entityLocalPositions.remove(toUnfix.getPersistentID().hashCode());
	}

	public void fixEntityUUID(int uuidHash, Vector localPos) {
		entityLocalPositions.put(uuidHash, localPos);
	}

	public void removeEntityUUID(int uuidHash) {
		entityLocalPositions.remove(uuidHash);
	}

	public boolean isEntityFixed(Entity toCheck){
		return entityLocalPositions.containsKey(toCheck.getPersistentID().hashCode());
	}

	public Vector getLocalPositionForEntity(Entity getPositionFor) {
		int uuidHash = getPositionFor.getPersistentID().hashCode();
		return entityLocalPositions.get(uuidHash);
	}

	public void writeToNBTTag(NBTTagCompound compound) {
		ownedChunks.writeToNBT(compound);
		NBTUtils.writeVectorToNBT("c", centerCoord, compound);
		compound.setDouble("pitch", wrapper.pitch);
		compound.setDouble("yaw", wrapper.yaw);
		compound.setDouble("roll", wrapper.roll);
		compound.setBoolean("doPhysics", doPhysics);
		for (int row = 0; row < ownedChunks.chunkOccupiedInLocal.length; row++) {
			boolean[] curArray = ownedChunks.chunkOccupiedInLocal[row];
			for (int column = 0; column < curArray.length; column++) {
				compound.setBoolean("CC:" + row + ":" + column, curArray[column]);
			}
		}
		NBTUtils.writeEntityPositionHashMapToNBT("entityPosHashMap", entityLocalPositions, compound);
		physicsProcessor.writeToNBTTag(compound);
		pilotingController.writeToNBTTag(compound);

		Iterator<String> iter = allowedUsers.iterator();
		StringBuilder result = new StringBuilder("");
		while (iter.hasNext()) {
			result.append(iter.next() + (iter.hasNext() ? ";" : ""));
		}
		compound.setString("allowedUsers", result.toString());
		compound.setString("owner", creator);
	}

	public void readFromNBTTag(NBTTagCompound compound) {
		ownedChunks = new ChunkSet(compound);
		lastTickCenterCoord = centerCoord = NBTUtils.readVectorFromNBT("c", compound);
		wrapper.pitch = compound.getDouble("pitch");
		wrapper.yaw = compound.getDouble("yaw");
		wrapper.roll = compound.getDouble("roll");
		doPhysics = compound.getBoolean("doPhysics");
		for (int row = 0; row < ownedChunks.chunkOccupiedInLocal.length; row++) {
			boolean[] curArray = ownedChunks.chunkOccupiedInLocal[row];
			for (int column = 0; column < curArray.length; column++) {
				curArray[column] = compound.getBoolean("CC:" + row + ":" + column);
			}
		}
		loadClaimedChunks();
		entityLocalPositions = NBTUtils.readEntityPositionMap("entityPosHashMap", compound);
		physicsProcessor.readFromNBTTag(compound);
		pilotingController.readFromNBTTag(compound);

		String[] toAllow = compound.getString("allowedUsers").split(";");
		for (String s : toAllow) {
			allowedUsers.add(s);
		}

		creator = compound.getString("owner");
		for (int x = ownedChunks.minX; x <= ownedChunks.maxX; x++) {
			for (int z = ownedChunks.minZ; z <= ownedChunks.maxZ; z++) {
				worldObj.getChunkFromChunkCoords(x, z);
			}
		}
	}

	public void readSpawnData(ByteBuf additionalData) {
		PacketBuffer modifiedBuffer = new PacketBuffer(additionalData);

		ownedChunks = new ChunkSet(modifiedBuffer.readInt(), modifiedBuffer.readInt(), modifiedBuffer.readInt());

		wrapper.posX = modifiedBuffer.readDouble();
		wrapper.posY = modifiedBuffer.readDouble();
		wrapper.posZ = modifiedBuffer.readDouble();

		wrapper.pitch = modifiedBuffer.readDouble();
		wrapper.yaw = modifiedBuffer.readDouble();
		wrapper.roll = modifiedBuffer.readDouble();

		wrapper.prevPitch = wrapper.pitch;
		wrapper.prevYaw = wrapper.yaw;
		wrapper.prevRoll = wrapper.roll;

		wrapper.lastTickPosX = wrapper.posX;
		wrapper.lastTickPosY = wrapper.posY;
		wrapper.lastTickPosZ = wrapper.posZ;

		centerCoord = new Vector(modifiedBuffer);
		for (boolean[] array : ownedChunks.chunkOccupiedInLocal) {
			for (int i = 0; i < array.length; i++) {
				array[i] = modifiedBuffer.readBoolean();
			}
		}
		loadClaimedChunks();
		renderer.updateOffsetPos(refrenceBlockPos);

		coordTransform.stack.pushMessage(new PhysWrapperPositionMessage(this));

		try {
			NBTTagCompound entityFixedPositionNBT = modifiedBuffer.readNBTTagCompoundFromBuffer();
			entityLocalPositions = NBTUtils.readEntityPositionMap("entityFixedPosMap", entityFixedPositionNBT);
			// if(worldObj.isRemote){
			// System.out.println(entityLocalPositions.containsKey(Minecraft.getMinecraft().thePlayer.getPersistentID().hashCode()));
			// System.out.println(Minecraft.getMinecraft().thePlayer.getPersistentID().hashCode());
			// }
		} catch (IOException e) {
			System.err.println("Couldn't load the entityFixedPosNBT; this is really bad.");
			e.printStackTrace();
		}
	}

	public void writeSpawnData(ByteBuf buffer) {
		PacketBuffer modifiedBuffer = new PacketBuffer(buffer);

		modifiedBuffer.writeInt(ownedChunks.centerX);
		modifiedBuffer.writeInt(ownedChunks.centerZ);
		modifiedBuffer.writeInt(ownedChunks.radius);

		modifiedBuffer.writeDouble(wrapper.posX);
		modifiedBuffer.writeDouble(wrapper.posY);
		modifiedBuffer.writeDouble(wrapper.posZ);

		modifiedBuffer.writeDouble(wrapper.pitch);
		modifiedBuffer.writeDouble(wrapper.yaw);
		modifiedBuffer.writeDouble(wrapper.roll);
		centerCoord.writeToByteBuf(modifiedBuffer);
		for (boolean[] array : ownedChunks.chunkOccupiedInLocal) {
			for (boolean b : array) {
				modifiedBuffer.writeBoolean(b);
			}
		}

		NBTTagCompound entityFixedPositionNBT = new NBTTagCompound();
		NBTUtils.writeEntityPositionHashMapToNBT("entityFixedPosMap", entityLocalPositions, entityFixedPositionNBT);
		modifiedBuffer.writeNBTTagCompoundToBuffer(entityFixedPositionNBT);
	}

	/**
	 * Tries to change the owner of this PhysicsObject.
	 *
	 * @param newOwner
	 * @return
	 */
	public EnumChangeOwnerResult changeOwner(EntityPlayer newOwner) {
		if (!ValkyrienWarfareMod.canChangeAirshipCounter(true, newOwner)) {
			return EnumChangeOwnerResult.ERROR_NEWOWNER_NOT_ENOUGH;
		}

		if (newOwner.entityUniqueID.toString().equals(creator))	{
			return EnumChangeOwnerResult.ALREADY_CLAIMED;
		}

		EntityPlayer player = null;
		try {
			player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(UUID.fromString(creator));
		} catch (NullPointerException e)	{
			newOwner.addChatMessage(new TextComponentString("That airship doesn't have an owner, you get to have it :D"));
			newOwner.getCapability(ValkyrienWarfareMod.airshipCounter, null).onCreate();
			allowedUsers.clear();
			creator = newOwner.entityUniqueID.toString();
			return EnumChangeOwnerResult.SUCCESS;
		}

		if (player != null) {
			player.getCapability(ValkyrienWarfareMod.airshipCounter, null).onLose();
		} else {
			try {
				File f = new File(DimensionManager.getCurrentSaveRootDirectory(), "playerdata/" + creator + ".dat");
				NBTTagCompound tag = CompressedStreamTools.read(f);
				NBTTagCompound capsTag = tag.getCompoundTag("ForgeCaps");
				capsTag.setInteger("valkyrienwarfare:IAirshipCounter", capsTag.getInteger("valkyrienwarfare:IAirshipCounter") - 1);
				CompressedStreamTools.safeWrite(tag, f);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		newOwner.getCapability(ValkyrienWarfareMod.airshipCounter, null).onCreate();

		allowedUsers.clear();

		creator = newOwner.entityUniqueID.toString();
		return EnumChangeOwnerResult.SUCCESS;
	}

}
