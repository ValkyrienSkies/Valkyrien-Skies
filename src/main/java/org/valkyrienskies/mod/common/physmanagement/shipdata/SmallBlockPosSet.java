package org.valkyrienskies.mod.common.physmanagement.shipdata;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

/**
 * An implementation of IBlockPosSet that stores block positions as 1 integer. This is accomplished by storing each
 * position as its relative coordinates to the centerX and centerZ values of this set. In this implementation the x
 * and z positions are 12 bits each, so they can range anywhere from -2048 to + 2047 relative to centerX and centerZ.
 * This leaves 8 bits for storing the y coordinate, which allows it the range of 0 to 255, exactly the same as
 * Minecraft.
 */
public class SmallBlockPosSet implements IBlockPosSet {

    private static final int BOT_12_BITS = 0x00000FFF;
    private static final int BOT_8_BITS = 0x000000FF;

    private final TIntSet blockHashSet;
    private final int centerX;
    private final int centerZ;

    public SmallBlockPosSet(int centerX, int centerZ) {
        this.blockHashSet = new TIntHashSet();
        this.centerX = centerX;
        this.centerZ = centerZ;
    }

    @Override
    public boolean addPos(int x, int y, int z) {
        if (!canStorePos(x, y, z)) {
            throw new IllegalArgumentException("Cannot store block position at <" + x + "," + y + "," + z + ">");
        }
        return blockHashSet.add(calculateHash(x, y, z));
    }

    @Override
    public boolean removePos(int x, int y, int z) {
        if (!canStorePos(x, y, z)) {
            // Nothing to remove
            return false;
        }
        return blockHashSet.remove(calculateHash(x, y, z));
    }

    @Override
    public boolean hasPos(int x, int y, int z) {
        if (!canStorePos(x, y, z)) {
            // This pos cannot exist in this set
            return false;
        }
        return blockHashSet.contains(calculateHash(x, y, z));
    }

    @Override
    public boolean canStorePos(int x, int y, int z) {
        int xLocal = x - centerX;
        int zLocal = z - centerZ;
        return !(y < 0 | y > 255 | xLocal < -2048 | xLocal > 2047 | zLocal < -2048 | zLocal > 2047);
    }

    @Override
    public int size() {
        return blockHashSet.size();
    }

    private int calculateHash(int x, int y, int z) {
        // Allocate 12 bits for x, 12 bits for z, and 8 bits for y.
        int xBits = (x - centerX) & BOT_12_BITS;
        int yBits = y & BOT_8_BITS;
        int zBits = (z - centerZ) & BOT_12_BITS;
        return xBits | (yBits << 12) | (zBits << 20);
    }
}
