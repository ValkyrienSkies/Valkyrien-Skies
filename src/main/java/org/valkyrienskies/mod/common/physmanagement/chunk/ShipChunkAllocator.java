package org.valkyrienskies.mod.common.physmanagement.chunk;

import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;
import java.util.Map;

public class ShipChunkAllocator {

    // Each ship gets its own 32x32 square of chunks to do whatever it wants
    public static final int MAX_SHIP_CHUNK_LENGTH = 32;
    public static final int MAX_SHIP_CHUNK_RADIUS = (MAX_SHIP_CHUNK_LENGTH / 2) - 1;

    public static final int SHIP_CHUNK_X_START = 320000;
    public static final int SHIP_CHUNK_Z_START = 0;

    public static boolean isLikelyShipChunk(int chunkX, int chunkZ) {
        return chunkX >= SHIP_CHUNK_X_START && chunkZ >= SHIP_CHUNK_Z_START;
    }

    private int nextChunkX = SHIP_CHUNK_X_START;
    private int nextChunkZ = SHIP_CHUNK_Z_START;

    private Map<ChunkAllocation, String> chunkAllocationToShipIds = new HashMap<>();

    public ChunkAllocation allocateChunks(String shipId, int chunkRadius) {
        // Don't go over the maximum
        if (chunkRadius > MAX_SHIP_CHUNK_RADIUS) {
            System.err.println(shipId + " just tried allocating chunks with a radius of: " + chunkRadius);
            System.err.println("This is just too big! Expect bad luck and decades of instability after this!");
            return null;
        }
        ChunkAllocation nextAllocation = allocateNext();
        chunkAllocationToShipIds.put(nextAllocation, shipId);
        return nextAllocation;
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
            System.err.println("Either you created a new world, or Valkyrien Warfare just lost track of every single ship chunk! If its case #2 then good luck dude your ships are screwed.");
            nextChunkX = SHIP_CHUNK_X_START;
            nextChunkZ = SHIP_CHUNK_Z_START;
        }
        // Then load the stuff I might end up implementing eventually.
        // TODO: Save the chunkAllocationToShipIds map
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
                return otherChunk.lowerChunkX == lowerChunkX && otherChunk.lowerChunkZ == lowerChunkZ;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Long.hashCode(((long) lowerChunkX << 32) | ((long) lowerChunkZ & 0xFFFFFFFL));
        }
    }
}
