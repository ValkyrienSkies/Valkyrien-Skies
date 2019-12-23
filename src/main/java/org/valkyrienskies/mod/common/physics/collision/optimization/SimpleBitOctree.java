package org.valkyrienskies.mod.common.physics.collision.optimization;

public class SimpleBitOctree implements IBitOctree {

    private final IBitSet bitbuffer;

    public SimpleBitOctree() {
        bitbuffer = new SmallBitSet(BITS_TOTAL);
    }

    @Override
    public void set(int x, int y, int z, boolean bit) {
        int index = getBlockIndex(x, y, z);
        ensureCapacity(index);
        if (bitbuffer.get(index) != bit) {
            bitbuffer.set(index, bit);
            updateOctrees(x, y, z, bit);
        }
    }

    @Override
    public boolean get(int x, int y, int z) {
        return getAtIndex(getBlockIndex(x, y, z));
    }

    @Override
    public boolean getAtIndex(int index) {
        ensureCapacity(index);
        return bitbuffer.get(index);
    }

    @Override
    public int getOctreeLevelOneIndex(int levelTwoIndex, int offset) {
        return levelTwoIndex + offset + 1;
    }

    @Override
    public int getOctreeLevelTwoIndex(int levelThreeIndex, int offset) {
        return levelThreeIndex + (9 * offset) + 1;
    }

    @Override
    public int getOctreeLevelThreeIndex(int offset) {
        return BLOCKS_TOTAL + (73 * offset);
    }

    // If something tried calling code outside of the buffer size, throw an
    // IllegalArgumentException its way.
    private void ensureCapacity(int index) {
        if (index > BITS_TOTAL) {
            throw new IllegalArgumentException("Tried accessing an element out of bounds!");
        }
    }

    private void updateOctrees(int x, int y, int z, boolean bit) {
        int levelThreeIndex = getOctreeLevelThreeIndex(x, y, z);
        int levelTwoIndex = getOctreeLevelTwoIndex(x, y, z, levelThreeIndex);
        int levelOneIndex = getOctreeLevelOneIndex(x, y, z, levelTwoIndex);

        if (getAtIndex(levelOneIndex) != bit) {
            if (updateOctreeLevelOne(levelOneIndex, x, y, z)) {
                if (updateOctreeLevelTwo(levelTwoIndex)) {
                    updateOctreeLevelThree(levelThreeIndex);
                }
            }
        }
    }

    private void updateOctreeLevelThree(int levelThreeIndex) {
        if (bitbuffer.get(levelThreeIndex + 1) || bitbuffer.get(levelThreeIndex + 10)
            || bitbuffer.get(levelThreeIndex + 19) || bitbuffer.get(levelThreeIndex + 28)
            || bitbuffer.get(levelThreeIndex + 37) || bitbuffer.get(levelThreeIndex + 46)
            || bitbuffer.get(levelThreeIndex + 55) || bitbuffer.get(levelThreeIndex + 64)) {
            bitbuffer.set(levelThreeIndex);
        } else {
            bitbuffer.clear(levelThreeIndex);
        }
    }

    // Returns true if the next level of octree should be updated
    private boolean updateOctreeLevelTwo(int levelTwoIndex) {
        if (bitbuffer.get(levelTwoIndex + 1) || bitbuffer.get(levelTwoIndex + 2) || bitbuffer
            .get(levelTwoIndex + 3)
            || bitbuffer.get(levelTwoIndex + 4) || bitbuffer.get(levelTwoIndex + 5)
            || bitbuffer.get(levelTwoIndex + 6) || bitbuffer.get(levelTwoIndex + 7)
            || bitbuffer.get(levelTwoIndex + 8)) {
            if (!bitbuffer.get(levelTwoIndex)) {
                bitbuffer.set(levelTwoIndex);
                return true;
            }
        } else {
            if (bitbuffer.get(levelTwoIndex)) {
                bitbuffer.clear(levelTwoIndex);
                return true;
            }
        }
        return false;
    }

    // Returns true if the next level of octree should be updated
    private boolean updateOctreeLevelOne(int levelOneIndex, int x, int y, int z) {
        // Only keep the last 4 bits; 0x0E = 1110, also removes the last bit
        x &= 0x0E;
        y &= 0x0E;
        z &= 0x0E;
        if (get(x, y, z) || get(x, y, z + 1) || get(x, y + 1, z) || get(x, y + 1, z + 1) || get(
            x + 1, y, z)
            || get(x + 1, y, z + 1) || get(x + 1, y + 1, z) || get(x + 1, y + 1, z + 1)) {
            if (!bitbuffer.get(levelOneIndex)) {
                bitbuffer.set(levelOneIndex);
                return true;
            }
        } else {
            if (bitbuffer.get(levelOneIndex)) {
                bitbuffer.clear(levelOneIndex);
                return true;
            }
        }
        return false;
    }

    private int getOctreeLevelOneIndex(int x, int y, int z, int levelTwoIndex) {
        x = (x & 0x02) >> 1;
        y = (y & 0x02);
        z = (z & 0x02) << 1;
        return getOctreeLevelOneIndex(levelTwoIndex, x | y | z);
    }

    private int getOctreeLevelTwoIndex(int x, int y, int z, int levelThreeIndex) {
        x = (x & 0x04) >> 2;
        y = (y & 0x04) >> 1;
        z = (z & 0x04);
        return getOctreeLevelTwoIndex(levelThreeIndex, x | y | z);
    }

    private int getOctreeLevelThreeIndex(int x, int y, int z) {
        x = (x & 0x08) >> 3;
        y = (y & 0x08) >> 2;
        z = (z & 0x08) >> 1;
        return getOctreeLevelThreeIndex(x | y | z);
    }

    private int getBlockIndex(int x, int y, int z) {
        return x | (y << 4) | (z << 8);
    }

}
