package org.valkyrienskies.mod.common.physics.collision.meshing;

import java.util.TreeMap;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

/**
 * Do not serialize.
 */
public class NaiveVoxelFieldAABBMaker implements IVoxelFieldAABBMaker {


    private final BlockPos centerPos;
    private final TreeMap<Integer, TreeSet<TwoInts>> xMap, yMap, zMap;
    private BlockPos minCoords, maxCoords;
    private int voxelCount;

    public NaiveVoxelFieldAABBMaker(int x, int z) {
        this.centerPos = new BlockPos(x, 0, z);
        this.xMap = new TreeMap<>();
        this.yMap = new TreeMap<>();
        this.zMap = new TreeMap<>();
        this.minCoords = null;
        this.maxCoords = null;
        this.voxelCount = 0;
    }

    @Override
    public AxisAlignedBB makeVoxelFieldAABB() {
        if (voxelCount == 0) {
            return null;
        }
        AxisAlignedBB inLocal = new AxisAlignedBB(minCoords, maxCoords);
        return inLocal.offset(centerPos);
    }

    @Override
    public boolean addVoxel(int x, int y, int z) {
        // Put xyz into local coordinates.
        x -= centerPos.getX();
        y -= centerPos.getY();
        z -= centerPos.getZ();

        assertValidInputs(x, y, z);

        // Update the treemaps.
        boolean isVoxelNew = false;
        if (!xMap.containsKey(x)) {
            xMap.put(x, new TreeSet<>());
        }
        TwoInts yz = new TwoInts(y, z);
        if (xMap.get(x).add(yz)) {
            isVoxelNew = true;
        }
        if (!yMap.containsKey(y)) {
            yMap.put(y, new TreeSet<>());
        }
        TwoInts xz = new TwoInts(x, z);
        if (yMap.get(y).add(xz)) {
            isVoxelNew = true;
        }
        if (!zMap.containsKey(z)) {
            zMap.put(z, new TreeSet<>());
        }
        TwoInts xy = new TwoInts(x, y);
        if (zMap.get(z).add(xy)) {
            isVoxelNew = true;
        }

        if (!isVoxelNew) {
            // Nothing removed, nothing to change.
            return false;
        }
        // A voxel was added, update the voxel count
        voxelCount++;

        // Update max/min coords
        if (minCoords == null || maxCoords == null) {
            minCoords = new BlockPos(x, y, z);
            maxCoords = new BlockPos(x, y, z);
            return true;
        }
        if (x > maxCoords.getX() || y > maxCoords.getY() || z > maxCoords.getZ()) {
            maxCoords = new BlockPos(Math.max(x, maxCoords.getX()), Math.max(y, maxCoords.getY()),
                Math.max(z, maxCoords.getZ()));
        }
        if (x < minCoords.getX() || y < minCoords.getY() || z < minCoords.getZ()) {
            minCoords = new BlockPos(Math.min(x, minCoords.getX()), Math.min(y, minCoords.getY()),
                Math.min(z, minCoords.getZ()));
        }
        return true;
    }

    @Override
    public boolean removeVoxel(int x, int y, int z) {
        // Put xyz into local coordinates.
        x -= centerPos.getX();
        y -= centerPos.getY();
        z -= centerPos.getZ();

        assertValidInputs(x, y, z);

        // Update the treemaps.
        boolean isVoxelRemoved = false;
        if (!xMap.containsKey(x)) {
            xMap.put(x, new TreeSet<>());
        }
        TwoInts yz = new TwoInts(y, z);
        if (xMap.get(x).remove(yz)) {
            isVoxelRemoved = true;
        }
        if (!yMap.containsKey(y)) {
            yMap.put(y, new TreeSet<>());
        }
        TwoInts xz = new TwoInts(x, z);
        if (yMap.get(y).remove(xz)) {
            isVoxelRemoved = true;
        }
        if (!zMap.containsKey(z)) {
            zMap.put(z, new TreeSet<>());
        }
        TwoInts xy = new TwoInts(x, y);
        if (zMap.get(z).remove(xy)) {
            isVoxelRemoved = true;
        }

        if (!isVoxelRemoved) {
            // Nothing removed, nothing to change.
            return false;
        }
        // A voxel was removed, update the voxel count
        voxelCount--;

        // Update max/min coords
        // Update maxCoords.
        if (x == maxCoords.getX() || y == maxCoords.getY() || z == maxCoords.getZ()) {
            int newMaxX = maxCoords.getX();
            int newMaxY = maxCoords.getY();
            int newMaxZ = maxCoords.getZ();
            if (x == maxCoords.getX()) {
                for (int i = newMaxX; i >= MIN_X; i--) {
                    if (xMap.containsKey(i)) {
                        if (!xMap.get(i).isEmpty()) {
                            newMaxX = i;
                            break;
                        }
                    }
                }
            }
            if (y == maxCoords.getY()) {
                for (int i = newMaxY; i >= MIN_Y; i--) {
                    if (yMap.containsKey(i)) {
                        if (!yMap.get(i).isEmpty()) {
                            newMaxY = i;
                            break;
                        }
                    }
                }
            }
            if (z == maxCoords.getZ()) {
                for (int i = newMaxZ; i >= MIN_Z; i--) {
                    if (zMap.containsKey(i)) {
                        if (!zMap.get(i).isEmpty()) {
                            newMaxZ = i;
                            break;
                        }
                    }
                }
            }
            maxCoords = new BlockPos(newMaxX, newMaxY, newMaxZ);
        }
        // Update minCoords.
        if (x == minCoords.getX() || y == minCoords.getY() || z == minCoords.getZ()) {
            int newMinX = minCoords.getX();
            int newMinY = minCoords.getY();
            int newMinZ = minCoords.getZ();
            if (x == minCoords.getX()) {
                for (int i = newMinX; i <= MAX_X; i++) {
                    if (xMap.containsKey(i)) {
                        if (!xMap.get(i).isEmpty()) {
                            newMinX = i;
                            break;
                        }
                    }
                }
            }
            if (y == minCoords.getY()) {
                for (int i = newMinY; i <= MAX_Y; i++) {
                    if (yMap.containsKey(i)) {
                        if (!yMap.get(i).isEmpty()) {
                            newMinY = i;
                            break;
                        }
                    }
                }
            }
            if (z == minCoords.getZ()) {
                for (int i = newMinZ; i <= MAX_Z; i++) {
                    if (zMap.containsKey(i)) {
                        if (!zMap.get(i).isEmpty()) {
                            newMinZ = i;
                            break;
                        }
                    }
                }
            }
            minCoords = new BlockPos(newMinX, newMinY, newMinZ);
        }

        return true;
    }

    @Nonnull
    @Override
    public BlockPos getFieldCenter() {
        return centerPos;
    }

    private void assertValidInputs(int x, int y, int z) throws IllegalArgumentException {
        if (x < MIN_X || x > MAX_X || y < MIN_Y || y > MAX_Y || z < MIN_Z || z > MAX_Z) {
            throw new IllegalArgumentException(
                x + ":" + y + ":" + z + " is out of range from " + getFieldCenter());
        }
    }

    /**
     * Actually just two integers.
     */
    private static class TwoInts implements Comparable<TwoInts> {

        final int first, second;

        TwoInts(int first, int second) {
            this.first = first;
            this.second = second;
        }

        // This needs to be sortable to work with TreeSet.
        @Override
        public int compareTo(TwoInts other) {
            if (first != other.first) {
                return first - other.first;
            } else {
                return second - other.second;
            }
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof TwoInts)) {
                return false;
            }
            TwoInts otherInts = (TwoInts) other;
            return first == otherInts.first && second == otherInts.second;
        }

        @Override
        public int hashCode() {
            return ((first + 512) << 14) | (second + 512);
        }
    }

}
