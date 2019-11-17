package org.valkyrienskies.mod.common.physics.collision.meshing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.RepeatedTest;

@SuppressWarnings({"UnusedDeclaration", "WeakerAccess"})
public class TestVoxelFieldAABBMaker {

    @RepeatedTest(25)
    public void naiveTest1() {
        NaiveVoxelFieldAABBMaker naive = new NaiveVoxelFieldAABBMaker(0, 0);
        ExtremelyNaiveVoxelFieldAABBMaker extreme = new ExtremelyNaiveVoxelFieldAABBMaker(0, 0);
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            int randomX = random.nextInt(512) - 256;
            int randomY = random.nextInt(256);
            int randomZ = random.nextInt(512) - 256;
            assertEquals(extreme.addVoxel(randomX, randomY, randomZ),
                    naive.addVoxel(randomX, randomY, randomZ));
            assertEquals(extreme.makeVoxelFieldAABB(), naive.makeVoxelFieldAABB());
        }

        for (int i = 0; i < 10000; i++) {
            int randomX = random.nextInt(512) - 256;
            int randomY = random.nextInt(256);
            int randomZ = random.nextInt(512) - 256;
            naive.removeVoxel(randomX, randomY, randomZ);
            extreme.removeVoxel(randomX, randomY, randomZ);
            assertEquals(extreme.makeVoxelFieldAABB(), naive.makeVoxelFieldAABB());
        }

        for (int i = 0; i < 100; i++) {
            int randomX = random.nextInt(512) - 256;
            int randomY = random.nextInt(256);
            int randomZ = random.nextInt(512) - 256;
            assertEquals(extreme.addVoxel(randomX, randomY, randomZ),
                    naive.addVoxel(randomX, randomY, randomZ));
            assertEquals(extreme.makeVoxelFieldAABB(), naive.makeVoxelFieldAABB());
        }

        for (int i = 0; i < 1000; i++) {
            int randomX = random.nextInt(512) - 256;
            int randomY = random.nextInt(256);
            int randomZ = random.nextInt(512) - 256;
            if (random.nextBoolean()) {
                assertEquals(extreme.addVoxel(randomX, randomY, randomZ),
                        naive.addVoxel(randomX, randomY, randomZ));
            } else {
                assertEquals(extreme.removeVoxel(randomX, randomY, randomZ),
                        naive.removeVoxel(randomX, randomY, randomZ));
            }
            assertEquals(extreme.makeVoxelFieldAABB(), naive.makeVoxelFieldAABB());
        }
    }

    @RepeatedTest(100)
    public void naiveTest2() {
        Random random = new Random();
        BlockPos centerPos = new BlockPos(random.nextInt() / 100, 0, random.nextInt() / 100);

        NaiveVoxelFieldAABBMaker naive = new NaiveVoxelFieldAABBMaker(centerPos.getX(),
                centerPos.getZ());
        ExtremelyNaiveVoxelFieldAABBMaker extreme = new ExtremelyNaiveVoxelFieldAABBMaker(
                centerPos.getX(), centerPos.getZ());

        List<BlockPos> blockPosList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            int randomX = random.nextInt(512) - 256 + centerPos.getX();
            int randomY = random.nextInt(256) + centerPos.getY();
            int randomZ = random.nextInt(512) - 256 + centerPos.getZ();

            assertEquals(extreme.addVoxel(randomX, randomY, randomZ),
                    naive.addVoxel(randomX, randomY, randomZ));
            assertEquals(extreme.makeVoxelFieldAABB(), naive.makeVoxelFieldAABB());
            blockPosList.add(new BlockPos(randomX, randomY, randomZ));
        }

        Collections.shuffle(blockPosList);

        for (BlockPos pos : blockPosList) {
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            assertEquals(extreme.removeVoxel(x, y, z),
                    naive.removeVoxel(x, y, z));

            assertEquals(extreme.makeVoxelFieldAABB(), naive.makeVoxelFieldAABB());
        }
    }

}
