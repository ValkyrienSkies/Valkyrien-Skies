package org.valkyrienskies.mod.common.util.datastructures;

import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;
import org.valkyrienskies.mod.common.util.datastructures.ExtremelyNaiveVoxelFieldAABBMaker;
import org.valkyrienskies.mod.common.util.datastructures.SmallBlockPosSetAABB;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SmallBlockPosSetAABBTest {

    @Test
    public void testSmallBlockPosSetAABB() {
        SmallBlockPosSetAABB toTest = new SmallBlockPosSetAABB(0, 0, 0, 1024, 1024, 1024);
        ExtremelyNaiveVoxelFieldAABBMaker aabbMaker = new ExtremelyNaiveVoxelFieldAABBMaker(0, 0);


        // Test adding new positions
        BlockPos pos0 = new BlockPos(5, 10, 3);
        assertEquals(toTest.add(pos0), aabbMaker.addVoxel(pos0));
        assertEquals(toTest.makeAABB(), aabbMaker.makeVoxelFieldAABB());

        BlockPos pos1 = new BlockPos(2, 5, 3);
        assertEquals(toTest.add(pos1), aabbMaker.addVoxel(pos1));
        assertEquals(toTest.makeAABB(), aabbMaker.makeVoxelFieldAABB());

        BlockPos pos2 = new BlockPos(1, 20, 0);
        assertEquals(toTest.add(pos2), aabbMaker.addVoxel(pos2));
        assertEquals(toTest.makeAABB(), aabbMaker.makeVoxelFieldAABB());


        // Test adding duplicates
        BlockPos pos3 = new BlockPos(1, 20, 0);
        assertEquals(toTest.add(pos3), aabbMaker.addVoxel(pos3));
        assertEquals(toTest.makeAABB(), aabbMaker.makeVoxelFieldAABB());


        // Test removing what doesn't exist
        BlockPos pos4 = new BlockPos(6, 7, 8);
        assertEquals(toTest.remove(pos4), aabbMaker.removeVoxel(pos4));
        assertEquals(toTest.makeAABB(), aabbMaker.makeVoxelFieldAABB());


        // Test removing what does exist
        BlockPos pos5 = new BlockPos(5, 10, 3);
        assertEquals(toTest.remove(pos5), aabbMaker.removeVoxel(pos5));
        assertEquals(toTest.makeAABB(), aabbMaker.makeVoxelFieldAABB());

        BlockPos pos6 = new BlockPos(2, 5, 3);
        assertEquals(toTest.remove(pos6), aabbMaker.removeVoxel(pos6));
        assertEquals(toTest.makeAABB(), aabbMaker.makeVoxelFieldAABB());

        BlockPos pos7 = new BlockPos(1, 20, 0);
        assertEquals(toTest.remove(pos7), aabbMaker.removeVoxel(pos7));
        assertEquals(toTest.makeAABB(), aabbMaker.makeVoxelFieldAABB());


        // Test adding new positions
        BlockPos pos8 = new BlockPos(25, 2, 35);
        assertEquals(toTest.add(pos8), aabbMaker.addVoxel(pos8));
        assertEquals(toTest.makeAABB(), aabbMaker.makeVoxelFieldAABB());


        // Test clear
        toTest.clear();
        aabbMaker.clear();
        assertEquals(toTest.makeAABB(), aabbMaker.makeVoxelFieldAABB());
    }
}
