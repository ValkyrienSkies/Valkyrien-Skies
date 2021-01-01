package org.valkyrienskies.mod.common.ships.chunk_claims;

import lombok.extern.log4j.Log4j2;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.valkyrienskies.mod.common.config.VSConfig;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;

import javax.annotation.Nonnull;
import java.util.*;

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

    // private final Map<Long, Chunk> claimedChunks;
    private final Chunk[][] claimedChunks;
    private final int chunkBottomX, chunkBottomZ;
    private final int radius;

    /**
     * This constructor is expensive; it loads all the chunks when it's called. Be warned.
     *
     * @param parent The PhysicsObject that is using this ChunkCacheController
     */
    public ClaimedChunkCacheController(PhysicsObject parent) {
        this.world = parent.getWorld();
        this.parent = parent;
        // TODO: Bad :(
        this.radius = 7;
        this.claimedChunks = new Chunk[radius * 2 + 1][radius * 2 + 1];
        this.chunkBottomX = parent.getChunkClaim().getCenterPos().x - radius;
        this.chunkBottomZ = parent.getChunkClaim().getCenterPos().z - radius;
        loadChunksIntoCache();
    }

    /**
     * Retrieves a chunk from cache from its absolute position.
     *
     * @param chunkX The X position of the chunk
     * @param chunkZ The Z position of the chunk
     * @return The chunk from the cache
     */
    public Chunk getChunkAt(int chunkX, int chunkZ) {
        throwIfOutOfBounds(chunkX, chunkZ);

        return claimedChunks[chunkX - chunkBottomX][chunkZ - chunkBottomZ];
    }

    /**
     * Retrieves a chunk from cache from its absolute position.
     *
     * @param chunkX The X position of the chunk
     * @param chunkZ The Z position of the chunk
     * @param chunk  The chunk to cache.
     */
    private void setChunkAt(int chunkX, int chunkZ, Chunk chunk) {
        throwIfOutOfBounds(chunkX, chunkZ);

        final int relativeChunkX = chunkX - chunkBottomX;
        final int relativeChunkZ = chunkZ - chunkBottomZ;

        claimedChunks[relativeChunkX][relativeChunkZ] = chunk;
    }

    /**
     * Throws a ChunkNotInClaimException if (chunkX, chunkZ) isn't a part of the given VSChunkClaim.
     */
    private void throwIfOutOfBounds(int chunkX, int chunkZ) {
        // if (!claim.containsChunk(chunkX, chunkZ)) {
        //     throw new ChunkNotInClaimException(chunkX, chunkZ);
        // }
        final int relativeChunkX = chunkX - chunkBottomX;
        final int relativeChunkZ = chunkZ - chunkBottomZ;

        if (relativeChunkX < 0 || relativeChunkX >= radius * 2 + 1 || relativeChunkZ < 0 || relativeChunkZ >= radius * 2 + 1) {
            throw new ChunkNotInClaimException(chunkX, chunkZ);
        }
    }

    /**
     * Loads chunks from the world, and puts them into the cache.
     */
    private void loadChunksIntoCache() {
        if (VSConfig.showAnnoyingDebugOutput) {
            System.out.println("Loading chunks for " + parent.getShipData());
        }
        VSChunkClaim claim = parent.getShipData().getChunkClaim();

        claim.forEach((x, z) -> {
            // Added try catch to prevent ships deleting themselves because of a failed tile entity load.
            try {
                Chunk chunk = world.getChunk(x, z);
                if (chunk.isEmpty()) { // if (chunk instanceof EmptyChunk) { [Changed because EmptyChunk is a 'client' class]
                    if (VSConfig.showAnnoyingDebugOutput) {
                        System.out.println("Why did we put an empty chunk at (" + x + "," + z + ")?");
                    }
                }

                // Do this to get it re-integrated into the world
                if (!world.isRemote) {
                    // Inject the entry into the player chunk map.
                    PlayerChunkMap map = ((WorldServer) world).getPlayerChunkMap();
                    PlayerChunkMapEntry entry = map.getOrCreateEntry(x, z);
                    // Very important! We must update the chunk field of the entry to prevent old chunk objects from living on.
                    // If this entry already existed and we forget, then we will corrupt the entry by having different chunks
                    // in the world vs in the entries!
                    entry.chunk = chunk;
                    entry.sentToPlayers = true;
                    entry.players = parent.getWatchingPlayers();
                }

                chunk.tileEntities.forEach(parent::onSetTileEntity);

                setChunkAt(x, z, chunk);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void deleteShipChunksFromWorld() {
        PlayerChunkMap map = ((WorldServer) world).getPlayerChunkMap();

        final Iterator<Chunk> chunkIterator = iterator();
        while (chunkIterator.hasNext()) {
            final Chunk chunk = chunkIterator.next();
            // First delete all the TileEntities in the chunk
            List<BlockPos> chunkTilesPos = new ArrayList<>(chunk.tileEntities.keySet());
            for (BlockPos tilePos : chunkTilesPos) {
                chunk.world.removeTileEntity(tilePos);
            }
            // Then replace all the chunk's block storage with null.
            for (int i = 0; i < 16; i++) {
                chunk.storageArrays[i] = null;
            }
            chunk.markDirty();

            // Finally, remove the PlayerChunkMapEntry that was watching that chunk
            PlayerChunkMapEntry entry = map.getEntry(chunk.x, chunk.z);
            if (entry == null) {
                // This should be impossible, throw exception if it happens.
                throw new IllegalStateException("How did the entry at " + chunk.x + " : " + chunk.z + " return as null?");
            }
            map.removeEntry(entry);
        }
    }

    @Nonnull
    @Override
    public Iterator<Chunk> iterator() {
        final List<Chunk> chunksList = new ArrayList<>();
        for (Chunk[] chunks : claimedChunks) {
            for (Chunk chunk : chunks) {
                if (chunk != null) chunksList.add(chunk);
            }
        }
        return chunksList.iterator();
    }

    /**
     * Replace an old chunk object with a new one in this cache.
     */
    @SideOnly(Side.CLIENT)
    public void updateChunk(@Nonnull Chunk chunk) {
        setChunkAt(chunk.x, chunk.z, chunk);
    }

}
