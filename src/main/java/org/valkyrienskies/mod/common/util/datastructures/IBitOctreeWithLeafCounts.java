package org.valkyrienskies.mod.common.util.datastructures;

public interface IBitOctreeWithLeafCounts extends IBitOctree {

    int TOTAL_NODES = TREE_LEVEL_THREE + TREE_LEVEL_TWO + TREE_LEVEL_ONE;

    // The number of leaves in this octree
    int getOverallLeafCount();

    // Top level octree node, has 8x8x8 blocks
    int getOctreeLevelThreeLeafCount(int offset);

    // Mid level octree node, has 4x4x4 blocks
    int getOctreeLevelTwoLeafCount(int levelThreeIndex, int offset);

    // Bottom level octree node, has 2x2x2 blocks
    int getOctreeLevelOneLeafCount(int levelTwoIndex, int offset);

    int leavesInRange(int minX, int minY, int minZ, int maxX, int maxY, int maxZ);
}
