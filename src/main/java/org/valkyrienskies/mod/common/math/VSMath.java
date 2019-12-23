package org.valkyrienskies.mod.common.math;

import java.util.List;
import lombok.experimental.UtilityClass;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.joml.Matrix3d;
import org.joml.Matrix3dc;
import org.joml.Matrix4d;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.valkyrienskies.mod.common.coordinates.VectorImmutable;

/**
 * A lot of useful math functions belong here
 *
 * @author thebest108
 */
@UtilityClass
public class VSMath {

    public static final int AABB_MERGE_PASSES = 5;
    public static final double STANDING_TOLERANCE = .42D;

    public Vector3d toVector3d(Vec3i vec) {
        return new Vector3d(vec.getX(), vec.getY(), vec.getZ());
    }

    public Vector3d toVector3d(Vec3d vec) {
        return new Vector3d(vec.x, vec.y, vec.z);
    }

    /**
     * Converts a double-array matrix to a JOML {@link Matrix4d}
     *
     * @param matrix A 4x4 row-major double-array matrix
     * @return A {@link Matrix4d} representation
     */
    public static Matrix4d convertArrayMatrix4d(double[] matrix) {
        return new Matrix4d().set(matrix).transpose();
    }

    /**
     * Converts a double-array matrix into a JOML {@link Matrix3d}
     *
     * @param matrix A 3x3 row-major double-array matrix
     * @return A {@link Matrix3d} representation
     */
    public static Matrix3d convertArrayMatrix3d(double[] matrix) {
        return new Matrix3d().set(matrix).transpose();
    }

    public static double getPitchFromVectorImmutable(VectorImmutable vec) {
        return -Math.asin(vec.getY()) * 180 / Math.PI;
    }

    public static double getYawFromVectorImmutable(VectorImmutable vec, double rotPitch) {
        double f2 = -Math.cos(-rotPitch * (Math.PI / 180));
        double yawFromRotVec = Math.atan2(vec.getX() / f2, vec.getZ() / f2);
        yawFromRotVec += Math.PI;
        yawFromRotVec /= -0.017453292F;
        return yawFromRotVec;
    }

    /**
     * Sorts the array, returns a new array of 2 elements. Element 0 is the minimum of the array
     * passed in, element 1 is the maximum of the array.
     */
    public static double[] getMinMaxOfArray(double[] elements) {
        double[] minMax = new double[2];
        minMax[0] = minMax[1] = elements[elements.length - 1];
        // We iterate backwards because that way the number we are comparing against is
        // 0, which doesn't have to get loaded into a register to be compared by the
        // cpu. Its not much, but it is technically faster.
        for (int i = elements.length - 2; i >= 0; i--) {
            minMax[0] = Math.min(minMax[0], elements[i]);
            minMax[1] = Math.max(minMax[1], elements[i]);
        }
        return minMax;
    }

    /**
     * Used by the collision code to determine if the player should slide when standing on a ship.
     * That depends on the angle of the normal relative to the Y vector (0, 1, 0).
     *
     * @return true/false
     */
    public static boolean canStandOnNormal(Vector normal) {
        double radius = normal.x * normal.x + normal.z * normal.z;
        return radius < STANDING_TOLERANCE;
    }

    /**
     * Takes an arrayList of AABB's and merges them into larger AABB's
     */
    public static void mergeAABBList(List<AxisAlignedBB> toFuse) {
        boolean changed = true;
        int passes = 0;
        while (changed && passes < AABB_MERGE_PASSES) {
            changed = false;
            passes++;
            for (int i = 0; i < toFuse.size(); i++) {
                AxisAlignedBB bb = toFuse.get(i);
                for (int j = i + 1; j < toFuse.size(); j++) {
                    AxisAlignedBB nextOne = toFuse.get(j);
                    if (connected(bb, nextOne)) {
                        AxisAlignedBB fused = getFusedBoundingBox(bb, nextOne);
                        toFuse.remove(j);
                        toFuse.remove(i);
                        toFuse.add(fused);
                        j = toFuse.size();
                        changed = true;
                    }
                }
            }
        }
    }

    private static AxisAlignedBB getFusedBoundingBox(AxisAlignedBB bb1, AxisAlignedBB bb2) {
        double mnX = bb1.minX;
        double mnY = bb1.minY;
        double mnZ = bb1.minZ;
        double mxX = bb1.maxX;
        double mxY = bb1.maxY;
        double mxZ = bb1.maxZ;
        if (bb2.minX < mnX) {
            mnX = bb2.minX;
        }
        if (bb2.minY < mnY) {
            mnY = bb2.minY;
        }
        if (bb2.minZ < mnZ) {
            mnZ = bb2.minZ;
        }
        if (bb2.maxX > mxX) {
            mxX = bb2.maxX;
        }
        if (bb2.maxY > mxY) {
            mxY = bb2.maxY;
        }
        if (bb2.maxZ > mxZ) {
            mxZ = bb2.maxZ;
        }
        return new AxisAlignedBB(mnX, mnY, mnZ, mxX, mxY, mxZ);
    }

    private static boolean connected(AxisAlignedBB bb1, AxisAlignedBB bb2) {
        return (connectedInX(bb1, bb2) || connectedInY(bb1, bb2) || connectedInZ(bb1, bb2));
    }

    private static boolean connectedInX(AxisAlignedBB bb1, AxisAlignedBB bb2) {
        return (intersectInX(bb1, bb2)) && (areXAligned(bb1, bb2));
    }

    private static boolean connectedInY(AxisAlignedBB bb1, AxisAlignedBB bb2) {
        return (intersectInY(bb1, bb2)) && (areYAligned(bb1, bb2));
    }

    private static boolean connectedInZ(AxisAlignedBB bb1, AxisAlignedBB bb2) {
        return (intersectInZ(bb1, bb2)) && (areZAligned(bb1, bb2));
    }

    private static boolean intersectInX(AxisAlignedBB bb1, AxisAlignedBB bb2) {
        return ((bb1.maxX >= bb2.minX) && (bb1.maxX < bb2.maxX)) || ((bb1.minX > bb2.minX) && (
            bb1.minX <= bb2.maxX));
    }

    private static boolean intersectInY(AxisAlignedBB bb1, AxisAlignedBB bb2) {
        return ((bb1.maxY >= bb2.minY) && (bb1.maxY < bb2.maxY)) || ((bb1.minY > bb2.minY) && (
            bb1.minY <= bb2.maxY));
    }

    private static boolean intersectInZ(AxisAlignedBB bb1, AxisAlignedBB bb2) {
        return ((bb1.maxZ >= bb2.minZ) && (bb1.maxZ < bb2.maxZ)) || ((bb1.minZ > bb2.minZ) && (
            bb1.minZ <= bb2.maxZ));
    }

    private static boolean areXAligned(AxisAlignedBB bb1, AxisAlignedBB bb2) {
        return (bb1.minY == bb2.minY) && (bb1.minZ == bb2.minZ) && (bb1.maxY == bb2.maxY) && (
            bb1.maxZ == bb2.maxZ);
    }

    private static boolean areYAligned(AxisAlignedBB bb1, AxisAlignedBB bb2) {
        return (bb1.minX == bb2.minX) && (bb1.minZ == bb2.minZ) && (bb1.maxX == bb2.maxX) && (
            bb1.maxZ == bb2.maxZ);
    }

    private static boolean areZAligned(AxisAlignedBB bb1, AxisAlignedBB bb2) {
        return (bb1.minX == bb2.minX) && (bb1.minY == bb2.minY) && (bb1.maxX == bb2.maxX) && (
            bb1.maxY == bb2.maxY);
    }

    /**
     * Interpolates between 2 circular numbers by assuming the shortest path taken.
     *
     * @param prev        Can be anything.
     * @param current     Can be anything.
     * @param partialStep Must be between 0 and 1.
     * @param modulus     Must be greater than 0.
     * @return Smoothly interpolated number.
     */
    public static double interpolateModulatedNumbers(double prev, double current,
        double partialStep, double modulus) {
        double delta = current - prev;
        double deltaForward = calculateRemainder(delta, modulus);
        double deltaBackward = deltaForward - modulus;
        // Choose the smallest of either delta based on their absolute value
        double shortestDelta = deltaForward < -deltaBackward ? deltaForward : deltaBackward;
        // Then we will interpolate in the shortest direction, being careful to limit the answer
        // between 0 and modulus.
        double interpolatedButPossiblyNegative = prev + shortestDelta * partialStep;
        return calculateRemainder(interpolatedButPossiblyNegative, modulus);
    }

    /**
     * @param dividend The number we calculate the remainder of; can be anything.
     * @param divisor  The number that we divide the dividend by the calculate the remainder; must
     *                 be greater than 0.
     * @return A number in the range [0, dividend).
     */
    private static double calculateRemainder(double dividend, double divisor) {
        double remainder = dividend % divisor;
        if (remainder < 0) {
            remainder += divisor;
        }
        return remainder;
    }

    public static Matrix3dc createRotationMatrix(double pitchRadians, double yawRadians,
        double rollRadians) {
        return new Matrix3d().rotateXYZ(pitchRadians, yawRadians, rollRadians);
    }

    public static Quaterniondc createRotationQuat(double pitchRadians, double yawRadians,
        double rollRadians) {
        return createRotationMatrix(pitchRadians, yawRadians, rollRadians)
            .getNormalizedRotation(new Quaterniond());
    }
}