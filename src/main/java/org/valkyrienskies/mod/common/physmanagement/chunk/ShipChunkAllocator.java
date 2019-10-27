package org.valkyrienskies.mod.common.physmanagement.chunk;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

/**
 * <p>
 * This class allocates chunks for usage in ships. Chunks in these ship chunks, (oftentimes referred
 * to as "the shipyard") are reserved for usage by Valkyrien Skies, and should not be edited by
 * other mods. Placing chunks in this region will either overwrite existing ships or may be
 * overwritten by future ships.
 * </p>
 * <p>
 * This "shipyard" is necessary because TileEntities expect that they are in the same world as other
 * entities that are interacting with them.
 * </p>
 *
 * <p>
 * The coordinate of the center of chunks allocated * for ship # N would be ({@link #CHUNK_X_START},
 * {@link #CHUNK_Z_START} + N*{@link #MAX_CHUNK_LENGTH}).
 * </p>
 */
@Log4j2
@NonNull
public class ShipChunkAllocator {

    /**
     * The size of the square of chunks (default 256 x 256) that are allocated to a ship.
     */
    @SuppressWarnings("WeakerAccess")
    public static final int MAX_CHUNK_LENGTH = 32;
    @SuppressWarnings("WeakerAccess")
    public static final int MAX_CHUNK_RADIUS = (MAX_CHUNK_LENGTH / 2) - 1;
    @SuppressWarnings("WeakerAccess")
    public static final int CHUNK_X_START = 320000;
    @SuppressWarnings("WeakerAccess")
    public static final int CHUNK_Z_START = 0;

    @Getter
    private int lastChunkX = CHUNK_X_START;
    @Getter
    private int lastChunkZ = CHUNK_Z_START;

    public static boolean isLikelyShipChunk(int chunkX, int chunkZ) {
        return chunkX >= CHUNK_X_START - MAX_CHUNK_RADIUS && chunkZ >= CHUNK_Z_START - MAX_CHUNK_RADIUS;
    }

    /**
     * This finds the next empty chunkSet for use, currently only increases the xPos to get new
     * positions
     */
    public VSChunkClaim allocateNextChunkClaim() {
        return new VSChunkClaim(lastChunkX ,
            lastChunkZ += MAX_CHUNK_LENGTH, MAX_CHUNK_RADIUS);
    }

}
