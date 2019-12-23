package org.valkyrienskies.mod.common.physmanagement.chunk;

import java.util.Objects;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.physmanagement.shipdata.IValkyrienSkiesWorldData;

/**
 * This class is responsible for finding/allocating the Chunks for PhysicsObjects; also ensures the
 * custom chunk-loading system in place
 *
 * @author thebest108
 */
public class PhysicsChunkManager {

    // The +50 is used to make sure chunks too close to ships dont interfere
    public static boolean isLikelyShipChunk(int chunkX, int chunkZ) {
        boolean likelyLegacy = chunkZ < -1870000 + 12 + 50;
        return likelyLegacy || ShipChunkAllocator.isLikelyShipChunk(chunkX, chunkZ);
    }

    PhysicsChunkManager(World worldFor) {
        worldObj = worldFor;
    }

    public final World worldObj;

    /**
     * This finds the next empty chunkSet for use, currently only increases the xPos to get new
     * positions
     */
    public VSChunkClaim getNextAvailableChunkSet(int radius) {
        IValkyrienSkiesWorldData worldDataCapability =
            worldObj.getCapability(ValkyrienSkiesMod.VS_WORLD_DATA, null);

        // TODO: Add the ship id to the allocation eventually.
        ShipChunkAllocator.ChunkAllocation allocatedChunks = Objects
            .requireNonNull(worldDataCapability)
            .getChunkAllocator()
            .allocateChunks("insert ship id here", radius);

        return new VSChunkClaim(
            allocatedChunks.lowerChunkX + ShipChunkAllocator.MAX_SHIP_CHUNK_RADIUS,
            allocatedChunks.lowerChunkZ + ShipChunkAllocator.MAX_SHIP_CHUNK_RADIUS, radius);
    }

}
