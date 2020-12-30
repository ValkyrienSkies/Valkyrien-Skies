package org.valkyrienskies.mod.common.util.datastructures;

import org.junit.jupiter.api.RepeatedTest;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LeafCountBitOctreeTest {

    @RepeatedTest(25)
    public void testLeafCounts() {
        final ThreadLocalRandom random = ThreadLocalRandom.current();

        final LeafCountBitOctree octree = new LeafCountBitOctree();

        final boolean[][][] referenceCopy = new boolean[16][16][16];

        for (int i = 0; i < 10000; i++) {
            final int posX = random.nextInt(16);
            final int posY = random.nextInt(16);
            final int posZ = random.nextInt(16);
            final boolean set = random.nextBoolean();

            octree.set(posX, posY, posZ, set);
            referenceCopy[posX][posY][posZ] = set;
        }

        assertEquals(countInRange(0, 0, 0, 15, 15, 15, referenceCopy), octree.getOverallLeafCount());

        for (int i = 0; i < 100; i++) {
            final int minX = random.nextInt(16);
            final int minY = random.nextInt(16);
            final int minZ = random.nextInt(16);

            final int maxX = random.nextInt(minX, 16);
            final int maxY = random.nextInt(minY, 16);
            final int maxZ = random.nextInt(minZ, 16);

            final int referenceCount = countInRange(minX, minY, minZ, maxX, maxY, maxZ, referenceCopy);
            final int octreeCount = octree.leavesInRange(minX, minY, minZ, maxX, maxY, maxZ);

            assertEquals(referenceCount, octreeCount);
        }
    }

    private static int countInRange(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean[][][] voxels) {
        int referenceCount = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (voxels[x][y][z]) {
                        referenceCount++;
                    }
                }
            }
        }
        return referenceCount;
    }

}
