package org.valkyrienskies.mod.common.physmanagement.chunk;

import lombok.extern.log4j.Log4j2;
import net.minecraft.nbt.NBTTagCompound;

@Log4j2
public class ShipChunkAllocator {

    // Each ship gets its own 32x32 square of chunks to do whatever it wants
    public static final int MAX_SHIP_CHUNK_LENGTH = 32;
    public static final int MAX_SHIP_CHUNK_RADIUS = (MAX_SHIP_CHUNK_LENGTH / 2) - 1;

    public static final int SHIP_CHUNK_X_START = 320000;
    public static final int SHIP_CHUNK_Z_START = 0;

    private int nextChunkX = SHIP_CHUNK_X_START;
    private int nextChunkZ = SHIP_CHUNK_Z_START;

    // The +50 is used to make sure chunks too close to ships dont interfere
    public static boolean isLikelyShipChunk(int chunkX, int chunkZ) {
        boolean likelyLegacy = chunkZ < -1870000 + 12 + 50;
        return likelyLegacy || ShipChunkAllocator.isLikelyShipChunk(chunkX, chunkZ);
    }

    /**
     * This finds the next empty chunkSet for use, currently only increases the xPos to get new
     * positions
     */
    public VSChunkClaim getNextAvailableChunkSet(int radius) {

        // TODO: Add the ship id to the allocation eventually.
        ChunkAllocation allocatedChunks = this.allocateChunks("insert ship id here", radius);

        return new VSChunkClaim(
            allocatedChunks.lowerChunkX + ShipChunkAllocator.MAX_SHIP_CHUNK_RADIUS,
            allocatedChunks.lowerChunkZ + ShipChunkAllocator.MAX_SHIP_CHUNK_RADIUS, radius);
    }

    public ChunkAllocation allocateChunks(String shipId, int chunkRadius) {
        // Don't go over the maximum
        if (chunkRadius > MAX_SHIP_CHUNK_RADIUS) {
            log.error(
                shipId + " just tried allocating chunks with a radius of: " + chunkRadius + "\n" +
                    "This is just too big! Expect bad luck and decades of instability after this!");

            throw new RuntimeException(shipId + " just tried allocating chunks with a radius of: " +
                chunkRadius + "\n" + "This is just too big! Expect bad luck and decades of "
                + "instability after this!");
        }
        return allocateNext();
    }

    private ChunkAllocation allocateNext() {
        ChunkAllocation next = new ChunkAllocation(nextChunkX, nextChunkZ);
        nextChunkZ += MAX_SHIP_CHUNK_LENGTH;
        return next;
    }

    public void writeToNBT(NBTTagCompound toReturn) {
        toReturn.setInteger("nextChunkX", nextChunkX);
        toReturn.setInteger("nextChunkZ", nextChunkZ);
    }

    public void readFromNBT(NBTTagCompound compound) {
        // First load the stuff that actually matters
        if (compound.hasKey("nextChunkX") && compound.hasKey("nextChunkZ")) {
            nextChunkX = compound.getInteger("nextChunkX");
            nextChunkZ = compound.getInteger("nextChunkZ");
        } else {
            log.error("Either you created a new world, or Valkyrien Skies just lost track of every "
                + "single ship chunk! If its case #2 then good luck dude your ships are screwed.");
            nextChunkX = SHIP_CHUNK_X_START;
            nextChunkZ = SHIP_CHUNK_Z_START;
        }
    }

    /**
     * Allocates a 32x32 square of chunks.
     */
    public static class ChunkAllocation {

        final int lowerChunkX, lowerChunkZ;

        private ChunkAllocation(int lowerChunkX, int lowerChunkZ) {
            this.lowerChunkX = lowerChunkX;
            this.lowerChunkZ = lowerChunkZ;
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof ChunkAllocation) {
                ChunkAllocation otherChunk = (ChunkAllocation) other;
                return otherChunk.lowerChunkX == lowerChunkX
                    && otherChunk.lowerChunkZ == lowerChunkZ;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Long.hashCode(((long) lowerChunkX << 32) | ((long) lowerChunkZ & 0xFFFFFFFL));
        }
    }
}
