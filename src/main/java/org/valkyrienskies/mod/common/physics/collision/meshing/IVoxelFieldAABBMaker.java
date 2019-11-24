package org.valkyrienskies.mod.common.physics.collision.meshing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.joml.Vector3ic;

/**
 * This class enables the updating of ship AABBs in O(1) time
 */
public interface IVoxelFieldAABBMaker {

    int MIN_X = -512, MAX_X = 511, MIN_Y = 0, MAX_Y = 255, MIN_Z = -512, MAX_Z = 511;

    /**
     * Creates an AABB in world space.
     *
     * @return Null if there are no voxels in the voxel field.
     */
    @Nullable
    AxisAlignedBB makeVoxelFieldAABB();

    /**
     * Adds a voxel to the field
     *
     * @param x Must be between [getFieldCenter().getX() - 512, getFieldCenter().getX() + 511].
     * @param y Must be between [0, 255].
     * @param z Must be between [getFieldCenter().getZ() - 512, getFieldCenter().getZ() + 511].
     * @return True if a voxel didn't already exist at xyz, false otherwise.
     */
    boolean addVoxel(int x, int y, int z) throws IllegalArgumentException;

    /**
     * Removes a voxel from the field
     *
     * @param x Must be between [getFieldCenter().getX() - 512, getFieldCenter().getX() + 511].
     * @param y Must be between [0, 255].
     * @param z Must be between [getFieldCenter().getZ() - 512, getFieldCenter().getZ() + 511].
     * @return True if a voxel existed at xyz, false otherwise.
     */
    boolean removeVoxel(int x, int y, int z) throws IllegalArgumentException;

    /**
     * @return The BlockPos at the center of this voxel field.
     */
    @Nonnull
    BlockPos getFieldCenter();

    // region OVERLOADS

    default boolean addVoxel(Vec3i vec) {
        return this.addVoxel(vec.getX(), vec.getY(), vec.getZ());
    }

    default boolean removeVoxel(Vec3i vec) {
        return this.removeVoxel(vec.getX(), vec.getY(), vec.getZ());
    }

    default boolean addVoxel(Vector3ic vec) {
        return this.addVoxel(vec.x(), vec.y(), vec.z());
    }

    default boolean removeVoxel(Vector3ic vec) {
        return this.removeVoxel(vec.x(), vec.y(), vec.z());
    }

    // endregion
}
