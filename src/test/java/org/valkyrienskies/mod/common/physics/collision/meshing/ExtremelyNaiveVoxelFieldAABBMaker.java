package org.valkyrienskies.mod.common.physics.collision.meshing;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

/**
 * Only used for testing, don't actually use this ever. Its inefficient.
 */
public class ExtremelyNaiveVoxelFieldAABBMaker implements IVoxelFieldAABBMaker {

    private final Set<BlockPos> blockPosSet;
    private final BlockPos voxelFieldWorldCenter;

    public ExtremelyNaiveVoxelFieldAABBMaker(int x, int z) {
        this.blockPosSet = new HashSet<>();
        this.voxelFieldWorldCenter = new BlockPos(x, 0, z);
    }

    @Override
    public AxisAlignedBB makeVoxelFieldAABB() {
        int minX, minY, minZ, maxX, maxY, maxZ;
        minX = minY = minZ = Integer.MAX_VALUE;
        maxX = maxY = maxZ = Integer.MIN_VALUE;
        if (blockPosSet.isEmpty()) {
            return null;
        }

        for (BlockPos pos : blockPosSet) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }
        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public boolean addVoxel(int x, int y, int z) {
        assertValidInputs(x - getFieldCenter().getX(), y - getFieldCenter().getY(),
            z - getFieldCenter().getZ());
        return blockPosSet.add(new BlockPos(x, y, z));
    }

    @Override
    public boolean removeVoxel(int x, int y, int z) {
        assertValidInputs(x - getFieldCenter().getX(), y - getFieldCenter().getY(),
            z - getFieldCenter().getZ());
        return blockPosSet.remove(new BlockPos(x, y, z));
    }

    @Nonnull
    @Override
    public BlockPos getFieldCenter() {
        return voxelFieldWorldCenter;
    }

    private void assertValidInputs(int x, int y, int z) throws IllegalArgumentException {
        if (x < MIN_X || x > MAX_X || y < MIN_Y || y > MAX_Y || z < MIN_Z || z > MAX_Z) {
            throw new IllegalArgumentException(
                x + ":" + y + ":" + z + " is out of range from " + getFieldCenter());
        }
    }
}
