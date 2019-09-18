package org.valkyrienskies.mod.common.ship_handling;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;

public class ImplShipChunkClaims implements IShipChunkClaims {

    private final Set<ChunkPos> claimedPositions;
    private transient Map<Long, Chunk> loadedChunksMap;
    private transient IWorldShipManager worldShipManager;
    private transient ShipHolder shipHolder;

    protected ImplShipChunkClaims() {
        this.claimedPositions = new TreeSet<>();
    }

    public void initializeTransients(IWorldShipManager worldShipManager, ShipHolder shipHolder) {
        if (loadedChunksMap != null || worldShipManager != null || shipHolder != null) {
            throw new IllegalStateException();
        }
        loadedChunksMap = new HashMap<>();
        this.worldShipManager = worldShipManager;
        this.shipHolder = shipHolder;
    }

    @Override
    public Iterator<ChunkPos> getClaimedChunkPos() {
        return claimedPositions.iterator();
    }

    @Override
    public boolean isPosClaimed(ChunkPos pos) {
        return claimedPositions.contains(pos);
    }

    @Override
    public boolean removeClaim(ChunkPos pos) {
        return claimedPositions.remove(pos);
    }

    @Override
    public boolean loadAllChunkClaims() {
        for (ChunkPos chunkPos : claimedPositions) {
            Chunk chunk = getWorldObj().getChunk(chunkPos.x, chunkPos.z);
            if (chunk == null) {
                System.out.println("Just a loaded a null chunk");
                chunk = new Chunk(getWorldObj(), chunkPos.x, chunkPos.z);
            }
            injectChunkIntoWorld(chunkPos.x, chunkPos.z, chunk);
        }
        return false;
    }

    @Override
    public boolean areChunkClaimsFullyLoaded() {
        // TODO: This is a terrible way of checking, must be replaced eventually.
        return loadedChunksMap.keySet().size() == claimedPositions.size();
    }

    @Override
    public boolean claimPos(ChunkPos pos) {
        return claimedPositions.add(pos);
    }

    private void injectChunkIntoWorld(int x, int z, Chunk chunk) {
        ChunkProviderServer provider = (ChunkProviderServer) this.getWorldObj().getChunkProvider();
        chunk.dirty = true;
        // claimedChunks[x - getownedChunks().getMinX()][z - getownedChunks().getMinZ()] = chunk;
        loadedChunksMap.put(ChunkPos.asLong(x, z), chunk);

        provider.loadedChunks.put(ChunkPos.asLong(x, z), chunk);

        chunk.onLoad();

        PlayerChunkMap map = ((WorldServer) this.getWorldObj()).getPlayerChunkMap();

        PlayerChunkMapEntry entry = new PlayerChunkMapEntry(map, x, z);

        long i = PlayerChunkMap.getIndex(x, z);
        // TODO: In future we need to do better to account for concurrency.
        map.entryMap.put(i, entry);
        map.entries.add(entry);

        entry.sentToPlayers = true;
        // TODO: In future we need to do better to account for concurrency.
        entry.players = Collections.unmodifiableList(shipHolder.getWatchingPlayers());
    }

    private World getWorldObj() {
        return worldShipManager.getWorld();
    }
}
