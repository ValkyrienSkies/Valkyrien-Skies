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

    import com.google.common.collect.Sets;
    import gnu.trove.iterator.TIntIterator;
    import gnu.trove.list.array.TIntArrayList;
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
    import net.minecraft.util.math.Vec3d;
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
    import valkyrienwarfare.addon.control.nodenetwork.INodeController;
    import valkyrienwarfare.api.IPhysicsEntity;
    import valkyrienwarfare.api.TransformType;
    import valkyrienwarfare.deprecated_api.EnumChangeOwnerResult;
    import valkyrienwarfare.fixes.IPhysicsChunk;
    import valkyrienwarfare.math.Quaternion;
    import valkyrienwarfare.math.Vector;
    import valkyrienwarfare.mod.BlockPhysicsRegistration;
    import valkyrienwarfare.mod.client.render.PhysObjectRenderManager;
    import valkyrienwarfare.mod.coordinates.*;
    import valkyrienwarfare.mod.network.PhysWrapperPositionMessage;
    import valkyrienwarfare.mod.physmanagement.chunk.ShipChunkAllocator;
    import valkyrienwarfare.mod.physmanagement.chunk.VWChunkCache;
    import valkyrienwarfare.mod.physmanagement.chunk.VWChunkClaim;
    import valkyrienwarfare.mod.physmanagement.relocation.DetectorManager;
    import valkyrienwarfare.mod.physmanagement.relocation.MoveBlocks;
    import valkyrienwarfare.mod.physmanagement.relocation.SpatialDetector;
    import valkyrienwarfare.mod.tileentity.TileEntityPhysicsInfuser;
    import valkyrienwarfare.physics.BlockForce;
    import valkyrienwarfare.physics.PhysicsCalculations;
    import valkyrienwarfare.util.NBTUtils;

    import java.io.File;
    import java.io.IOException;
    import java.util.*;
    import java.util.Map.Entry;
    import java.util.concurrent.ConcurrentHashMap;

    /**
     * The heart and soul of this mod. The physics object does everything from
     * custom collision, block interactions, physics, networking, rendering, and
     * more!
     *
     * @author thebest108
     */
    public class PhysicsObject implements ISubspaceProvider, IPhysicsEntity {

        public static final int MIN_TICKS_EXISTED_BEFORE_PHYSICS = 5;

        private final PhysicsWrapperEntity wrapper;
        private final List<EntityPlayerMP> watchingPlayers;
        private final Set<String> allowedUsers;
        // This is used to delay mountEntity() operations by 1 tick
        private final List<Entity> queuedEntitiesToMount;
        private final ISubspace shipSubspace;
        private final Set<INodeController> physicsControllers;
        private final Set<INodeController> physicsControllersImmutable;
        // Used to iterate over the ship blocks extremely quickly by taking advantage of the cache
        private final TIntArrayList blockPositionsGameTick;
        private PhysObjectRenderManager shipRenderer;
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
        private BlockPos physicsInfuserPos;
        private boolean shipAligningToGrid;

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
            this.setBlockPositions(ConcurrentHashMap.newKeySet());
            this.shipBoundingBox = Entity.ZERO_AABB;
            this.watchingPlayers = new ArrayList<EntityPlayerMP>();
            this.isPhysicsEnabled = false;
            this.allowedUsers = new HashSet<String>();
            this.gameConsecutiveTicks = 0;
            this.physicsConsecutiveTicks = 0;
            this.shipSubspace = new ImplSubspace(this);
            this.physicsControllers = Sets.newConcurrentHashSet();
            this.physicsControllersImmutable = Collections.unmodifiableSet(this.physicsControllers);
            this.blockPositionsGameTick = new TIntArrayList();
            this.physicsInfuserPos = null;
            this.shipAligningToGrid = false;
        }

        public void onSetBlockState(IBlockState oldState, IBlockState newState, BlockPos posAt) {
            if (getWorldObj().isRemote) {
                return;
            }
            // System.out.println("OLD: " + oldState.getBlock());
            // System.out.println("NEW: " + newState.getBlock());
            // new Exception().printStackTrace();

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

            boolean isOldAir = oldState == null || oldState.getBlock()
                    .equals(Blocks.AIR);
            boolean isNewAir = newState == null || newState.getBlock()
                    .equals(Blocks.AIR);

            if (isNewAir) {
                boolean removed = getBlockPositions().remove(posAt);
                if (removed) {
                    this.blockPositionsGameTick.remove(this.getBlockPosToIntRelToShip(posAt));
                }
            }

            if ((isOldAir && !isNewAir)) {
                boolean isAdded = getBlockPositions().add(posAt);
                if (isAdded) {
                    this.blockPositionsGameTick.add(this.getBlockPosToIntRelToShip(posAt));
                }

                int chunkX = (posAt.getX() >> 4) - claimedChunks[0][0].x;
                int chunkZ = (posAt.getZ() >> 4) - claimedChunks[0][0].z;
                getOwnedChunks().chunkOccupiedInLocal[chunkX][chunkZ] = true;
            }

            if (getBlockPositions().isEmpty()) {
                try {
                    if (getCreator() != null) {
                        EntityPlayer player = FMLCommonHandler.instance()
                                .getMinecraftServerInstance()
                                .getPlayerList()
                                .getPlayerByUsername(getCreator());
                        if (player != null) {
                            player.getCapability(ValkyrienWarfareMod.airshipCounter, null)
                                    .onLose();
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
            ValkyrienWarfareMod.VW_CHUNK_MANAGER.removeRegistedChunksForShip(getWrapperEntity());
            ValkyrienWarfareMod.VW_CHUNK_MANAGER.removeShipPosition(getWrapperEntity());
            ValkyrienWarfareMod.VW_CHUNK_MANAGER.removeShipNameRegistry(getWrapperEntity());
            ValkyrienWarfareMod.VW_PHYSICS_MANAGER.onShipUnload(getWrapperEntity());
        }

        public void claimNewChunks(int radius) {
            setOwnedChunks(ValkyrienWarfareMod.VW_CHUNK_MANAGER.getManagerForWorld(getWrapperEntity().world)
                    .getNextAvaliableChunkSet(radius));
            ValkyrienWarfareMod.VW_CHUNK_MANAGER.registerChunksForShip(getWrapperEntity());
            claimedChunksInMap = true;
        }

        /**
         * Generates the new chunks
         */
        public void assembleShipAsOrderedByPlayer(EntityPlayer player) {
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

        /**
         * Creates the PhysicsProcessor object before any data gets loaded into it; can
         * be overridden to change the class of the Object
         */
        private void createPhysicsCalculations() {
            if (getPhysicsProcessor() == null) {
                setPhysicsProcessor(new PhysicsCalculations(this));
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

            radiusNeeded = Math.min(radiusNeeded, ShipChunkAllocator.MAX_SHIP_CHUNK_RADIUS);
            claimNewChunks(radiusNeeded);
            ValkyrienWarfareMod.VW_PHYSICS_MANAGER.onShipPreload(getWrapperEntity());

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

            setRefrenceBlockPos(getRegionCenter());

            setCenterCoord(new Vector(getReferenceBlockPos().getX() + .5, getReferenceBlockPos().getY() + .5, getReferenceBlockPos().getZ() + .5));

            createPhysicsCalculations();

            iter = detector.foundSet.iterator();
            BlockPos centerDifference = getReferenceBlockPos().subtract(centerInWorld);

            MutableBlockPos oldPos = new MutableBlockPos();
            MutableBlockPos newPos = new MutableBlockPos();

            // First copy all the blocks from ship to world.
            while (iter.hasNext()) {
                int i = iter.next();
                SpatialDetector.setPosWithRespectTo(i, centerInWorld, oldPos);
                SpatialDetector.setPosWithRespectTo(i, centerInWorld, newPos);
                newPos.setPos(newPos.getX() + centerDifference.getX(), newPos.getY() + centerDifference.getY(), newPos.getZ() + centerDifference.getZ());

                MoveBlocks.copyBlockToPos(getWorldObj(), oldPos, newPos, Optional.of(this));
            }
            // Remember to update the infuser pos
            if (this.getShipType() == ShipType.PHYSICS_CORE_INFUSED) {
                this.physicsInfuserPos = this.physicsInfuserPos.add(centerDifference);
            }
            // Then destroy all of the blocks we copied from in world.
            iter = detector.foundSet.iterator();
            while (iter.hasNext()) {
                int i = iter.next();
                SpatialDetector.setPosWithRespectTo(i, centerInWorld, pos);
                TileEntity tile = getWorldObj().getTileEntity(pos);
                if (tile != null && !tile.isInvalid()) {
                    try {
                        tile.invalidate();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        getWorldObj().removeTileEntity(pos);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                getWorldObj().setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
            }

            for (int x = getOwnedChunks().getMinX(); x <= getOwnedChunks().getMaxX(); x++) {
                for (int z = getOwnedChunks().getMinZ(); z <= getOwnedChunks().getMaxZ(); z++) {
                    claimedChunks[x - getOwnedChunks().getMinX()][z - getOwnedChunks().getMinZ()].generateSkylightMap();
                }
            }

            getWrapperEntity().posX += .5;
            getWrapperEntity().posY += .5;
            getWrapperEntity().posZ += .5;

            // Some extra ship crap at the end.
            detectBlockPositions();
            setShipTransformationManager(new ShipTransformationManager(this));

            getPhysicsProcessor().updateParentCenterOfMass();
        }

        public void injectChunkIntoWorld(Chunk chunk, int x, int z, boolean putInId2ChunkMap) {
            // Make sure this chunk knows we own it.
            ((IPhysicsChunk) chunk).setParentPhysicsObject(Optional.of(this));

            ChunkProviderServer provider = (ChunkProviderServer) getWorldObj().getChunkProvider();
            chunk.dirty = true;
            claimedChunks[x - getOwnedChunks().getMinX()][z - getOwnedChunks().getMinZ()] = chunk;

            if (putInId2ChunkMap) {
                provider.loadedChunks.put(ChunkPos.asLong(x, z), chunk);
            }

            chunk.onLoad();
            // We need to set these otherwise certain events like Sponge's PhaseTracker will refuse to work properly with ships!
            chunk.setTerrainPopulated(true);
            chunk.setLightPopulated(true);

            PlayerChunkMap map = ((WorldServer) getWorldObj()).getPlayerChunkMap();

            PlayerChunkMapEntry entry = new PlayerChunkMapEntry(map, x, z);

            // TODO: This is causing concurrency crashes
            long i = PlayerChunkMap.getIndex(x, z);

            map.entryMap.put(i, entry);
            map.entries.add(entry);

            entry.sentToPlayers = true;
            entry.players = getWatchingPlayers();

            claimedChunksEntries[x - getOwnedChunks().getMinX()][z - getOwnedChunks().getMinZ()] = entry;
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
                        provider.loadedChunks.put(ChunkPos.asLong(x, z), chunk);
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
                        ((WorldServer) getWorldObj()).getEntityTracker()
                                .sendLeashedEntitiesInChunk(player, chunk);
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
            for (Object o : ((WorldServer) getWorldObj()).getEntityTracker()
                    .getTrackingPlayers(getWrapperEntity())) {
                EntityPlayerMP player = (EntityPlayerMP) o;
                if (!getWatchingPlayers().contains(player)) {
                    newPlayers.add(player);
                    getWatchingPlayers().add(player);
                }
            }
            return newPlayers;
        }

        public void onTick() {
            // idk
            for (int x = this.ownedChunks.getMinX(); x <= this.ownedChunks.getMaxX(); x++) {
                for (int z = this.ownedChunks.getMinZ(); z <= this.ownedChunks.getMaxZ(); z++) {
                    ((IPhysicsChunk) shipChunks.getChunkAt(x, z)).setParentPhysicsObject(Optional.of(this));
                }
            }

            if (!getWorldObj().isRemote) {
                for (Entity e : queuedEntitiesToMount) {
                    if (e != null) {
                        e.startRiding(this.getWrapperEntity(), true);
                    }
                }
                queuedEntitiesToMount.clear();


                if (this.getShipType() == ShipType.PHYSICS_CORE_INFUSED) {
                    TileEntity te = getWorldObj().getTileEntity(this.physicsInfuserPos);
                    boolean shouldDeconstructShip;
                    if (te instanceof TileEntityPhysicsInfuser) {
                        TileEntityPhysicsInfuser physicsCore = (TileEntityPhysicsInfuser) te;
                        // Mark for deconstruction
                        shouldDeconstructShip = !physicsCore.canMaintainShip() || physicsCore.isTryingToDisassembleShip();
                        shipAligningToGrid = !physicsCore.canMaintainShip() || physicsCore.isTryingToAlignShip();
                        setPhysicsEnabled(physicsCore.isPhysicsEnabled());
                    } else {
                        // Mark for deconstruction
                        shipAligningToGrid = true;
                        shouldDeconstructShip = true;
                        setPhysicsEnabled(true);
                    }

                    if (shouldDeconstructShip) {
                        this.tryToDeconstructShip();
                    }
                }
            }

            gameConsecutiveTicks++;
        }

        public void onPostTick() {
            if (!getWrapperEntity().isDead && !getWrapperEntity().world.isRemote) {
                ValkyrienWarfareMod.VW_CHUNK_MANAGER.updateShipPosition(getWrapperEntity());
                if (!claimedChunksInMap) {
                    // Old ships not in the map will add themselves in once loaded
                    ValkyrienWarfareMod.VW_CHUNK_MANAGER.registerChunksForShip(getWrapperEntity());
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
            ValkyrienWarfareMod.VW_PHYSICS_MANAGER.onShipPreload(getWrapperEntity());

            claimedChunks = new Chunk[(getOwnedChunks().getRadius() * 2) + 1][(getOwnedChunks().getRadius() * 2) + 1];
            claimedChunksEntries = new PlayerChunkMapEntry[(getOwnedChunks().getRadius() * 2) + 1][(getOwnedChunks().getRadius() * 2) + 1];
            for (int x = getOwnedChunks().getMinX(); x <= getOwnedChunks().getMaxX(); x++) {
                for (int z = getOwnedChunks().getMinZ(); z <= getOwnedChunks().getMaxZ(); z++) {
                    // Added try catch to prevent ships deleting themselves because of a failed tile entity load.
                    try {
                        Chunk chunk = getWorldObj().getChunk(x, z);
                        if (chunk == null) {
                            System.out.println("Just a loaded a null chunk");
                            chunk = new Chunk(getWorldObj(), x, z);
                        }
                        // Do this to get it re-integrated into the world
                        if (!getWorldObj().isRemote) {
                            injectChunkIntoWorld(chunk, x, z, false);
                        }
                        for (Entry<BlockPos, TileEntity> entry : chunk.tileEntities.entrySet()) {
                            this.onSetTileEntity(entry.getKey(), entry.getValue());
                        }
                        claimedChunks[x - getOwnedChunks().getMinX()][z - getOwnedChunks().getMinZ()] = chunk;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
                                                blockPositionsGameTick.add(this.getBlockPosToIntRelToShip(pos));
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
            entityLocalPositions.put(toFix.getPersistentID()
                    .hashCode(), posInLocal);
        }

        /**
         * ONLY USE THESE 2 METHODS TO EVER ADD/REMOVE ENTITIES
         */
        public void unFixEntity(Entity toUnfix) {
            EntityFixMessage entityUnfixingMessage = new EntityFixMessage(getWrapperEntity(), toUnfix, false, null);
            for (EntityPlayerMP watcher : getWatchingPlayers()) {
                ValkyrienWarfareControl.controlNetwork.sendTo(entityUnfixingMessage, watcher);
            }
            entityLocalPositions.remove(toUnfix.getPersistentID()
                    .hashCode());
        }

        public void fixEntityUUID(int uuidHash, Vector localPos) {
            entityLocalPositions.put(uuidHash, localPos);
        }

        public void removeEntityUUID(int uuidHash) {
            entityLocalPositions.remove(uuidHash);
        }

        public boolean isEntityFixed(Entity toCheck) {
            return entityLocalPositions.containsKey(toCheck.getPersistentID()
                    .hashCode());
        }

        public Vector getLocalPositionForEntity(Entity getPositionFor) {
            int uuidHash = getPositionFor.getPersistentID()
                    .hashCode();
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
            NBTUtils.writeBlockPosToNBT("phys_infuser_pos", physicsInfuserPos, compound);

            try {
                compound.setString("ship_type", shipType.name());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void readFromNBTTag(NBTTagCompound compound) {
            // This first
            setCenterCoord(NBTUtils.readVectorFromNBT("c", compound));
            // Then this second
            createPhysicsCalculations();
            assert getPhysicsProcessor() != null : "Insert error message here";

            setOwnedChunks(new VWChunkClaim(compound));
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

            if (compound.hasKey("ship_type")) {
                shipType = ShipType.valueOf(ShipType.class, compound.getString("ship_type"));
            } else {
                // Assume its an older Ship, and that its fully unlocked
                shipType = ShipType.FULL_UNLOCKED;
            }

            loadClaimedChunks();

            // After we have loaded which positions are stored in the ship; we load the physics calculations object.
            getPhysicsProcessor().readFromNBTTag(compound);
            entityLocalPositions = NBTUtils.readEntityPositionMap("entityPosHashMap", compound);

            getAllowedUsers().clear();
            Collections.addAll(getAllowedUsers(), compound.getString("allowedUsers")
                    .split(";"));

            setCreator(compound.getString("owner"));
            claimedChunksInMap = compound.getBoolean("claimedChunksInMap");
            setNameCustom(compound.getBoolean("isNameCustom"));
            getWrapperEntity().dataManager.set(PhysicsWrapperEntity.IS_NAME_CUSTOM, isNameCustom());

            this.setShipBoundingBox(NBTUtils.readAABBFromNBT("collision_aabb", compound));

            setPhysicsEnabled(compound.getBoolean("doPhysics"));
            physicsInfuserPos = NBTUtils.readBlockPosFromNBT("phys_infuser_pos", compound);
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
            getShipRenderer().updateOffsetPos(getReferenceBlockPos());

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

            if (newOwner.entityUniqueID.toString()
                    .equals(getCreator())) {
                return EnumChangeOwnerResult.ALREADY_CLAIMED;
            }

            EntityPlayer player = null;
            try {
                player = FMLCommonHandler.instance()
                        .getMinecraftServerInstance()
                        .getPlayerList()
                        .getPlayerByUUID(UUID.fromString(getCreator()));
            } catch (NullPointerException e) {
                newOwner.sendMessage(new TextComponentString("That airship doesn't have an owner, you get to have it :D"));
                newOwner.getCapability(ValkyrienWarfareMod.airshipCounter, null)
                        .onCreate();
                getAllowedUsers().clear();
                setCreator(newOwner.entityUniqueID.toString());
                return EnumChangeOwnerResult.SUCCESS;
            }

            if (player != null) {
                player.getCapability(ValkyrienWarfareMod.airshipCounter, null)
                        .onLose();
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

            newOwner.getCapability(ValkyrienWarfareMod.airshipCounter, null)
                    .onCreate();

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
         * @param physicsEnabled If true enables physics processing for this physics object, if
         *                       false disables physics processing.
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
        private void setBlockPositions(Set<BlockPos> blockPositions) {
            this.blockPositions = blockPositions;
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
         * @param refrenceBlockPos the refrenceBlockPos to set
         */
        public void setRefrenceBlockPos(BlockPos refrenceBlockPos) {
            this.refrenceBlockPos = refrenceBlockPos;
        }

        @Override
        public ISubspace getSubspace() {
            return this.shipSubspace;
        }

        // ===== Keep track of all Node Processors in a concurrent Set =====
        public void onSetTileEntity(BlockPos pos, TileEntity tileentity) {
            if (tileentity instanceof INodeController) {
                physicsControllers.add((INodeController) tileentity);
            }
            // System.out.println(physicsControllers.size());
        }

        public void onRemoveTileEntity(BlockPos pos) {
            Iterator<INodeController> controllersIterator = physicsControllers.iterator();
            while (controllersIterator.hasNext()) {
                INodeController next = controllersIterator.next();
                if (next.getNodePos()
                        .equals(pos)) {
                    controllersIterator.remove();
                }
            }
            // System.out.println(physicsControllers.size());
        }

        // Do not allow anything external to modify the physics controllers Set.
        public Set<INodeController> getPhysicsControllersInShip() {
            return physicsControllersImmutable;
        }

        public int getBlockPosToIntRelToShip(BlockPos pos) {
            return SpatialDetector.getHashWithRespectTo(pos.getX(), pos.getY(), pos.getZ(), this.refrenceBlockPos);
        }

        public void setBlockPosFromIntRelToShop(int pos, MutableBlockPos toSet) {
            SpatialDetector.setPosWithRespectTo(pos, this.refrenceBlockPos, toSet);
        }

        public TIntArrayList getBlockPositionsGameTick() {
            return blockPositionsGameTick;
        }

        private BlockPos getReferenceBlockPos() {
            return this.refrenceBlockPos;
        }

        public void tryToDeconstructShip() {
            // First check if the ship orientation is close to that of the grid; if it isn't then don't let this ship deconstruct.
            Quaternion zeroQuat = Quaternion.fromEuler(0, 0, 0);
            Quaternion shipQuat = Quaternion.fromEuler(wrapper.getPitch(), wrapper.getYaw(), wrapper.getRoll());
            double dotProduct = Quaternion.dotProduct(zeroQuat, shipQuat);
            double anglesBetweenQuaternions = Math.toDegrees(Math.acos(dotProduct));

            if (anglesBetweenQuaternions < .5) {
                // We're pretty close to the grid; time 2 go.
                MutableBlockPos newPos = new MutableBlockPos();
                BlockPos centerDifference = new BlockPos(Math.round(centerCoord.X - getWrapperEntity().posX),
                        Math.round(centerCoord.Y - getWrapperEntity().posY),
                        Math.round(centerCoord.Z - getWrapperEntity().posZ));
                // First copy all the blocks from ship to world.

                for (BlockPos oldPos : this.blockPositions) {
                    newPos.setPos(oldPos.getX() - centerDifference.getX(), oldPos.getY() - centerDifference.getY(), oldPos.getZ() - centerDifference.getZ());
                    MoveBlocks.copyBlockToPos(getWorldObj(), oldPos, newPos, Optional.empty());
                }

                // Delete old blocks. TODO: Used to use EMPTYCHUNK to do this but that causes crashes?
                for (int x = getOwnedChunks().getMinX(); x <= getOwnedChunks().getMaxX(); x++) {
                    for (int z = getOwnedChunks().getMinZ(); z <= getOwnedChunks().getMaxZ(); z++) {
                        Chunk chunk = new Chunk(getWorldObj(), x, z);
                        chunk.setTerrainPopulated(true);
                        chunk.setLightPopulated(true);
                        injectChunkIntoWorld(chunk, x, z, true);
                        claimedChunks[x - getOwnedChunks().getMinX()][z - getOwnedChunks().getMinZ()] = chunk;
                    }
                }

                this.destroy();
            }
        }

        public void setPhysicsInfuserPos(BlockPos pos) {
            this.physicsInfuserPos = pos;
        }

        public boolean getShipAligningToGrid() {
            return this.shipAligningToGrid;
        }

        // VW API Functions Begin:
        @Override
        public Vec3d rotateVector(Vec3d vector, TransformType transformType) {
            return this.getShipTransformationManager()
                    .getCurrentTickTransform()
                    .rotate(vector, transformType);
        }

        @Override
        public Vec3d transformVector(Vec3d vector, TransformType transformType) {
            return this.getShipTransformationManager()
                    .getCurrentTickTransform()
                    .transform(vector, transformType);
        }
        // VW API Functions End:
    }
