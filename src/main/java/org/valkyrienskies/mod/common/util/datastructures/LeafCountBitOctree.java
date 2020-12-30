package org.valkyrienskies.mod.common.util.datastructures;

public class LeafCountBitOctree extends SimpleBitOctree implements IBitOctreeWithLeafCounts {

    private final short[] leafCounts;
    private int overallLeafCount;

    public LeafCountBitOctree() {
        this.leafCounts = new short[TOTAL_NODES];
        this.overallLeafCount = 0;
    }

    @Override
    protected void updateOctrees(int x, int y, int z, boolean bit) {
        int levelThreeIndex = getOctreeLevelThreeIndex(x, y, z);
        int levelTwoIndex = getOctreeLevelTwoIndex(x, y, z, levelThreeIndex);
        int levelOneIndex = getOctreeLevelOneIndex(x, y, z, levelTwoIndex);

        if (bit) {
            // We added a water block, so increment all the counts
            overallLeafCount++;
            leafCounts[levelThreeIndex - BLOCKS_TOTAL]++;
            leafCounts[levelTwoIndex - BLOCKS_TOTAL]++;
            leafCounts[levelOneIndex - BLOCKS_TOTAL]++;
        } else {
            // We removed a water block, so decrement all the counts
            leafCounts[levelThreeIndex - BLOCKS_TOTAL]--;
            leafCounts[levelTwoIndex - BLOCKS_TOTAL]--;
            leafCounts[levelOneIndex - BLOCKS_TOTAL]--;
            overallLeafCount--;
        }

        super.updateOctrees(x, y, z, bit);
    }

    @Override
    public int getOverallLeafCount() {
        return overallLeafCount;
    }

    @Override
    public int getOctreeLevelThreeLeafCount(int offset) {
        return leafCounts[getOctreeLevelThreeIndex(offset) - BLOCKS_TOTAL];
    }

    @Override
    public int getOctreeLevelTwoLeafCount(int levelThreeIndex, int offset) {
        return leafCounts[getOctreeLevelTwoIndex(levelThreeIndex, offset) - BLOCKS_TOTAL];
    }

    @Override
    public int getOctreeLevelOneLeafCount(int levelTwoIndex, int offset) {
        return leafCounts[getOctreeLevelOneIndex(levelTwoIndex, offset) - BLOCKS_TOTAL];
    }

    @Override
    public int leavesInRange(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        int leavesCount = 0;
        for (int levelThree = 0; levelThree < 8; levelThree++) {
            final int levelThreeIndex = getOctreeLevelThreeIndex(levelThree);
            if (!getAtIndex(levelThreeIndex)) {
                // This octree node doesn't exist
                continue;
            }
            final int levelThreeRangeOverlap = isLevelThreeInRange(minX, minY, minZ, maxX, maxY, maxZ, levelThree);

            if (levelThreeRangeOverlap == FULLY_IN_RANGE) {
                leavesCount += getOctreeLevelThreeLeafCount(levelThree);
                continue;
            }
            if (levelThreeRangeOverlap == NOT_IN_RANGE) {
                continue;
            }

            // Partially in range, go deeper
            for (int levelTwo = 0; levelTwo < 8; levelTwo++) {
                final int levelTwoIndex = getOctreeLevelTwoIndex(levelThreeIndex, levelTwo);
                if (!getAtIndex(levelTwoIndex)) {
                    // This octree node doesn't exist
                    continue;
                }

                final int levelTwoRangeOverlap = isLevelTwoInRange(minX, minY, minZ, maxX, maxY, maxZ, levelThree, levelTwo);

                if (levelTwoRangeOverlap == FULLY_IN_RANGE) {
                    leavesCount += getOctreeLevelTwoLeafCount(levelThreeIndex, levelTwo);
                    continue;
                }
                if (levelTwoRangeOverlap == NOT_IN_RANGE) {
                    continue;
                }

                // Partially in range, go deeper
                for (int levelOne = 0; levelOne < 8; levelOne++) {
                    final int levelOneIndex = getOctreeLevelOneIndex(levelTwoIndex, levelOne);

                    if (!getAtIndex(levelOneIndex)) {
                        // This octree node doesn't exist
                        continue;
                    }

                    final int levelOneRangeOverlap = isLevelOneInRange(minX, minY, minZ, maxX, maxY, maxZ, levelThree, levelTwo, levelOne);

                    if (levelOneRangeOverlap == FULLY_IN_RANGE) {
                        leavesCount += getOctreeLevelOneLeafCount(levelTwoIndex, levelOne);
                        continue;
                    }
                    if (levelOneRangeOverlap == NOT_IN_RANGE) {
                        continue;
                    }
                    final int baseX =
                            ((levelThree % 2) * 8) + ((levelTwo % 2) * 4)
                                    + ((levelOne % 2) * 2);
                    final int baseY = (((levelThree >> 1) % 2) * 8) + (
                            ((levelTwo >> 1) % 2) * 4)
                            + (((levelOne >> 1) % 2) * 2);
                    final int baseZ = (((levelThree >> 2) % 2) * 8) + (
                            ((levelTwo >> 2) % 2) * 4)
                            + (((levelOne >> 2) % 2) * 2);

                    for (int j = 0; j < 8; j++) {
                        final int loopX = baseX + (j & 1);
                        final int loopY = baseY + ((j & 2) >> 1);
                        final int loopZ = baseZ + ((j & 4) >> 2);
                        if (loopX < minX || loopX > maxX) {
                            continue;
                        }
                        if (loopY < minY || loopY > maxY) {
                            continue;
                        }
                        if (loopZ < minZ || loopZ > maxZ) {
                            continue;
                        }
                        if (get(loopX, loopY, loopZ)) {
                            leavesCount++;
                        }
                    }

                }
            }
        }
        return leavesCount;
    }

    private static final int NOT_IN_RANGE = 0;
    private static final int PARTIALLY_IN_RANGE = 1;
    private static final int FULLY_IN_RANGE = 2;

    /**
     *
     * @param minX Between 0 and 15
     * @param minY Between 0 and 15
     * @param minZ Between 0 and 15
     * @param maxX Between 0 and 15
     * @param maxY Between 0 and 15
     * @param maxZ Between 0 and 15
     * @param levelThreeIndex Between 0 and 7
     * @return Returns {@link #NOT_IN_RANGE} if levelThreeIndex isn't in range, {@link #PARTIALLY_IN_RANGE} if its partially in range,
     *      * and {@link #FULLY_IN_RANGE} if fully in the range.
     */
    private int isLevelThreeInRange(final int minX, final int minY, final int minZ,
                                    final int maxX, final int maxY, final int maxZ, final int levelThreeIndex) {
        final boolean lowBit = (levelThreeIndex & 1) != 0;
        final boolean midBit = (levelThreeIndex & 2) != 0;
        final boolean highBit = (levelThreeIndex & 4) != 0;

        final int levelThreeMinX = lowBit ? 8 : 0;
        final int levelThreeMinY = midBit ? 8 : 0;
        final int levelThreeMinZ = highBit ? 8 : 0;

        final int levelThreeMaxX = levelThreeMinX + 7;
        final int levelThreeMaxY = levelThreeMinY + 7;
        final int levelThreeMaxZ = levelThreeMinZ + 7;

        return isRangeInRange(minX, minY, minZ, maxX, maxY, maxZ, levelThreeMinX, levelThreeMinY, levelThreeMinZ, levelThreeMaxX, levelThreeMaxY, levelThreeMaxZ);
    }

    private int isLevelTwoInRange(final int minX, final int minY, final int minZ,
                                    final int maxX, final int maxY, final int maxZ,
                                    final int levelThreeIndex, final int levelTwoIndex) {
        final boolean levelThreeLowBit = (levelThreeIndex & 1) != 0;
        final boolean levelThreeMidBit = (levelThreeIndex & 2) != 0;
        final boolean levelThreeHighBit = (levelThreeIndex & 4) != 0;

        final boolean levelTwoLowBit = (levelTwoIndex & 1) != 0;
        final boolean levelTwoMidBit = (levelTwoIndex & 2) != 0;
        final boolean levelTwoHighBit = (levelTwoIndex & 4) != 0;

        final int levelTwoMinX = (levelThreeLowBit ? 8 : 0) + (levelTwoLowBit ? 4 : 0);
        final int levelTwoMinY = (levelThreeMidBit ? 8 : 0) + (levelTwoMidBit ? 4 : 0);
        final int levelTwoMinZ = (levelThreeHighBit ? 8 : 0) + (levelTwoHighBit ? 4 : 0);

        final int levelTwoMaxX = levelTwoMinX + 3;
        final int levelTwoMaxY = levelTwoMinY + 3;
        final int levelTwoMaxZ = levelTwoMinZ + 3;

        return isRangeInRange(minX, minY, minZ, maxX, maxY, maxZ, levelTwoMinX, levelTwoMinY, levelTwoMinZ, levelTwoMaxX, levelTwoMaxY, levelTwoMaxZ);
    }

    private int isLevelOneInRange(final int minX, final int minY, final int minZ,
                                  final int maxX, final int maxY, final int maxZ,
                                  final int levelThreeIndex, final int levelTwoIndex, final int levelOneIndex) {
        final boolean levelThreeLowBit = (levelThreeIndex & 1) != 0;
        final boolean levelThreeMidBit = (levelThreeIndex & 2) != 0;
        final boolean levelThreeHighBit = (levelThreeIndex & 4) != 0;

        final boolean levelTwoLowBit = (levelTwoIndex & 1) != 0;
        final boolean levelTwoMidBit = (levelTwoIndex & 2) != 0;
        final boolean levelTwoHighBit = (levelTwoIndex & 4) != 0;

        final boolean levelOneLowBit = (levelOneIndex & 1) != 0;
        final boolean levelOneMidBit = (levelOneIndex & 2) != 0;
        final boolean levelOneHighBit = (levelOneIndex & 4) != 0;

        final int levelOneMinX = (levelThreeLowBit ? 8 : 0) + (levelTwoLowBit ? 4 : 0) + (levelOneLowBit ? 2 : 0);
        final int levelOneMinY = (levelThreeMidBit ? 8 : 0) + (levelTwoMidBit ? 4 : 0) + (levelOneMidBit ? 2 : 0);
        final int levelOneMinZ = (levelThreeHighBit ? 8 : 0) + (levelTwoHighBit ? 4 : 0) + (levelOneHighBit ? 2 : 0);

        final int levelOneMaxX = levelOneMinX + 1;
        final int levelOneMaxY = levelOneMinY + 1;
        final int levelOneMaxZ = levelOneMinZ + 1;

        return isRangeInRange(minX, minY, minZ, maxX, maxY, maxZ, levelOneMinX, levelOneMinY, levelOneMinZ, levelOneMaxX, levelOneMaxY, levelOneMaxZ);
    }

    private int isRangeInRange(final int minX, final int minY, final int minZ,
                               final int maxX, final int maxY, final int maxZ,
                               final int otherMinX, final int otherMinY, final int otherMinZ,
                               final int otherMaxX, final int otherMaxY, final int otherMaxZ) {
        final int xStatus = doRangesOverlap(minX, maxX, otherMinX, otherMaxX);
        final int yStatus = doRangesOverlap(minY, maxY, otherMinY, otherMaxY);
        final int zStatus = doRangesOverlap(minZ, maxZ, otherMinZ, otherMaxZ);

        if (xStatus == FULLY_IN_RANGE && yStatus == FULLY_IN_RANGE && zStatus == FULLY_IN_RANGE) {
            return FULLY_IN_RANGE;
        } else if (xStatus != NOT_IN_RANGE && yStatus != NOT_IN_RANGE && zStatus != NOT_IN_RANGE) {
            return PARTIALLY_IN_RANGE;
        } else {
            return NOT_IN_RANGE;
        }
    }

    private int doRangesOverlap(final int min1, final int max1, final int min2, final int max2) {
        if (min1 <= min2 && max1 >= max2) {
            return FULLY_IN_RANGE;
        } else if (min1 <= max2 && min2 <= max1) {
            return PARTIALLY_IN_RANGE;
        } else {
            return NOT_IN_RANGE;
        }
    }
}
