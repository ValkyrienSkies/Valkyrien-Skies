package org.valkyrienskies.mod.common.util.datastructures;

public interface IBitOctree {

    int BLOCKS_TOTAL = 4096;
    int TREE_LEVEL_ONE = 512;
    int TREE_LEVEL_TWO = 64;
    int TREE_LEVEL_THREE = 8;
    int BITS_TOTAL = BLOCKS_TOTAL + TREE_LEVEL_ONE + TREE_LEVEL_TWO + TREE_LEVEL_THREE;

    void set(int x, int y, int z, boolean bit);

    boolean get(int x, int y, int z);

    boolean getAtIndex(int index);

    int getOctreeLevelOneIndex(int levelTwoIndex, int offset);

    int getOctreeLevelTwoIndex(int levelThreeIndex, int offset);

    int getOctreeLevelThreeIndex(int offset);

    /**
     * You'd think there would be clever way of doing this faster but NOPE!
     *
     * When theres only 4096 elements in a bit array you might as well just count the naive way. Fancy tricks have too much overhead.
     */
    default int getCountInRange(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        int count = 0;
        // Iterate from x to z, order is important because X has the highest bits in get() and Z has the lowest bits in get().
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (get(x, y, z)) count++;
                }
            }
        }
        return count;
    }
}