/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2018 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package valkyrienwarfare.physics.management;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

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
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.addon.control.ValkyrienWarfareControl;
import valkyrienwarfare.addon.control.balloon.ShipBalloonManager;
import valkyrienwarfare.addon.control.network.EntityFixMessage;
import valkyrienwarfare.addon.control.nodenetwork.INodeProvider;
import valkyrienwarfare.addon.control.nodenetwork.Node;
import valkyrienwarfare.api.EnumChangeOwnerResult;
import valkyrienwarfare.api.RotationMatrices;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.api.block.ethercompressor.TileEntityEtherCompressor;
import valkyrienwarfare.mod.BlockPhysicsRegistration;
import valkyrienwarfare.mod.client.render.PhysObjectRenderManager;
import valkyrienwarfare.mod.network.PhysWrapperPositionMessage;
import valkyrienwarfare.mod.physmanagement.chunk.ChunkSet;
import valkyrienwarfare.mod.physmanagement.relocation.DetectorManager;
import valkyrienwarfare.mod.physmanagement.relocation.SpatialDetector;
import valkyrienwarfare.mod.physmanagement.relocation.VWChunkCache;
import valkyrienwarfare.mod.schematics.SchematicReader.Schematic;
import valkyrienwarfare.physics.calculations.PhysicsCalculations;
import valkyrienwarfare.physics.calculations.PhysicsCalculationsManualControl;
import valkyrienwarfare.physics.calculations.PhysicsCalculationsOrbital;
import valkyrienwarfare.physics.data.BlockForce;
import valkyrienwarfare.physics.data.PhysicsQueuedForce;
import valkyrienwarfare.physics.data.ShipTransformData;
import valkyrienwarfare.util.NBTUtils;

public class PhysicsObject {

	public final World worldObj;
	public final PhysicsWrapperEntity wrapper;
	// This handles sending packets to players involving block changes in the Ship
	// space
	public final List<EntityPlayerMP> watchingPlayers;
	public final List<EntityPlayerMP> newWatchers;

	// It is from this position that the x,y,z coords in local are 0; and that the
	// posX,
	// posY and posZ align with in the global coords
	public BlockPos refrenceBlockPos;
	public Vector centerCoord, lastTickCenterCoord;
	public CoordTransformObject coordTransform;
	public PhysObjectRenderManager renderer;
	public PhysicsCalculations physicsProcessor;
	public Set<BlockPos> blockPositions;
	private AxisAlignedBB collisionBB;

	public final List<PhysicsQueuedForce> queuedPhysForces;
	public final List<BlockPos> explodedPositionsThisTick;
	public boolean doPhysics = true;

	public String creator;
	public final PhysCollisionCallable collisionCallable;
    public int lastMessageTick;
    public int detectorID;

    // The closest Chunks to the Ship cached in here
    public ChunkCache surroundingWorldChunksCache;
    // TODO: Make for re-organizing these to make Ship sizes Dynamic
    public ChunkSet ownedChunks;
    // Used for faster memory access to the Chunks this object 'owns'
    public Chunk[][] claimedChunks;
    public VWChunkCache VKChunkCache;
    // Some badly written mods use these Maps to determine who to send packets to,
    // so we need to manually fill them with nearby players
    public PlayerChunkMapEntry[][] claimedChunksEntries;
    public final ShipBalloonManager balloonManager;
    public Map<Integer, Vector> entityLocalPositions;
    public final List<String> allowedUsers;
    // This is used to delay mountEntity() operations by 1 tick
    public final List<Entity> queuedEntitiesToMount;
    // Compatibility for ships made before the update
    public boolean claimedChunksInMap;
    public boolean isNameCustom;
    private boolean blocksChanged;
	private ShipType shipType;

	// public HashSet<nodenetwork> nodeNetworks = new HashSet<nodenetwork>();
	public Set<Node> nodesWithinShip = new HashSet<Node>();

	public PhysicsObject(PhysicsWrapperEntity host) {
		wrapper = host;
		worldObj = host.world;
		if (host.world.isRemote) {
		    balloonManager = null;
			renderer = new PhysObjectRenderManager(this);
		} else {
			balloonManager = new ShipBalloonManager(this);
			renderer = null;
		}
		isNameCustom = false;
		claimedChunksInMap = false;
		queuedEntitiesToMount = new ArrayList<Entity>();
		allowedUsers = new ArrayList<String>();
		entityLocalPositions = new HashMap<Integer, Vector>();
		blocksChanged = false;
		blockPositions = new HashSet<BlockPos>();
		collisionBB = PhysicsWrapperEntity.ZERO_AABB;
		collisionCallable = new PhysCollisionCallable(this);
		explodedPositionsThisTick = new ArrayList<BlockPos>();
		queuedPhysForces = new ArrayList<PhysicsQueuedForce>();
		newWatchers = new ArrayList<EntityPlayerMP>();
		watchingPlayers = new ArrayList<EntityPlayerMP>();
	}

	public void onSetBlockState(IBlockState oldState, IBlockState newState, BlockPos posAt) {
		// If the block here is not to be physicsed, just treat it like you'd treat AIR
		// blocks.
		if (oldState != null && BlockPhysicsRegistration.blocksToNotPhysicise.contains(oldState.getBlock())) {
			oldState = Blocks.AIR.getDefaultState();
		}
		if (newState != null && BlockPhysicsRegistration.blocksToNotPhysicise.contains(newState.getBlock())) {
			newState = Blocks.AIR.getDefaultState();
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
			if (!worldObj.isRemote) {
				balloonManager.onBlockPositionAdded(posAt);
				blockPositions.add(posAt);
			} else {
				if (!blockPositions.contains(posAt)) {
					blockPositions.add(posAt);
				}
			}
			int chunkX = (posAt.getX() >> 4) - claimedChunks[0][0].x;
			int chunkZ = (posAt.getZ() >> 4) - claimedChunks[0][0].z;
			ownedChunks.chunkOccupiedInLocal[chunkX][chunkZ] = true;
		}

		if (blockPositions.isEmpty()) {
			try {
				if (!worldObj.isRemote) {
					if (creator != null) {
						EntityPlayer player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList()
								.getPlayerByUUID(UUID.fromString(creator));
						if (player != null) {
							player.getCapability(ValkyrienWarfareMod.airshipCounter, null).onLose();
						} else {
							try {
								File f = new File(DimensionManager.getCurrentSaveRootDirectory(),
										"playerdata/" + creator + ".dat");
								NBTTagCompound tag = CompressedStreamTools.read(f);
								NBTTagCompound capsTag = tag.getCompoundTag("ForgeCaps");
								capsTag.setInteger("valkyrienwarfare:IAirshipCounter",
										capsTag.getInteger("valkyrienwarfare:IAirshipCounter") - 1);
								CompressedStreamTools.safeWrite(tag, f);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						ValkyrienWarfareMod.chunkManager.getManagerForWorld(worldObj).data.avalibleChunkKeys
								.add(ownedChunks.centerX);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			destroy();
		}

		if (!worldObj.isRemote) {
			if (physicsProcessor != null) {
				physicsProcessor.onSetBlockState(oldState, newState, posAt);
			}
		}

		// System.out.println(blockPositions.size() + ":" + wrapper.isDead);
	}

    public void destroy() {
        wrapper.setDead();
        List<EntityPlayerMP> watchersCopy = new ArrayList<EntityPlayerMP>(watchingPlayers);
        for (int x = ownedChunks.minX; x <= ownedChunks.maxX; x++) {
            for (int z = ownedChunks.minZ; z <= ownedChunks.maxZ; z++) {
                SPacketUnloadChunk unloadPacket = new SPacketUnloadChunk(x, z);
                for (EntityPlayerMP wachingPlayer : watchersCopy) {
                    wachingPlayer.connection.sendPacket(unloadPacket);
                }
            }
            // NOTICE: This method isnt being called to avoid the
            // watchingPlayers.remove(player) call, which is a waste of CPU time
            // onPlayerUntracking(wachingPlayer);
        }
		watchingPlayers.clear();
		ValkyrienWarfareMod.chunkManager.removeRegistedChunksForShip(wrapper);
		ValkyrienWarfareMod.chunkManager.removeShipPosition(wrapper);
		ValkyrienWarfareMod.chunkManager.removeShipNameRegistry(wrapper);
		ValkyrienWarfareMod.physicsManager.onShipUnload(wrapper);
	}

	public void claimNewChunks(int radius) {
		ownedChunks = ValkyrienWarfareMod.chunkManager.getManagerForWorld(wrapper.world)
				.getNextAvaliableChunkSet(radius);
		ValkyrienWarfareMod.chunkManager.registerChunksForShip(wrapper);
		claimedChunksInMap = true;
	}

	/**
	 * Generates the new chunks
	 */
	public void processChunkClaims(EntityPlayer player) {
		BlockPos centerInWorld = new BlockPos(wrapper.posX, wrapper.posY, wrapper.posZ);
		SpatialDetector detector = DetectorManager.getDetectorFor(detectorID, centerInWorld, worldObj,
				ValkyrienWarfareMod.maxShipSize + 1, true);
		if (detector.foundSet.size() > ValkyrienWarfareMod.maxShipSize || detector.cleanHouse) {
			if (player != null) {
				player.sendMessage(new TextComponentString(
						"Ship construction canceled because its exceeding the ship size limit (Raise with /physSettings maxShipSize <number>) ; Or because it's attatched to bedrock)"));
			}
			wrapper.setDead();
			return;
		}
		assembleShip(player, detector, centerInWorld);
	}

	public void processChunkClaims(Schematic toFollow) {
		BlockPos centerInWorld = new BlockPos(-(toFollow.width / 2), 128 - (toFollow.height / 2),
				-(toFollow.length / 2));

		int radiusNeeded = (Math.max(toFollow.length, toFollow.width) / 16) + 2;

		// System.out.println(radiusNeeded);

		claimNewChunks(radiusNeeded);

		ValkyrienWarfareMod.physicsManager.onShipPreload(wrapper);

		claimedChunks = new Chunk[(ownedChunks.radius * 2) + 1][(ownedChunks.radius * 2) + 1];
		claimedChunksEntries = new PlayerChunkMapEntry[(ownedChunks.radius * 2) + 1][(ownedChunks.radius * 2) + 1];
		for (int x = ownedChunks.minX; x <= ownedChunks.maxX; x++) {
			for (int z = ownedChunks.minZ; z <= ownedChunks.maxZ; z++) {
				Chunk chunk = new Chunk(worldObj, x, z);
				injectChunkIntoWorld(chunk, x, z, true);
				claimedChunks[x - ownedChunks.minX][z - ownedChunks.minZ] = chunk;
			}
		}

		replaceOuterChunksWithAir();

		VKChunkCache = new VWChunkCache(worldObj, claimedChunks);

		refrenceBlockPos = getRegionCenter();
		centerCoord = new Vector(refrenceBlockPos.getX(), refrenceBlockPos.getY(), refrenceBlockPos.getZ());

		createPhysicsCalculations();
		// The ship just got build, how can it not be the latest?
		physicsProcessor.isShipPastBuild91 = true;

		BlockPos centerDifference = refrenceBlockPos.subtract(centerInWorld);

		toFollow.placeBlockAndTilesInWorld(worldObj, centerDifference);

		detectBlockPositions();

		// TODO: This fixes the lighting, but it adds lag; maybe remove this
		for (int x = ownedChunks.minX; x <= ownedChunks.maxX; x++) {
			for (int z = ownedChunks.minZ; z <= ownedChunks.maxZ; z++) {
				// claimedChunks[x - ownedChunks.minX][z - ownedChunks.minZ].isTerrainPopulated
				// = true;
				// claimedChunks[x - ownedChunks.minX][z -
				// ownedChunks.minZ].generateSkylightMap();
				// claimedChunks[x - ownedChunks.minX][z - ownedChunks.minZ].checkLight();
			}
		}

		coordTransform = new CoordTransformObject(this);
		physicsProcessor.processInitialPhysicsData();
		physicsProcessor.updateParentCenterOfMass();

		coordTransform.updateAllTransforms();
	}

	/**
	 * Creates the PhysicsProcessor object before any data gets loaded into it; can
	 * be overridden to change the class of the Object
	 */
	private void createPhysicsCalculations() {
		if (physicsProcessor == null) {
			if (shipType == ShipType.Oribtal || shipType == ShipType.Semi_Unlocked_Orbital) {
				physicsProcessor = new PhysicsCalculationsOrbital(this);
			} else {
				if (shipType == ShipType.Zepplin || shipType == ShipType.Dungeon_Sky) {
					physicsProcessor = new PhysicsCalculationsManualControl(this);
				} else {
					physicsProcessor = new PhysicsCalculations(this);
				}
			}
		}
	}

	private void assembleShip(EntityPlayer player, SpatialDetector detector, BlockPos centerInWorld) {
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

		// radiusNeeded = math.max(radiusNeeded, 5);

		iter = detector.foundSet.iterator();

		radiusNeeded = Math.min(radiusNeeded,
				ValkyrienWarfareMod.chunkManager.getManagerForWorld(wrapper.world).maxChunkRadius);

		// System.out.println(radiusNeeded);

		claimNewChunks(radiusNeeded);

		ValkyrienWarfareMod.physicsManager.onShipPreload(wrapper);

		claimedChunks = new Chunk[(ownedChunks.radius * 2) + 1][(ownedChunks.radius * 2) + 1];
		claimedChunksEntries = new PlayerChunkMapEntry[(ownedChunks.radius * 2) + 1][(ownedChunks.radius * 2) + 1];
		for (int x = ownedChunks.minX; x <= ownedChunks.maxX; x++) {
			for (int z = ownedChunks.minZ; z <= ownedChunks.maxZ; z++) {
				Chunk chunk = new Chunk(worldObj, x, z);
				injectChunkIntoWorld(chunk, x, z, true);
				claimedChunks[x - ownedChunks.minX][z - ownedChunks.minZ] = chunk;
			}
		}

		// Prevents weird shit from spawning at the edges of a ship
		replaceOuterChunksWithAir();

		VKChunkCache = new VWChunkCache(worldObj, claimedChunks);
		int minChunkX = claimedChunks[0][0].x;
		int minChunkZ = claimedChunks[0][0].z;

		refrenceBlockPos = getRegionCenter();
		centerCoord = new Vector(refrenceBlockPos.getX(), refrenceBlockPos.getY(), refrenceBlockPos.getZ());

		createPhysicsCalculations();
		// The ship just got build, how can it not be the latest?
		physicsProcessor.isShipPastBuild91 = true;

		BlockPos centerDifference = refrenceBlockPos.subtract(centerInWorld);
		while (iter.hasNext()) {
			int i = iter.next();
			detector.setPosWithRespectTo(i, centerInWorld, pos);

			IBlockState state = detector.cache.getBlockState(pos);

			TileEntity worldTile = detector.cache.getTileEntity(pos);

			pos.setPos(pos.getX() + centerDifference.getX(), pos.getY() + centerDifference.getY(),
					pos.getZ() + centerDifference.getZ());
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
				// Change the block position to be inside of the Ship
				tileEntNBT.setInteger("x", pos.getX());
				tileEntNBT.setInteger("y", pos.getY());
				tileEntNBT.setInteger("z", pos.getZ());

				// Translates the Node connections from World space into Ship space
				if (worldTile instanceof INodeProvider) {
					int[] backingPositionArray = tileEntNBT.getIntArray("connectednodesarray");
					for (int cont = 0; cont < backingPositionArray.length; cont += 3) {
						backingPositionArray[cont] = backingPositionArray[cont] + centerDifference.getX();
						backingPositionArray[cont + 1] = backingPositionArray[cont + 1] + centerDifference.getY();
						backingPositionArray[cont + 2] = backingPositionArray[cont + 2] + centerDifference.getZ();
					}
					tileEntNBT.setIntArray("connectednodesarray", backingPositionArray);
				}

				// TODO: Remove this later
				if (worldTile instanceof TileEntityEtherCompressor) {
					int controllerPosX = tileEntNBT.getInteger("controllerPosX");
					int controllerPosY = tileEntNBT.getInteger("controllerPosY");
					int controllerPosZ = tileEntNBT.getInteger("controllerPosZ");

					tileEntNBT.setInteger("controllerPosX", controllerPosX + centerDifference.getX());
					tileEntNBT.setInteger("controllerPosY", controllerPosY + centerDifference.getY());
					tileEntNBT.setInteger("controllerPosZ", controllerPosZ + centerDifference.getZ());
				}

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
								int hash = detector.getHashWithRespectTo(inTilePos.getX(), inTilePos.getY(),
										inTilePos.getZ(), detector.firstBlock);
								if (detector.foundSet.contains(hash)) {
									if (!(o instanceof MutableBlockPos)) {
										inTilePos = inTilePos.add(centerDifference.getX(), centerDifference.getY(),
												centerDifference.getZ());
										field.set(newInstance, inTilePos);
									} else {
										MutableBlockPos mutable = (MutableBlockPos) o;
										mutable.setPos(inTilePos.getX() + centerDifference.getX(),
												inTilePos.getY() + centerDifference.getY(),
												inTilePos.getZ() + centerDifference.getZ());
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

				// TODO: Maybe move this after the setTileEntity() method
				if (newInstance instanceof INodeProvider) {
					((INodeProvider) newInstance).getNode().updateParentEntity(this);
				}

				worldObj.setTileEntity(newInstance.getPos(), newInstance);

				if (newInstance instanceof INodeProvider) {
					this.nodesWithinShip.add(((INodeProvider) newInstance).getNode());
				}

				newInstance.markDirty();
			}
			// chunkCache.setBlockState(pos, state);
			// worldObj.setBlockState(pos, state);
		}
		iter = detector.foundSet.iterator();
		while (iter.hasNext()) {
			int i = iter.next();
			// BlockPos respectTo = detector.getPosWithRespectTo(i, centerInWorld);
			detector.setPosWithRespectTo(i, centerInWorld, pos);
			// detector.cache.setBlockState(pos, Blocks.air.getDefaultState());
			// TODO: Get this to update on clientside as well, you bastard!
			TileEntity tile = worldObj.getTileEntity(pos);
			if (tile != null) {
				tile.invalidate();
			}
			worldObj.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
		}
		// centerDifference = new
		// BlockPos(claimedChunks[ownedChunks.radius+1][ownedChunks.radius+1].x*16,128,claimedChunks[ownedChunks.radius+1][ownedChunks.radius+1].z*16);
		// System.out.println(chunkCache.getBlockState(centerDifference).getBlock());

		for (int x = ownedChunks.minX; x <= ownedChunks.maxX; x++) {
			for (int z = ownedChunks.minZ; z <= ownedChunks.maxZ; z++) {
				claimedChunks[x - ownedChunks.minX][z - ownedChunks.minZ].isTerrainPopulated = true;
				claimedChunks[x - ownedChunks.minX][z - ownedChunks.minZ].generateSkylightMap();
				claimedChunks[x - ownedChunks.minX][z - ownedChunks.minZ].checkLight();
			}
		}

		detectBlockPositions();

		// TODO: This fixes the lighting, but it adds lag; maybe remove this
		for (int x = ownedChunks.minX; x <= ownedChunks.maxX; x++) {
			for (int z = ownedChunks.minZ; z <= ownedChunks.maxZ; z++) {
				// claimedChunks[x - ownedChunks.minX][z - ownedChunks.minZ].isTerrainPopulated
				// = true;
				// claimedChunks[x - ownedChunks.minX][z -
				// ownedChunks.minZ].generateSkylightMap();
				// claimedChunks[x - ownedChunks.minX][z - ownedChunks.minZ].checkLight();
			}
		}

		coordTransform = new CoordTransformObject(this);
		physicsProcessor.processInitialPhysicsData();
		physicsProcessor.updateParentCenterOfMass();
	}

	public void injectChunkIntoWorld(Chunk chunk, int x, int z, boolean putInId2ChunkMap) {
		ChunkProviderServer provider = (ChunkProviderServer) worldObj.getChunkProvider();
		// TileEntities will break if you don't do this
		chunk.loaded = true;
		chunk.dirty = true;
		claimedChunks[x - ownedChunks.minX][z - ownedChunks.minZ] = chunk;

		if (putInId2ChunkMap) {
			provider.id2ChunkMap.put(ChunkPos.asLong(x, z), chunk);
		}

		PlayerChunkMap map = ((WorldServer) worldObj).getPlayerChunkMap();

		PlayerChunkMapEntry entry = new PlayerChunkMapEntry(map, x, z) {
			// @Override
			// public boolean hasPlayerMatchingInRange(double range,
			// Predicate<EntityPlayerMP> predicate)
			// {
			// return true;
			// }
		};

		long i = map.getIndex(x, z);

		map.entryMap.put(i, entry);
		map.entries.add(entry);

		entry.sentToPlayers = true;
		entry.players = watchingPlayers;

		claimedChunksEntries[x - ownedChunks.minX][z - ownedChunks.minZ] = entry;

		// Ticket ticket =
		// ValkyrienWarfareMod.physicsManager.getManagerForWorld(this.worldObj).chunkLoadingTicket;

		// MinecraftForge.EVENT_BUS.post(new ForceChunkEvent(ticket, new ChunkPos(x,
		// z)));

		// Laggy as fuck, hell no!
		// ForgeChunkManager.forceChunk(ticket, new ChunkPos(x, z));
		// MinecraftForge.EVENT_BUS.post(new ChunkEvent.Load(chunk));
	}

	// Experimental, could fix issues with random shit generating inside of Ships
	private void replaceOuterChunksWithAir() {
		for (int x = ownedChunks.minX - 1; x <= ownedChunks.maxX + 1; x++) {
			for (int z = ownedChunks.minZ - 1; z <= ownedChunks.maxZ + 1; z++) {
				if (x == ownedChunks.minX - 1 || x == ownedChunks.maxX + 1 || z == ownedChunks.minZ - 1
						|| z == ownedChunks.maxZ + 1) {
					// This is satisfied for the chunks surrounding a Ship, do fill it with empty
					// space
					Chunk chunk = new Chunk(worldObj, x, z);
					ChunkProviderServer provider = (ChunkProviderServer) worldObj.getChunkProvider();
					chunk.dirty = true;
					provider.id2ChunkMap.put(ChunkPos.asLong(x, z), chunk);
				}
			}
		}
	}

	/**
	 * TODO: Add the methods that send the tileEntities in each given chunk
	 */
	public void preloadNewPlayers() {
		Set<EntityPlayerMP> newWatchers = getPlayersThatJustWatched();
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
		return new BlockPos((claimedChunks[ownedChunks.radius + 1][ownedChunks.radius + 1].x * 16) - 8, 127,
				(claimedChunks[ownedChunks.radius + 1][ownedChunks.radius + 1].z * 16) - 8);
	}

	/**
	 * TODO: Make this further get the player to stop all further tracking of those
	 * physObject
	 *
	 * @param untracking EntityPlayer that stopped tracking
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
		} else {
			renderer.killRenderers();
		}
	}

	public void unloadShipChunksFromWorld() {
		ChunkProviderServer provider = (ChunkProviderServer) worldObj.getChunkProvider();
		for (int x = ownedChunks.minX; x <= ownedChunks.maxX; x++) {
			for (int z = ownedChunks.minZ; z <= ownedChunks.maxZ; z++) {
				provider.queueUnload(claimedChunks[x - ownedChunks.minX][z - ownedChunks.minZ]);

				// Ticket ticket =
				// ValkyrienWarfareMod.physicsManager.getManagerForWorld(this.worldObj).chunkLoadingTicket;
				// So fucking laggy!
				// ForgeChunkManager.unforceChunk(manager.chunkLoadingTicket, new ChunkPos(x,
				// z));
				// MinecraftForge.EVENT_BUS.post(new UnforceChunkEvent(ticket, new ChunkPos(x,
				// z)));
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
			for (Entity e : queuedEntitiesToMount) {
				if (e != null) {
					e.startRiding(this.wrapper, true);
				}
			}
			queuedEntitiesToMount.clear();
		}
		// wrapper.isDead = true;
	}

	public void queueForce(PhysicsQueuedForce toQueue) {
		queuedPhysForces.add(toQueue);
	}

	public void onPostTick() {
		tickQueuedForces();
		explodedPositionsThisTick.clear();

		if (!wrapper.isDead && !wrapper.world.isRemote) {
			ValkyrienWarfareMod.chunkManager.updateShipPosition(wrapper);
			if (!claimedChunksInMap) {
				// Old ships not in the map will add themselves in once loaded
				ValkyrienWarfareMod.chunkManager.registerChunksForShip(wrapper);
				System.out.println("Old ship detected, adding to the registered Chunks map");
				claimedChunksInMap = true;
			}
		}
	}

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

			if (this.wrapper.yaw == toUse.yaw && this.wrapper.pitch == toUse.pitch) {
				// System.out.println("Duplicate damnit!");
				// System.out.println(toUse == coordTransform.stack.recentTransforms[0]);
				// toUse = coordTransform.stack.getDataForTick(lastMessageTick);
			}

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
		List<TileEntity> nodeTileEntitiesToUpdate = new ArrayList<TileEntity>();

		ValkyrienWarfareMod.physicsManager.onShipPreload(wrapper);

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
				for (Entry<BlockPos, TileEntity> entry : chunk.tileEntities.entrySet()) {
					TileEntity tile = entry.getValue();
					if (tile instanceof INodeProvider) {
						nodeTileEntitiesToUpdate.add(tile);
					}
				}
				claimedChunks[x - ownedChunks.minX][z - ownedChunks.minZ] = chunk;
			}
		}
		VKChunkCache = new VWChunkCache(worldObj, claimedChunks);
		refrenceBlockPos = getRegionCenter();
		coordTransform = new CoordTransformObject(this);
		if (!worldObj.isRemote) {
			createPhysicsCalculations();
		}
		detectBlockPositions();
		for (TileEntity tile : nodeTileEntitiesToUpdate) {
			Node node = ((INodeProvider) tile).getNode();
			if (node != null) {
				node.updateParentEntity(this);
			} else {
				System.err.println("How the fuck did we get a null node?");
			}
		}
		for (TileEntity tile : nodeTileEntitiesToUpdate) {
			Node node = ((INodeProvider) tile).getNode();
			if (node != null) {
				node.updateBuildState();
			} else {
				System.err.println("How the fuck did we get a null node?");
			}
		}

		coordTransform.updateAllTransforms();
	}

	// Generates the blockPos array; must be loaded DIRECTLY after the chunks are
	// setup
	public void detectBlockPositions() {
		// int minChunkX = claimedChunks[0][0].x;
		// int minChunkZ = claimedChunks[0][0].z;
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
										if (storage.data.storage
												.getAt(y << 8 | z << 4 | x) != ValkyrienWarfareMod.airStateIndex) {
											BlockPos pos = new BlockPos(chunk.x * 16 + x, index * 16 + y,
													chunk.z * 16 + z);
											blockPositions.add(pos);
											if (!worldObj.isRemote) {
												if (BlockForce.basicForces.isBlockProvidingForce(
														worldObj.getBlockState(pos), pos, worldObj)) {
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

	public void queueEntityForMounting(Entity toMount) {
		queuedEntitiesToMount.add(toMount);
	}

	/**
	 * ONLY USE THESE 2 METHODS TO EVER ADD/REMOVE ENTITIES, OTHERWISE YOU'LL RUIN
	 * EVERYTHING!
	 *
	 * @param toFix
	 * @param posInLocal
	 */
	public void fixEntity(Entity toFix, Vector posInLocal) {
		EntityFixMessage entityFixingMessage = new EntityFixMessage(wrapper, toFix, true, posInLocal);
		for (EntityPlayerMP watcher : watchingPlayers) {
			ValkyrienWarfareControl.controlNetwork.sendTo(entityFixingMessage, watcher);
		}
		entityLocalPositions.put(toFix.getPersistentID().hashCode(), posInLocal);
	}

	/**
	 * ONLY USE THESE 2 METHODS TO EVER ADD/REMOVE ENTITIES, OTHERWISE YOU'LL RUIN
	 * EVERYTHING!
	 */
	public void unFixEntity(Entity toUnfix) {
		EntityFixMessage entityUnfixingMessage = new EntityFixMessage(wrapper, toUnfix, false, null);
		for (EntityPlayerMP watcher : watchingPlayers) {
			ValkyrienWarfareControl.controlNetwork.sendTo(entityUnfixingMessage, watcher);
		}
		entityLocalPositions.remove(toUnfix.getPersistentID().hashCode());
	}

	public void fixEntityUUID(int uuidHash, Vector localPos) {
		entityLocalPositions.put(uuidHash, localPos);
	}

	public void removeEntityUUID(int uuidHash) {
		entityLocalPositions.remove(uuidHash);
	}

	public boolean isEntityFixed(Entity toCheck) {
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
		NBTUtils.writeEntityPositionMapToNBT("entityPosHashMap", entityLocalPositions, compound);
		physicsProcessor.writeToNBTTag(compound);

		Iterator<String> iter = allowedUsers.iterator();
		StringBuilder result = new StringBuilder("");
		while (iter.hasNext()) {
			result.append(iter.next() + (iter.hasNext() ? ";" : ""));
		}
		compound.setString("allowedUsers", result.toString());
		compound.setString("owner", creator);
		compound.setBoolean("claimedChunksInMap", claimedChunksInMap);
		compound.setBoolean("isNameCustom", isNameCustom);
		compound.setString("shipType", shipType.name());
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

		String shipTypeName = compound.getString("shipType");
		if (!shipTypeName.equals("")) {
			shipType = ShipType.valueOf(ShipType.class, shipTypeName);
		} else {
			// Assume its an older Ship, and that its fully unlocked
			shipType = ShipType.Full_Unlocked;
		}

		loadClaimedChunks();
		entityLocalPositions = NBTUtils.readEntityPositionMap("entityPosHashMap", compound);
		physicsProcessor.readFromNBTTag(compound);

		String[] toAllow = compound.getString("allowedUsers").split(";");
		for (String s : toAllow) {
			allowedUsers.add(s);
		}

		creator = compound.getString("owner");
		claimedChunksInMap = compound.getBoolean("claimedChunksInMap");
		for (int x = ownedChunks.minX; x <= ownedChunks.maxX; x++) {
			for (int z = ownedChunks.minZ; z <= ownedChunks.maxZ; z++) {
				worldObj.getChunkFromChunkCoords(x, z);
			}
		}

		isNameCustom = compound.getBoolean("isNameCustom");

		wrapper.dataManager.set(PhysicsWrapperEntity.IS_NAME_CUSTOM, isNameCustom);
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
			NBTTagCompound entityFixedPositionNBT = modifiedBuffer.readCompoundTag();
			entityLocalPositions = NBTUtils.readEntityPositionMap("entityFixedPosMap", entityFixedPositionNBT);
			// if(worldObj.isRemote){
			// System.out.println(entityLocalPositions.containsKey(Minecraft.getMinecraft().thePlayer.getPersistentID().hashCode()));
			// System.out.println(Minecraft.getMinecraft().thePlayer.getPersistentID().hashCode());
			// }
		} catch (IOException e) {
			System.err.println("Couldn't load the entityFixedPosNBT; this is really bad.");
			e.printStackTrace();
		}

		isNameCustom = modifiedBuffer.readBoolean();
		shipType = modifiedBuffer.readEnumValue(ShipType.class);
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
		NBTUtils.writeEntityPositionMapToNBT("entityFixedPosMap", entityLocalPositions, entityFixedPositionNBT);
		modifiedBuffer.writeCompoundTag(entityFixedPositionNBT);

		modifiedBuffer.writeBoolean(isNameCustom);
		modifiedBuffer.writeEnumValue(shipType);
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

		if (newOwner.entityUniqueID.toString().equals(creator)) {
			return EnumChangeOwnerResult.ALREADY_CLAIMED;
		}

		EntityPlayer player = null;
		try {
			player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList()
					.getPlayerByUUID(UUID.fromString(creator));
		} catch (NullPointerException e) {
			newOwner.sendMessage(new TextComponentString("That airship doesn't have an owner, you get to have it :D"));
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
				capsTag.setInteger("valkyrienwarfare:IAirshipCounter",
						capsTag.getInteger("valkyrienwarfare:IAirshipCounter") - 1);
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
	
	public void setShipType(ShipType shipType) {
	    this.shipType = shipType;
	}
	
	public ShipType getShipType() {
	    return shipType;
	}

	public AxisAlignedBB getCollisionBoundingBox() {
	    return collisionBB;
	}
	
	public void setCollisionBoundingBox(AxisAlignedBB newCollisionBB) {
	    this.collisionBB = newCollisionBB;
	}
}
