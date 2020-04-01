package org.valkyrienskies.mod.common.physics.management.chunkcache;

import lombok.extern.log4j.Log4j2;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.gen.ChunkProviderServer;
import org.valkyrienskies.mod.common.capability.VSCapabilityRegistry;
import org.valkyrienskies.mod.common.capability.VSChunkPhysoCapability;
import org.valkyrienskies.mod.common.ship_handling.PhysicsObject;
import org.valkyrienskies.mod.common.physmanagement.chunk.VSChunkClaim;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * The ClaimedChunkCacheController is a chunk cache controller used by the {@link PhysicsObject}. It
 * keeps all of a ship's chunks in cache for fast access.
 */
@Log4j2
public class ClaimedChunkCacheController implements Iterable<Chunk> {

    /**
     * You should ideally not be accessing this directly
     */
    private final PhysicsObject parent;
    private final World world;

    private final Map<Long, Chunk> claimedChunks;

    /**
     * This constructor is expensive; it loads all the chunks when it's called. Be warned.
     *
     * @param parent The PhysicsObject that is using this ChunkCacheController
     * @param loaded Whether or not the chunks that are being cached have been loaded before. e.g.,
     *               whether they are being loaded from NBT or from the world.
     */
    public ClaimedChunkCacheController(PhysicsObject parent, boolean loaded) {
        this.world = parent.getWorld();
        this.parent = parent;
        this.claimedChunks = new HashMap<>();

        if (loaded) {
            loadLoadedChunks();
        } else {
            loadNewChunks();
        }
    }

    /**
     * Retrieves a chunk from cache from its absolute position.
     *
     * @param chunkX The X position of the chunk
     * @param chunkZ The Z position of the chunk
     * @return The chunk from the cache
     */
    public Chunk getChunkAt(int chunkX, int chunkZ) {
        VSChunkClaim claim = parent.getShipData().getChunkClaim();

        throwIfOutOfBounds(claim, chunkX, chunkZ);

        long chunkPos = ChunkPos.asLong(chunkX, chunkZ);

        return claimedChunks.get(chunkPos);
    }

    /**
     * Retrieves a chunk from cache from its absolute position.
     *
     * @param chunkX The X position of the chunk
     * @param chunkZ The Z position of the chunk
     * @param chunk  The chunk to cache.
     */
    public void setChunkAt(int chunkX, int chunkZ, Chunk chunk) {
        VSChunkClaim claim = parent.getShipData().getChunkClaim();

        throwIfOutOfBounds(claim, chunkX, chunkZ);

        long chunkPos = ChunkPos.asLong(chunkX, chunkZ);

        claimedChunks.put(chunkPos, chunk);
    }

    private static void throwIfOutOfBounds(VSChunkClaim claim, int chunkX, int chunkZ) {
        if (!claim.containsChunk(chunkX, chunkZ)) {
            throw new ChunkNotInClaimException(chunkX, chunkZ);
        }
    }

    /**
     * Loads chunks that have been generated before into the cache
     */
    private void loadLoadedChunks() {
        System.out.println("Loading chunks");
        VSChunkClaim claim = parent.getShipData().getChunkClaim();

        claim.forEach((x, z) -> {
            // Added try catch to prevent ships deleting themselves because of a failed tile entity load.
            try {
                Chunk chunk = world.getChunk(x, z);
                if (chunk instanceof EmptyChunk) {
                    System.out.println("Why did we put an empty chunk at (" + x + "," + z + ")?");
                }

                // Do this to get it re-integrated into the world
                if (world.isRemote) {
                    attachAsParent(chunk);
                } else {
                    injectChunkIntoWorldServer(chunk, x, z, false, true);
                }

                chunk.tileEntities.forEach(parent::onSetTileEntity);

                setChunkAt(x, z, chunk);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Loads chunks that haven't been generated before into the cache. At the moment make sure to
     * only call this from the game thread. Running it on a separate thread will lead to data
     * races.
     */
    private void loadNewChunks() {
        System.out.println("Loading new chunks");
        VSChunkClaim claim = parent.getShipData().getChunkClaim();

        claim.forEach((x, z) -> {
            Chunk chunk = new Chunk(world, x, z);
            injectChunkIntoWorldServer(chunk, x, z, true, true);
            setChunkAt(x, z, chunk);
        });
    }

    private void injectChunkIntoWorldServer(Chunk chunk, int x, int z, boolean putInId2ChunkMap, boolean createPlayerEntry) {
        // Sanity check first
        if (!((WorldServer) world).isCallingFromMinecraftThread()) {
            throw new IllegalThreadStateException("We cannot call this crap from another thread!");
        }

        chunk.generateSkylightMap();
        chunk.checkLight();

        // Make sure this chunk knows we own it
        attachAsParent(chunk);

        ChunkProviderServer provider = (ChunkProviderServer) world.getChunkProvider();
        chunk.dirty = true;
        setChunkAt(x, z, chunk);

        if (putInId2ChunkMap) {
            provider.loadedChunks.put(ChunkPos.asLong(x, z), chunk);
        }

        chunk.onLoad();
        // We need to set these otherwise certain events like Sponge's PhaseTracker will refuse to work properly with ships!
        chunk.setTerrainPopulated(true);
        chunk.setLightPopulated(true);

        // Inject the entry into the player chunk map.
        if (createPlayerEntry) {
            PlayerChunkMap map = ((WorldServer) world).getPlayerChunkMap();
            PlayerChunkMapEntry entry = map.getOrCreateEntry(x, z);
            // Very important! We must update the chunk field of the entry to prevent old chunk objects from living on.
            // If this entry already existed and we forget, then we will corrupt the entry by having different chunks
            // in the world vs in the entries!
            entry.chunk = chunk;
            entry.sentToPlayers = true;
            entry.players = parent.getWatchingPlayers();
        }
    }

    public void deleteShipChunksFromWorld() {
        parent.getChunkClaim().forEach((x, z) -> {
            Chunk chunk = new Chunk(world, x, z);
            chunk.setTerrainPopulated(true);
            chunk.setLightPopulated(true);
            injectChunkIntoWorldServer(chunk, x, z, true, false);
            PlayerChunkMap map = ((WorldServer) world).getPlayerChunkMap();
            PlayerChunkMapEntry entry = map.getEntry(x, z);
            if (entry == null) {
                throw new IllegalStateException("How did the entry at " + x + " : " + z + " return as null?");
            }
            // Want to throw away old chunk.
            entry.chunk = chunk;
            map.removeEntry(entry);
        });
    }

    /**
     * Attaches the parent physo to the selected chunk's {@link VSCapabilityRegistry#VS_CHUNK_PHYSO}
     * capability
     */
    private void attachAsParent(Chunk chunk) {
        VSChunkPhysoCapability physoCapability = Objects.requireNonNull(
            chunk.getCapability(VSCapabilityRegistry.VS_CHUNK_PHYSO, null));
        physoCapability.set(parent);
    }

    @Nonnull
    @Override
    public Iterator<Chunk> iterator() {
        return claimedChunks.values().iterator();
    }
}
