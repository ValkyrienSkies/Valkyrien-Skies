package org.valkyrienskies.mod.common.physics.collision.optimization;

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
}