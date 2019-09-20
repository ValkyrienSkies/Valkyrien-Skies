package org.valkyrienskies.mod.common.physics.management.chunkcache;

import net.minecraft.util.math.ChunkPos;

/**
 * This exception is thrown when a chunk is a requested from a cache but is not a part of the claim
 * from which the cache is operating on
 *
 * @see ClaimedChunkCacheController
 * @see SurroundingChunkCacheController
 */
class ChunkNotInClaimException extends IllegalArgumentException {

    public ChunkNotInClaimException(ChunkPos pos) {
        super("The chunk requested, " + pos.toString() + ", does not exist or was not cached!");
    }

    public ChunkNotInClaimException(int chunkX, int chunkZ) {
        this(new ChunkPos(chunkX, chunkZ));
    }

}
