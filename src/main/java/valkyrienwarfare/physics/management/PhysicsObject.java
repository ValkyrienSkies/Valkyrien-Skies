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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
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
import valkyrienwarfare.addon.control.network.EntityFixMessage;
import valkyrienwarfare.addon.control.nodenetwork.INodeProvider;
import valkyrienwarfare.addon.control.nodenetwork.Node;
import valkyrienwarfare.api.EnumChangeOwnerResult;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.api.block.ethercompressor.TileEntityEtherCompressor;
import valkyrienwarfare.math.Quaternion;
import valkyrienwarfare.mod.BlockPhysicsRegistration;
import valkyrienwarfare.mod.client.render.PhysObjectRenderManager;
import valkyrienwarfare.mod.coordinates.ISubspace;
import valkyrienwarfare.mod.coordinates.ISubspaceProvider;
import valkyrienwarfare.mod.coordinates.ImplSubspace;
import valkyrienwarfare.mod.coordinates.ShipTransform;
import valkyrienwarfare.mod.coordinates.ShipTransformationPacketHolder;
import valkyrienwarfare.mod.coordinates.TransformType;
import valkyrienwarfare.mod.network.PhysWrapperPositionMessage;
import valkyrienwarfare.mod.physmanagement.chunk.VWChunkCache;
import valkyrienwarfare.mod.physmanagement.chunk.VWChunkClaim;
import valkyrienwarfare.mod.physmanagement.relocation.DetectorManager;
import valkyrienwarfare.mod.physmanagement.relocation.SpatialDetector;
import valkyrienwarfare.mod.schematics.SchematicReader.Schematic;
import valkyrienwarfare.physics.BlockForce;
import valkyrienwarfare.physics.PhysicsCalculations;
import valkyrienwarfare.physics.PhysicsCalculationsManualControl;
import valkyrienwarfare.util.NBTUtils;

/**
 * The heart and soul of this mod. The physics object does everything from
 * custom collision, block interactions, physics, networking, rendering, and
 * more!
 * 
 * @author thebest108
 *
 */
public class PhysicsObject implements ISubspaceProvider {

	public static final int MIN_TICKS_EXISTED_BEFORE_PHYSICS = 5;
	private final PhysicsWrapperEntity wrapper;
    private final List<EntityPlayerMP> watchingPlayers;
    private PhysObjectRenderManager shipRenderer;
    private final Set<String> allowedUsers;
    private final Set<Node> concurrentNodesWithinShip;
    // This is used to delay mountEntity() operations by 1 tick
    private final List<Entity> queuedEntitiesToMount;
    // Used when rendering to avoid horrible floating point errors, just a random
    // blockpos inside the ship space.
    private BlockPos refrenceBlockPos;
    private Vector centerCoord;
    private ShipTransformationManager shipTransformationManager;
    private PhysicsCalculations physicsProcessor;
	// Has to be concurrent, only exists properly on the server. Do not use this for
	// anything client side!
    private Set<BlockPos> blockPositions;
	private boolean isPhysicsEnabled;
	private String creator;
	private int detectorID;
    // The closest Chunks to the Ship cached in here
	private ChunkCache cachedSurroundingChunks;
    // TODO: Make for re-organizing these to make Ship sizes Dynamic
	private VWChunkClaim ownedChunks;
    // Used for faster memory access to the Chunks this object 'owns'
    private Chunk[][] claimedChunks;
    private VWChunkCache shipChunks;
    // Some badly written mods use these Maps to determine who to send packets to,
    // so we need to manually fill them with nearby players
    private PlayerChunkMapEntry[][] claimedChunksEntries;
    // Compatibility for ships made before the update
    private boolean claimedChunksInMap;
    private boolean isNameCustom;
    private AxisAlignedBB shipBoundingBox;
    private TIntObjectMap<Vector> entityLocalPositions;
    private ShipType shipType;
    private volatile int gameConsecutiveTicks;
    private volatile int physicsConsecutiveTicks;
    private final ISubspace shipSubspace;

    public PhysicsObject(PhysicsWrapperEntity host) {
    	this.wrapper = host;
        if (host.world.isRemote) {
        	this.shipRenderer = new PhysObjectRenderManager(this);
        }
        this.setNameCustom(false);
        this.claimedChunksInMap = false;
        this.queuedEntitiesToMount = new ArrayList<Entity>();
        this.entityLocalPositions = new TIntObjectHashMap<Vector>();
        this.setPhysicsEnabled(false);
        // We need safe access to this across multiple threads.
        this.setBlockPositions(ConcurrentHashMap.<BlockPos>newKeySet());
        this.shipBoundingBox = Entity.ZERO_AABB;
        this.watchingPlayers = new ArrayList<EntityPlayerMP>();
        this.concurrentNodesWithinShip = ConcurrentHashMap.<Node>newKeySet();
        this.isPhysicsEnabled = false;
        this.allowedUsers = new HashSet<String>();
        this.gameConsecutiveTicks = 0;
        this.physicsConsecutiveTicks = 0;
        this.shipSubspace = new ImplSubspace(this);
    }

	public void onSetBlockState(IBlockState oldState, IBlockState newState, BlockPos posAt) {
		if (getWorldObj().isRemote) {
			return;
		}

		if (!getOwnedChunks().isChunkEnclosedInSet(posAt.getX() >> 4, posAt.getZ() >> 4)) {
			return;
		}

		// If the block here is not to be made with physics, just treat it like you'd
		// treat AIR blocks.
		if (oldState != null && BlockPhysicsRegistration.blocksToNotPhysicise.contains(oldState.getBlock())) {
			oldState = Blocks.AIR.getDefaultState();
		}
		if (newState != null && BlockPhysicsRegistration.blocksToNotPhysicise.contains(newState.getBlock())) {
			newState = Blocks.AIR.getDefaultState();
		}

		boolean isOldAir = oldState == null || oldState.getBlock().equals(Blocks.AIR);
		boolean isNewAir = newState == null || newState.getBlock().equals(Blocks.AIR);

		if (isNewAir) {
			getBlockPositions().remove(posAt);
		}

		if ((isOldAir && !isNewAir)) {
			getBlockPositions().add(posAt);
			int chunkX = (posAt.getX() >> 4) - claimedChunks[0][0].x;
			int chunkZ = (posAt.getZ() >> 4) - claimedChunks[0][0].z;
			getOwnedChunks().chunkOccupiedInLocal[chunkX][chunkZ] = true;
		}

		if (getBlockPositions().isEmpty()) {
			try {
				if (getCreator() != null) {
					EntityPlayer player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList()
							.getPlayerByUsername(getCreator());
					if (player != null) {
						player.getCapability(ValkyrienWarfareMod.airshipCounter, null).onLose();
					} else {
						// TODO: Fix this later
						if (false) {
							try {
								File f = new File(DimensionManager.getCurrentSaveRootDirectory(),
										"playerdata/" + getCreator() + ".dat");
								NBTTagCompound tag = CompressedStreamTools.read(f);
								NBTTagCompound capsTag = tag.getCompoundTag("ForgeCaps");
								capsTag.setInteger("valkyrienwarfare:IAirshipCounter",
										capsTag.getInteger("valkyrienwarfare:IAirshipCounter") - 1);
								CompressedStreamTools.safeWrite(tag, f);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
					ValkyrienWarfareMod.chunkManager.getManagerForWorld(getWorldObj()).data.getAvalibleChunkKeys()
							.add(getOwnedChunks().getCenterX());
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			destroy();
		}

		if (getPhysicsProcessor() != null) {
			getPhysicsProcessor().onSetBlockState(oldState, newState, posAt);
		}

		// System.out.println(blockPositions.size() + ":" + wrapper.isDead);
	}

    public void destroy() {
        getWrapperEntity().setDead();
        List<EntityPlayerMP> watchersCopy = new ArrayList<EntityPlayerMP>(getWatchingPlayers());
        for (int x = getOwnedChunks().getMinX(); x <= getOwnedChunks().getMaxX(); x++) {
            for (int z = getOwnedChunks().getMinZ(); z <= getOwnedChunks().getMaxZ(); z++) {
                SPacketUnloadChunk unloadPacket = new SPacketUnloadChunk(x, z);
                for (EntityPlayerMP wachingPlayer : watchersCopy) {
                    wachingPlayer.connection.sendPacket(unloadPacket);
                }
            }
            // NOTICE: This method isnt being called to avoid the
            // watchingPlayers.remove(player) call, which is a waste of CPU time
            // onPlayerUntracking(wachingPlayer);
        }
        getWatchingPlayers().clear();
        ValkyrienWarfareMod.chunkManager.removeRegistedChunksForShip(getWrapperEntity());
        ValkyrienWarfareMod.chunkManager.removeShipPosition(getWrapperEntity());
        ValkyrienWarfareMod.chunkManager.removeShipNameRegistry(getWrapperEntity());
        ValkyrienWarfareMod.physicsManager.onShipUnload(getWrapperEntity());
    }

    public void claimNewChunks(int radius) {
        setOwnedChunks(ValkyrienWarfareMod.chunkManager.getManagerForWorld(getWrapperEntity().world)
                .getNextAvaliableChunkSet(radius));
        ValkyrienWarfareMod.chunkManager.registerChunksForShip(getWrapperEntity());
        claimedChunksInMap = true;
    }

    /**
     * Generates the new chunks
     */
    public void processChunkClaims(EntityPlayer player) {
        BlockPos centerInWorld = new BlockPos(getWrapperEntity().posX, getWrapperEntity().posY, getWrapperEntity().posZ);
        SpatialDetector detector = DetectorManager.getDetectorFor(getDetectorID(), centerInWorld, getWorldObj(),
                ValkyrienWarfareMod.maxShipSize + 1, true);
        if (detector.foundSet.size() > ValkyrienWarfareMod.maxShipSize || detector.cleanHouse) {
            if (player != null) {
                player.sendMessage(new TextComponentString(
                        "Ship construction canceled because its exceeding the ship size limit; or because it's attatched to bedrock. Raise it with /physsettings maxshipsize [number]"));
            }
            getWrapperEntity().setDead();
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

        ValkyrienWarfareMod.physicsManager.onShipPreload(getWrapperEntity());

        claimedChunks = new Chunk[(getOwnedChunks().getRadius() * 2) + 1][(getOwnedChunks().getRadius() * 2) + 1];
        claimedChunksEntries = new PlayerChunkMapEntry[(getOwnedChunks().getRadius() * 2) + 1][(getOwnedChunks().getRadius() * 2) + 1];
        for (int x = getOwnedChunks().getMinX(); x <= getOwnedChunks().getMaxX(); x++) {
            for (int z = getOwnedChunks().getMinZ(); z <= getOwnedChunks().getMaxZ(); z++) {
                Chunk chunk = new Chunk(getWorldObj(), x, z);
                injectChunkIntoWorld(chunk, x, z, true);
                claimedChunks[x - getOwnedChunks().getMinX()][z - getOwnedChunks().getMinZ()] = chunk;
            }
        }

        replaceOuterChunksWithAir();

        setShipChunks(new VWChunkCache(getWorldObj(), claimedChunks));

        setRefrenceBlockPos(getRegionCenter());
        setCenterCoord(new Vector(getRefrenceBlockPos().getX(), getRefrenceBlockPos().getY(), getRefrenceBlockPos().getZ()));

        createPhysicsCalculations();
        BlockPos centerDifference = getRefrenceBlockPos().subtract(centerInWorld);

        toFollow.placeBlockAndTilesInWorld(getWorldObj(), centerDifference);

        detectBlockPositions();

        // TODO: This fixes the lighting, but it adds lag; maybe remove this
        for (int x = getOwnedChunks().getMinX(); x <= getOwnedChunks().getMaxX(); x++) {
            for (int z = getOwnedChunks().getMinZ(); z <= getOwnedChunks().getMaxZ(); z++) {
                // claimedChunks[x - ownedChunks.minX][z - ownedChunks.minZ].isTerrainPopulated
                // = true;
                // claimedChunks[x - ownedChunks.minX][z -
                // ownedChunks.minZ].generateSkylightMap();
                // claimedChunks[x - ownedChunks.minX][z - ownedChunks.minZ].checkLight();
            }
        }

        setShipTransformationManager(new ShipTransformationManager(this));
        getPhysicsProcessor().processInitialPhysicsData();
        getPhysicsProcessor().updateParentCenterOfMass();

        getShipTransformationManager().updateAllTransforms(false, false);
    }

    /**
     * Creates the PhysicsProcessor object before any data gets loaded into it; can
     * be overridden to change the class of the Object
     */
    private void createPhysicsCalculations() {
        if (getPhysicsProcessor() == null) {
            if (shipType == ShipType.Zepplin || shipType == ShipType.Dungeon_Sky) {
                setPhysicsProcessor(new PhysicsCalculationsManualControl(this));
            } else {
                setPhysicsProcessor(new PhysicsCalculations(this));
            }
        }
    }

    private void assembleShip(EntityPlayer player, SpatialDetector detector, BlockPos centerInWorld) {
    	this.setPhysicsEnabled(true);
        MutableBlockPos pos = new MutableBlockPos();
        TIntIterator iter = detector.foundSet.iterator();

        int radiusNeeded = 1;

        while (iter.hasNext()) {
            int i = iter.next();
            SpatialDetector.setPosWithRespectTo(i, BlockPos.ORIGIN, pos);

            int xRad = Math.abs(pos.getX() >> 4);
            int zRad = Math.abs(pos.getZ() >> 4);

            radiusNeeded = Math.max(Math.max(zRad, xRad), radiusNeeded + 1);
        }

        // radiusNeeded = math.max(radiusNeeded, 5);


        radiusNeeded = Math.min(radiusNeeded,
                ValkyrienWarfareMod.chunkManager.getManagerForWorld(getWrapperEntity().world).maxChunkRadius);

        // System.out.println(radiusNeeded);

        claimNewChunks(radiusNeeded);

        ValkyrienWarfareMod.physicsManager.onShipPreload(getWrapperEntity());

        claimedChunks = new Chunk[(getOwnedChunks().getRadius() * 2) + 1][(getOwnedChunks().getRadius() * 2) + 1];
        claimedChunksEntries = new PlayerChunkMapEntry[(getOwnedChunks().getRadius() * 2) + 1][(getOwnedChunks().getRadius() * 2) + 1];
        for (int x = getOwnedChunks().getMinX(); x <= getOwnedChunks().getMaxX(); x++) {
            for (int z = getOwnedChunks().getMinZ(); z <= getOwnedChunks().getMaxZ(); z++) {
                Chunk chunk = new Chunk(getWorldObj(), x, z);
                injectChunkIntoWorld(chunk, x, z, true);
                claimedChunks[x - getOwnedChunks().getMinX()][z - getOwnedChunks().getMinZ()] = chunk;
            }
        }

        // Prevents weird shit from spawning at the edges of a ship
        replaceOuterChunksWithAir();

        setShipChunks(new VWChunkCache(getWorldObj(), claimedChunks));
        int minChunkX = claimedChunks[0][0].x;
        int minChunkZ = claimedChunks[0][0].z;

        setRefrenceBlockPos(getRegionCenter());
        setCenterCoord(new Vector(getRefrenceBlockPos().getX(), getRefrenceBlockPos().getY(), getRefrenceBlockPos().getZ()));

        createPhysicsCalculations();

        iter = detector.foundSet.iterator();
        BlockPos centerDifference = getRefrenceBlockPos().subtract(centerInWorld);
        while (iter.hasNext()) {
            int i = iter.next();
            SpatialDetector.setPosWithRespectTo(i, centerInWorld, pos);

            IBlockState state = detector.cache.getBlockState(pos);

            TileEntity worldTile = detector.cache.getTileEntity(pos);

            pos.setPos(pos.getX() + centerDifference.getX(), pos.getY() + centerDifference.getY(),
                    pos.getZ() + centerDifference.getZ());
            getOwnedChunks().chunkOccupiedInLocal[(pos.getX() >> 4) - minChunkX][(pos.getZ() >> 4) - minChunkZ] = true;

            Chunk chunkToSet = claimedChunks[(pos.getX() >> 4) - minChunkX][(pos.getZ() >> 4) - minChunkZ];
            int storageIndex = pos.getY() >> 4;

            if (chunkToSet.storageArrays[storageIndex] == Chunk.NULL_BLOCK_STORAGE) {
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

                TileEntity newInstance = TileEntity.create(getWorldObj(), tileEntNBT);
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
                                int hash = SpatialDetector.getHashWithRespectTo(inTilePos.getX(), inTilePos.getY(),
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

                getWorldObj().setTileEntity(newInstance.getPos(), newInstance);

                if (newInstance instanceof INodeProvider) {
                    // System.out.println(newInstance.getClass().getName());
                    this.getConcurrentNodesWithinShip().add(((INodeProvider) newInstance).getNode());
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
            SpatialDetector.setPosWithRespectTo(i, centerInWorld, pos);
            // detector.cache.setBlockState(pos, Blocks.air.getDefaultState());
            // TODO: Get this to update on clientside as well, you bastard!
            TileEntity tile = getWorldObj().getTileEntity(pos);
            if (tile != null) {
                tile.invalidate();
            }
            getWorldObj().setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
        }
        // centerDifference = new
        // BlockPos(claimedChunks[ownedChunks.radius+1][ownedChunks.radius+1].x*16,128,claimedChunks[ownedChunks.radius+1][ownedChunks.radius+1].z*16);
        // System.out.println(chunkCache.getBlockState(centerDifference).getBlock());

        for (int x = getOwnedChunks().getMinX(); x <= getOwnedChunks().getMaxX(); x++) {
            for (int z = getOwnedChunks().getMinZ(); z <= getOwnedChunks().getMaxZ(); z++) {
                claimedChunks[x - getOwnedChunks().getMinX()][z - getOwnedChunks().getMinZ()].isTerrainPopulated = true;
                claimedChunks[x - getOwnedChunks().getMinX()][z - getOwnedChunks().getMinZ()].generateSkylightMap();
                claimedChunks[x - getOwnedChunks().getMinX()][z - getOwnedChunks().getMinZ()].checkLight();
            }
        }

        detectBlockPositions();

        // TODO: This fixes the lighting, but it adds lag; maybe remove this
        for (int x = getOwnedChunks().getMinX(); x <= getOwnedChunks().getMaxX(); x++) {
            for (int z = getOwnedChunks().getMinZ(); z <= getOwnedChunks().getMaxZ(); z++) {
                // claimedChunks[x - ownedChunks.minX][z - ownedChunks.minZ].isTerrainPopulated
                // = true;
                // claimedChunks[x - ownedChunks.minX][z -
                // ownedChunks.minZ].generateSkylightMap();
                // claimedChunks[x - ownedChunks.minX][z - ownedChunks.minZ].checkLight();
            }
        }

        setShipTransformationManager(new ShipTransformationManager(this));
        getPhysicsProcessor().processInitialPhysicsData();
        getPhysicsProcessor().updateParentCenterOfMass();

        for (Node node : this.getConcurrentNodesWithinShip()) {
            node.updateBuildState();
        }
    }

    public void injectChunkIntoWorld(Chunk chunk, int x, int z, boolean putInId2ChunkMap) {
        ChunkProviderServer provider = (ChunkProviderServer) getWorldObj().getChunkProvider();
        // TileEntities will break if you don't do this
        chunk.loaded = true;
        chunk.dirty = true;
        claimedChunks[x - getOwnedChunks().getMinX()][z - getOwnedChunks().getMinZ()] = chunk;

        if (putInId2ChunkMap) {
            provider.id2ChunkMap.put(ChunkPos.asLong(x, z), chunk);
        }

        PlayerChunkMap map = ((WorldServer) getWorldObj()).getPlayerChunkMap();

        PlayerChunkMapEntry entry = new PlayerChunkMapEntry(map, x, z) {
            // @Override
            // public boolean hasPlayerMatchingInRange(double range,
            // Predicate<EntityPlayerMP> predicate)
            // {
            // return true;
            // }
        };

        // TODO: This is causing concurrency crashes
        long i = PlayerChunkMap.getIndex(x, z);

        map.entryMap.put(i, entry);
        map.entries.add(entry);

        entry.sentToPlayers = true;
        entry.players = getWatchingPlayers();

        claimedChunksEntries[x - getOwnedChunks().getMinX()][z - getOwnedChunks().getMinZ()] = entry;

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
        for (int x = getOwnedChunks().getMinX() - 1; x <= getOwnedChunks().getMaxX() + 1; x++) {
            for (int z = getOwnedChunks().getMinZ() - 1; z <= getOwnedChunks().getMaxZ() + 1; z++) {
                if (x == getOwnedChunks().getMinX() - 1 || x == getOwnedChunks().getMaxX() + 1 || z == getOwnedChunks().getMinZ() - 1
                        || z == getOwnedChunks().getMaxZ() + 1) {
                    // This is satisfied for the chunks surrounding a Ship, do fill it with empty
                    // space
                    Chunk chunk = new Chunk(getWorldObj(), x, z);
                    ChunkProviderServer provider = (ChunkProviderServer) getWorldObj().getChunkProvider();
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
                    ((WorldServer) getWorldObj()).getEntityTracker().sendLeashedEntitiesInChunk(player, chunk);
                }
            }
        }
    }

    public BlockPos getRegionCenter() {
        return getOwnedChunks().getRegionCenter();
    }

    /**
     * TODO: Make this further get the player to stop all further tracking of those
     * physObject
     *
     * @param untracking EntityPlayer that stopped tracking
     */
    public void onPlayerUntracking(EntityPlayer untracking) {
        getWatchingPlayers().remove(untracking);
        for (int x = getOwnedChunks().getMinX(); x <= getOwnedChunks().getMaxX(); x++) {
            for (int z = getOwnedChunks().getMinZ(); z <= getOwnedChunks().getMaxZ(); z++) {
                SPacketUnloadChunk unloadPacket = new SPacketUnloadChunk(x, z);
                ((EntityPlayerMP) untracking).connection.sendPacket(unloadPacket);
            }
        }
    }

    /**
     * Called when this entity has been unloaded from the world
     */
    public void onThisUnload() {
        if (!getWorldObj().isRemote) {
            unloadShipChunksFromWorld();
        } else {
            getShipRenderer().killRenderers();
        }
    }

    public void unloadShipChunksFromWorld() {
        ChunkProviderServer provider = (ChunkProviderServer) getWorldObj().getChunkProvider();
        for (int x = getOwnedChunks().getMinX(); x <= getOwnedChunks().getMaxX(); x++) {
            for (int z = getOwnedChunks().getMinZ(); z <= getOwnedChunks().getMaxZ(); z++) {
                provider.queueUnload(claimedChunks[x - getOwnedChunks().getMinX()][z - getOwnedChunks().getMinZ()]);

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
        for (Object o : ((WorldServer) getWorldObj()).getEntityTracker().getTrackingPlayers(getWrapperEntity())) {
            EntityPlayerMP player = (EntityPlayerMP) o;
            if (!getWatchingPlayers().contains(player)) {
                newPlayers.add(player);
                getWatchingPlayers().add(player);
            }
        }
        return newPlayers;
    }

    public void onTick() {
        if (!getWorldObj().isRemote) {
            for (Entity e : queuedEntitiesToMount) {
                if (e != null) {
                    e.startRiding(this.getWrapperEntity(), true);
                }
            }
            queuedEntitiesToMount.clear();
        }
        gameConsecutiveTicks++;
    }

    public void onPostTick() {
        if (!getWrapperEntity().isDead && !getWrapperEntity().world.isRemote) {
            ValkyrienWarfareMod.chunkManager.updateShipPosition(getWrapperEntity());
            if (!claimedChunksInMap) {
                // Old ships not in the map will add themselves in once loaded
                ValkyrienWarfareMod.chunkManager.registerChunksForShip(getWrapperEntity());
                System.out.println("Old ship detected, adding to the registered Chunks map");
                claimedChunksInMap = true;
            }
        }
    }

	/**
	 * Updates the position and orientation of the client according to the data sent
	 * from the server.
	 */
    public void onPostTickClient() {
        ShipTransformationPacketHolder toUse = getShipTransformationManager().serverBuffer.pollForClientTransform();
        if (toUse != null) {
        	toUse.applySmoothLerp(this, .6D);
        }

        getShipTransformationManager().updatePrevTickTransform();
        getShipTransformationManager().updateAllTransforms(false, true);
    }

    public void updateChunkCache() {
    	AxisAlignedBB cacheBB = this.getShipBoundingBox();
    	// Check if all those surrounding chunks are loaded
        BlockPos min = new BlockPos(cacheBB.minX, Math.max(cacheBB.minY, 0), cacheBB.minZ);
        BlockPos max = new BlockPos(cacheBB.maxX, Math.min(cacheBB.maxY, 255), cacheBB.maxZ);
        if (!getWorldObj().isRemote) {
        	ChunkProviderServer serverChunkProvider = (ChunkProviderServer) getWorldObj().getChunkProvider();
        	int chunkMinX = min.getX() >> 4;
            int chunkMaxX = max.getX() >> 4;
            int chunkMinZ = min.getZ() >> 4;
            int chunkMaxZ = max.getZ() >> 4;
            boolean areSurroundingChunksLoaded = true;
            for (int chunkX = chunkMinX; chunkX <= chunkMaxX; chunkX++) {
            	for (int chunkZ = chunkMinZ; chunkZ <= chunkMaxZ; chunkZ++) {
            		boolean isChunkLoaded = serverChunkProvider.chunkExists(chunkX, chunkZ);
            		areSurroundingChunksLoaded &= isChunkLoaded;
            	}
            }
            if (areSurroundingChunksLoaded) {
            	setCachedSurroundingChunks(new ChunkCache(getWorldObj(), min, max, 0));
            } else {
            	this.resetConsecutiveProperTicks();
            }
        } else {
            setCachedSurroundingChunks(new ChunkCache(getWorldObj(), min, max, 0));
        }
    }

    public void loadClaimedChunks() {
        List<TileEntity> nodeTileEntitiesToUpdate = new ArrayList<TileEntity>();

        ValkyrienWarfareMod.physicsManager.onShipPreload(getWrapperEntity());

        claimedChunks = new Chunk[(getOwnedChunks().getRadius() * 2) + 1][(getOwnedChunks().getRadius() * 2) + 1];
        claimedChunksEntries = new PlayerChunkMapEntry[(getOwnedChunks().getRadius() * 2) + 1][(getOwnedChunks().getRadius() * 2) + 1];
        for (int x = getOwnedChunks().getMinX(); x <= getOwnedChunks().getMaxX(); x++) {
            for (int z = getOwnedChunks().getMinZ(); z <= getOwnedChunks().getMaxZ(); z++) {
                Chunk chunk = getWorldObj().getChunkFromChunkCoords(x, z);
                if (chunk == null) {
                    System.out.println("Just a loaded a null chunk");
                    chunk = new Chunk(getWorldObj(), x, z);
                }
                // Do this to get it re-integrated into the world
                if (!getWorldObj().isRemote) {
                    injectChunkIntoWorld(chunk, x, z, false);
                }
                for (Entry<BlockPos, TileEntity> entry : chunk.tileEntities.entrySet()) {
                    TileEntity tile = entry.getValue();
                    if (tile instanceof INodeProvider) {
                        nodeTileEntitiesToUpdate.add(tile);
                    }
                }
                claimedChunks[x - getOwnedChunks().getMinX()][z - getOwnedChunks().getMinZ()] = chunk;
            }
        }
        setShipChunks(new VWChunkCache(getWorldObj(), claimedChunks));
        setRefrenceBlockPos(getRegionCenter());
        setShipTransformationManager(new ShipTransformationManager(this));
        if (!getWorldObj().isRemote) {
            createPhysicsCalculations();
            // The client doesn't need to keep track of this.
            detectBlockPositions();
        }
        for (TileEntity tile : nodeTileEntitiesToUpdate) {
            Node node = ((INodeProvider) tile).getNode();
            if (node != null) {
                node.updateParentEntity(this);
            } else {
                System.err.println("How did we get a null node?");
            }
        }
        for (TileEntity tile : nodeTileEntitiesToUpdate) {
            Node node = ((INodeProvider) tile).getNode();
            if (node != null) {
                node.updateBuildState();
            } else {
                System.err.println("How did we get a null node?");
            }
        }

        getShipTransformationManager().updateAllTransforms(false, false);
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
                if (chunk != null && getOwnedChunks().chunkOccupiedInLocal[chunkX][chunkZ]) {
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
											getBlockPositions().add(pos);
											if (BlockForce.basicForces.isBlockProvidingForce(
													getWorldObj().getBlockState(pos), pos, getWorldObj())) {
												getPhysicsProcessor().addPotentialActiveForcePos(pos);
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
        return getOwnedChunks().isChunkEnclosedInSet(chunkX, chunkZ);
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
        EntityFixMessage entityFixingMessage = new EntityFixMessage(getWrapperEntity(), toFix, true, posInLocal);
        for (EntityPlayerMP watcher : getWatchingPlayers()) {
            ValkyrienWarfareControl.controlNetwork.sendTo(entityFixingMessage, watcher);
        }
        entityLocalPositions.put(toFix.getPersistentID().hashCode(), posInLocal);
    }

    /**
     * ONLY USE THESE 2 METHODS TO EVER ADD/REMOVE ENTITIES
     */
    public void unFixEntity(Entity toUnfix) {
        EntityFixMessage entityUnfixingMessage = new EntityFixMessage(getWrapperEntity(), toUnfix, false, null);
        for (EntityPlayerMP watcher : getWatchingPlayers()) {
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
        getOwnedChunks().writeToNBT(compound);
        NBTUtils.writeVectorToNBT("c", getCenterCoord(), compound);
		NBTUtils.writeShipTransformToNBT("currentTickTransform",
				getShipTransformationManager().getCurrentTickTransform(), compound);
		compound.setBoolean("doPhysics", isPhysicsEnabled/* isPhysicsEnabled() */);
		for (int row = 0; row < getOwnedChunks().chunkOccupiedInLocal.length; row++) {
			boolean[] curArray = getOwnedChunks().chunkOccupiedInLocal[row];
			for (int column = 0; column < curArray.length; column++) {
                compound.setBoolean("CC:" + row + ":" + column, curArray[column]);
            }
        }
        NBTUtils.writeEntityPositionMapToNBT("entityPosHashMap", entityLocalPositions, compound);
        getPhysicsProcessor().writeToNBTTag(compound);

        // TODO: This is occasionally crashing the Ship save
        // StringBuilder result = new StringBuilder("");
        // allowedUsers.forEach(s -> {
        //     result.append(s);
        //     result.append(";");
        // });
        // compound.setString("allowedUsers", result.substring(0, result.length() - 1));

        compound.setString("owner", getCreator());
        compound.setBoolean("claimedChunksInMap", claimedChunksInMap);
        compound.setBoolean("isNameCustom", isNameCustom());
        compound.setString("shipType", shipType.name());
        // Write and read AABB from NBT to speed things up.
        NBTUtils.writeAABBToNBT("collision_aabb", getShipBoundingBox(), compound);
    }

    public void readFromNBTTag(NBTTagCompound compound) {
        setOwnedChunks(new VWChunkClaim(compound));
        setCenterCoord(NBTUtils.readVectorFromNBT("c", compound));
        ShipTransform savedTransform = NBTUtils.readShipTransformFromNBT("currentTickTransform", compound);
        if (savedTransform != null) {
        	Vector centerOfMassInGlobal = new Vector(getCenterCoord());
        	savedTransform.transform(centerOfMassInGlobal, TransformType.SUBSPACE_TO_GLOBAL);
        	
        	getWrapperEntity().posX = centerOfMassInGlobal.X;
        	getWrapperEntity().posY = centerOfMassInGlobal.Y;
        	getWrapperEntity().posZ = centerOfMassInGlobal.Z;
        	
        	Quaternion rotationQuaternion = savedTransform.createRotationQuaternion(TransformType.SUBSPACE_TO_GLOBAL);
        	double[] angles = rotationQuaternion.toRadians();
        	getWrapperEntity().setPhysicsEntityRotation(Math.toDegrees(angles[0]), Math.toDegrees(angles[1]), Math.toDegrees(angles[2]));
        } else {
        	// Old code here for compatibility reasons. Should be removed by MC 1.13
            getWrapperEntity().setPhysicsEntityRotation(compound.getDouble("pitch"), compound.getDouble("yaw"), compound.getDouble("roll"));
        }
        
        for (int row = 0; row < getOwnedChunks().chunkOccupiedInLocal.length; row++) {
            boolean[] curArray = getOwnedChunks().chunkOccupiedInLocal[row];
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
        getPhysicsProcessor().readFromNBTTag(compound);

        getAllowedUsers().clear();
        Collections.addAll(getAllowedUsers(), compound.getString("allowedUsers").split(";"));

        setCreator(compound.getString("owner"));
        claimedChunksInMap = compound.getBoolean("claimedChunksInMap");
        setNameCustom(compound.getBoolean("isNameCustom"));
        getWrapperEntity().dataManager.set(PhysicsWrapperEntity.IS_NAME_CUSTOM, isNameCustom());
        
        this.setShipBoundingBox(NBTUtils.readAABBFromNBT("collision_aabb", compound));
        
        setPhysicsEnabled(compound.getBoolean("doPhysics"));
    }

    public void readSpawnData(ByteBuf additionalData) {
        PacketBuffer modifiedBuffer = new PacketBuffer(additionalData);

        setOwnedChunks(new VWChunkClaim(modifiedBuffer.readInt(), modifiedBuffer.readInt(), modifiedBuffer.readInt()));

        double posX = modifiedBuffer.readDouble();
        double posY = modifiedBuffer.readDouble();
        double posZ = modifiedBuffer.readDouble();
        double pitch = modifiedBuffer.readDouble();
        double yaw = modifiedBuffer.readDouble();
        double roll = modifiedBuffer.readDouble();
        
        getWrapperEntity().setPhysicsEntityPositionAndRotation(posX, posY, posZ, pitch, yaw, roll);
        getWrapperEntity().physicsUpdateLastTickPositions();
        
        setCenterCoord(new Vector(modifiedBuffer));
        for (boolean[] array : getOwnedChunks().chunkOccupiedInLocal) {
            for (int i = 0; i < array.length; i++) {
                array[i] = modifiedBuffer.readBoolean();
            }
        }
        loadClaimedChunks();
        getShipRenderer().updateOffsetPos(getRefrenceBlockPos());

        getShipTransformationManager().serverBuffer.pushMessage(new PhysWrapperPositionMessage(this));

        try {
            NBTTagCompound entityFixedPositionNBT = modifiedBuffer.readCompoundTag();
            entityLocalPositions = NBTUtils.readEntityPositionMap("entityFixedPosMap", entityFixedPositionNBT);
        } catch (IOException e) {
            System.err.println("Couldn't load the entityFixedPosNBT; this is really bad.");
            e.printStackTrace();
        }

        setNameCustom(modifiedBuffer.readBoolean());
        shipType = modifiedBuffer.readEnumValue(ShipType.class);
    }

    public void writeSpawnData(ByteBuf buffer) {
        PacketBuffer modifiedBuffer = new PacketBuffer(buffer);

        modifiedBuffer.writeInt(getOwnedChunks().getCenterX());
        modifiedBuffer.writeInt(getOwnedChunks().getCenterZ());
        modifiedBuffer.writeInt(getOwnedChunks().getRadius());

        modifiedBuffer.writeDouble(getWrapperEntity().posX);
        modifiedBuffer.writeDouble(getWrapperEntity().posY);
        modifiedBuffer.writeDouble(getWrapperEntity().posZ);

        modifiedBuffer.writeDouble(getWrapperEntity().getPitch());
        modifiedBuffer.writeDouble(getWrapperEntity().getYaw());
        modifiedBuffer.writeDouble(getWrapperEntity().getRoll());

        getCenterCoord().writeToByteBuf(modifiedBuffer);
        for (boolean[] array : getOwnedChunks().chunkOccupiedInLocal) {
            for (boolean b : array) {
                modifiedBuffer.writeBoolean(b);
            }
        }

        NBTTagCompound entityFixedPositionNBT = new NBTTagCompound();
        NBTUtils.writeEntityPositionMapToNBT("entityFixedPosMap", entityLocalPositions, entityFixedPositionNBT);
        modifiedBuffer.writeCompoundTag(entityFixedPositionNBT);

        modifiedBuffer.writeBoolean(isNameCustom());
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

        if (newOwner.entityUniqueID.toString().equals(getCreator())) {
            return EnumChangeOwnerResult.ALREADY_CLAIMED;
        }

        EntityPlayer player = null;
        try {
            player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList()
                    .getPlayerByUUID(UUID.fromString(getCreator()));
        } catch (NullPointerException e) {
            newOwner.sendMessage(new TextComponentString("That airship doesn't have an owner, you get to have it :D"));
            newOwner.getCapability(ValkyrienWarfareMod.airshipCounter, null).onCreate();
            getAllowedUsers().clear();
            setCreator(newOwner.entityUniqueID.toString());
            return EnumChangeOwnerResult.SUCCESS;
        }

        if (player != null) {
            player.getCapability(ValkyrienWarfareMod.airshipCounter, null).onLose();
        } else {
            try {
                File f = new File(DimensionManager.getCurrentSaveRootDirectory(), "playerdata/" + getCreator() + ".dat");
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

        getAllowedUsers().clear();

        setCreator(newOwner.entityUniqueID.toString());
        return EnumChangeOwnerResult.SUCCESS;
    }

    /*
     * Encapsulation code past here.
     */
    public ShipType getShipType() {
        return shipType;
    }

    public void setShipType(ShipType shipType) {
        this.shipType = shipType;
    }

    public AxisAlignedBB getShipBoundingBox() {
        return shipBoundingBox;
    }

    public void setShipBoundingBox(AxisAlignedBB newShipBB) {
        this.shipBoundingBox = newShipBB;
    }

    /**
     * @return The World this PhysicsObject exists in.
     */
    public World getWorldObj() {
        return getWrapperEntity().getEntityWorld();
    }

	/**
	 * @return The Entity that wraps around this PhysicsObject.
	 */
	public PhysicsWrapperEntity getWrapperEntity() {
		return wrapper;
	}
	
	public boolean areShipChunksFullyLoaded() {
		return getShipChunks() != null;
	}

	/**
	 * @return true if physics are enabled
	 */
	// TODO: This still breaks when the server is lagging, because it will skip
	// ticks and therefore the counter will go higher than it really should be.
	public boolean isPhysicsEnabled() {
		return isPhysicsEnabled && gameConsecutiveTicks >= MIN_TICKS_EXISTED_BEFORE_PHYSICS && physicsConsecutiveTicks >= MIN_TICKS_EXISTED_BEFORE_PHYSICS * 5;
	}

	/**
	 * @param physicsEnabled
	 *            If true enables physics processing for this physics object, if
	 *            false disables physics processing.
	 */
	public void setPhysicsEnabled(boolean physicsEnabled) {
		this.isPhysicsEnabled = physicsEnabled;
	}

	/**
	 * Sets the consecutive tick counter to 0.
	 */
	public void resetConsecutiveProperTicks() {
		this.gameConsecutiveTicks = 0;
		this.physicsConsecutiveTicks = 0;
	}
	
	public void advanceConsecutivePhysicsTicksCounter() {
		this.physicsConsecutiveTicks++;
	}
	
	/**
	 * @return true if this PhysicsObject needs to update the collision cache immediately.
	 */
	public boolean needsImmediateCollisionCacheUpdate() {
		return gameConsecutiveTicks == MIN_TICKS_EXISTED_BEFORE_PHYSICS;
	}
	
	/**
	 * @return this ships ShipTransformationManager
	 */
	public ShipTransformationManager getShipTransformationManager() {
		return shipTransformationManager;
	}

	/**
	 * @param shipTransformationManager the coordTransform to set
	 */
	private void setShipTransformationManager(ShipTransformationManager shipTransformationManager) {
		this.shipTransformationManager = shipTransformationManager;
	}

	/**
	 * @return the watchingPlayers
	 */
	public List<EntityPlayerMP> getWatchingPlayers() {
		return watchingPlayers;
	}

	/**
	 * @return the ship renderer
	 */
	public PhysObjectRenderManager getShipRenderer() {
		return shipRenderer;
	}

	/**
	 * @return the physicsProcessor
	 */
	public PhysicsCalculations getPhysicsProcessor() {
		return physicsProcessor;
	}

	/**
	 * @param physicsProcessor the physicsProcessor to set
	 */
	public void setPhysicsProcessor(PhysicsCalculations physicsProcessor) {
		this.physicsProcessor = physicsProcessor;
	}

	/**
	 * @return the centerCoord
	 */
	public Vector getCenterCoord() {
		return centerCoord;
	}

	/**
	 * @param centerCoord the centerCoord to set
	 */
	public void setCenterCoord(Vector centerCoord) {
		this.centerCoord = centerCoord;
	}

	/**
	 * @return the allowedUsers
	 */
	public Set<String> getAllowedUsers() {
		return allowedUsers;
	}

	/**
	 * @return the creator
	 */
	public String getCreator() {
		return creator;
	}

	/**
	 * @param creator the creator to set
	 */
	public void setCreator(String creator) {
		this.creator = creator;
	}

	/**
	 * @return the blockPositions
	 */
	public Set<BlockPos> getBlockPositions() {
		return blockPositions;
	}

	/**
	 * @param blockPositions the blockPositions to set
	 */
	public void setBlockPositions(Set<BlockPos> blockPositions) {
		this.blockPositions = blockPositions;
	}

	/**
	 * @return the concurrentNodesWithinShip
	 */
	public Set<Node> getConcurrentNodesWithinShip() {
		return concurrentNodesWithinShip;
	}

	/**
	 * @return the shipChunks
	 */
	public VWChunkCache getShipChunks() {
		return shipChunks;
	}

	/**
	 * @param shipChunks the shipChunks to set
	 */
	public void setShipChunks(VWChunkCache shipChunks) {
		this.shipChunks = shipChunks;
	}

	/**
	 * @return the detectorID
	 */
	public int getDetectorID() {
		return detectorID;
	}

	/**
	 * @param detectorID the detectorID to set
	 */
	public void setDetectorID(int detectorID) {
		this.detectorID = detectorID;
	}

	/**
	 * @return the isNameCustom
	 */
	public boolean isNameCustom() {
		return isNameCustom;
	}

	/**
	 * @param isNameCustom the isNameCustom to set
	 */
	public void setNameCustom(boolean isNameCustom) {
		this.isNameCustom = isNameCustom;
	}

	/**
	 * @return the cachedSurroundingChunks
	 */
	public ChunkCache getCachedSurroundingChunks() {
		return cachedSurroundingChunks;
	}

	/**
	 * @param cachedSurroundingChunks the cachedSurroundingChunks to set
	 */
	public void setCachedSurroundingChunks(ChunkCache cachedSurroundingChunks) {
		this.cachedSurroundingChunks = cachedSurroundingChunks;
	}

	/**
	 * @return the ownedChunks
	 */
	public VWChunkClaim getOwnedChunks() {
		return ownedChunks;
	}

	/**
	 * @param ownedChunks the ownedChunks to set
	 */
	public void setOwnedChunks(VWChunkClaim ownedChunks) {
		this.ownedChunks = ownedChunks;
	}

	/**
	 * @return the refrenceBlockPos
	 */
	public BlockPos getRefrenceBlockPos() {
		return refrenceBlockPos;
	}

	/**
	 * @param refrenceBlockPos
	 *            the refrenceBlockPos to set
	 */
	public void setRefrenceBlockPos(BlockPos refrenceBlockPos) {
		this.refrenceBlockPos = refrenceBlockPos;
	}

	@Override
	public ISubspace getSubspace() {
		return this.shipSubspace;
	}

}
