/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2018 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package valkyrienwarfare.mod.common.math;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import valkyrienwarfare.api.TransformType;
import valkyrienwarfare.mod.common.coordinates.CoordinateSpaceType;
import valkyrienwarfare.mod.common.coordinates.ISubspacedEntity;
import valkyrienwarfare.mod.common.coordinates.ShipTransform;
import valkyrienwarfare.mod.common.coordinates.VectorImmutable;
import valkyrienwarfare.mod.common.physics.management.PhysicsWrapperEntity;

/**
 * This class creates and processes rotation matrix transforms used by Valkyrien
 * Warfare
 *
 * @author thebest108
 */
public class RotationMatrices {

    public static double[] getTranslationMatrix(double x, double y, double z) {
        double[] matrix = getDoubleIdentity();
        matrix[3] = x;
        matrix[7] = y;
        matrix[11] = z;
        return matrix;
    }

    public static double[] rotateAndTranslate(double[] input, double pitch, double yaw, double roll,
                                              Vector localOrigin) {
        input = RotationMatrices.getMatrixProduct(input,
                RotationMatrices.getRotationMatrix(1.0D, 0.0D, 0.0D, Math.toRadians(pitch)));
        input = RotationMatrices.getMatrixProduct(input,
                RotationMatrices.getRotationMatrix(0.0D, 1.0D, 0.0D, Math.toRadians(yaw)));
        input = RotationMatrices.getMatrixProduct(input,
                RotationMatrices.getRotationMatrix(0.0D, 0.0D, 1.0D, Math.toRadians(roll)));
        input = RotationMatrices.getMatrixProduct(input,
                RotationMatrices.getTranslationMatrix(-localOrigin.X, -localOrigin.Y, -localOrigin.Z));
        return input;
    }

    public static double[] rotateOnly(double[] input, double pitch, double yaw, double roll) {
        input = RotationMatrices.getMatrixProduct(input,
                RotationMatrices.getRotationMatrix(1.0D, 0.0D, 0.0D, Math.toRadians(pitch)));
        input = RotationMatrices.getMatrixProduct(input,
                RotationMatrices.getRotationMatrix(0.0D, 1.0D, 0.0D, Math.toRadians(yaw)));
        input = RotationMatrices.getMatrixProduct(input,
                RotationMatrices.getRotationMatrix(0.0D, 0.0D, 1.0D, Math.toRadians(roll)));
        return input;
    }

    /**
     * Returns a rotation matrix with the described rotation and no translation.
     *
     * @param pitch in degrees
     * @param yaw   in degrees
     * @param roll  in degrees
     * @return
     */
    public static double[] getRotationMatrix(double pitch, double yaw, double roll) {
        double[] input = RotationMatrices.getRotationMatrix(1.0D, 0.0D, 0.0D, Math.toRadians(pitch));
        input = RotationMatrices.getMatrixProduct(input,
                RotationMatrices.getRotationMatrix(0.0D, 1.0D, 0.0D, Math.toRadians(yaw)));
        input = RotationMatrices.getMatrixProduct(input,
                RotationMatrices.getRotationMatrix(0.0D, 0.0D, 1.0D, Math.toRadians(roll)));
        return input;
    }

    /**
     * Creates a rotation matrix for the given rotation axis and angle.
     *
     * @param ux    the x component of the rotation axis
     * @param uy    the y component of the rotation axis
     * @param uz    the z component of the rotation axis
     * @param angle in radians
     * @return
     */
    public static double[] getRotationMatrix(double ux, double uy, double uz, double angle) {
        if ((ux == 0.0D) && (uy == 0.0D) && (uz == 0.0D)) {
            return getDoubleIdentity();
        }
        double C = Math.cos(angle);
        double S = Math.sin(angle);
        double t = 1.0D - C;
        double axismag = Math.sqrt(ux * ux + uy * uy + uz * uz);
        ux /= axismag;
        uy /= axismag;
        uz /= axismag;
        double[] matrix = getDoubleIdentity();
        matrix[0] = (t * ux * ux + C);
        matrix[1] = (t * ux * uy - S * uz);
        matrix[2] = (t * ux * uz + S * uy);
        matrix[4] = (t * ux * uy + S * uz);
        matrix[5] = (t * uy * uy + C);
        matrix[6] = (t * uy * uz - S * ux);
        matrix[8] = (t * ux * uz - S * uy);
        matrix[9] = (t * uy * uz + S * ux);
        matrix[10] = (t * uz * uz + C);
        return matrix;
    }

    public static double[] getDoubleIdentity() {
        return new double[]{1.0D, 0, 0, 0, 0, 1.0D, 0, 0, 0, 0, 1.0D, 0, 0, 0, 0, 1.0D};
    }

    public static double[] getZeroMatrix(int size) {
        return new double[size * size];
    }

    public static double[] getMatrixProduct(double[] M1, double[] M2) {
        double[] product = new double[16];
        product[0] = (M1[0] * M2[0] + M1[1] * M2[4] + M1[2] * M2[8] + M1[3] * M2[12]);
        product[1] = (M1[0] * M2[1] + M1[1] * M2[5] + M1[2] * M2[9] + M1[3] * M2[13]);
        product[2] = (M1[0] * M2[2] + M1[1] * M2[6] + M1[2] * M2[10] + M1[3] * M2[14]);
        product[3] = (M1[0] * M2[3] + M1[1] * M2[7] + M1[2] * M2[11] + M1[3] * M2[15]);
        product[4] = (M1[4] * M2[0] + M1[5] * M2[4] + M1[6] * M2[8] + M1[7] * M2[12]);
        product[5] = (M1[4] * M2[1] + M1[5] * M2[5] + M1[6] * M2[9] + M1[7] * M2[13]);
        product[6] = (M1[4] * M2[2] + M1[5] * M2[6] + M1[6] * M2[10] + M1[7] * M2[14]);
        product[7] = (M1[4] * M2[3] + M1[5] * M2[7] + M1[6] * M2[11] + M1[7] * M2[15]);
        product[8] = (M1[8] * M2[0] + M1[9] * M2[4] + M1[10] * M2[8] + M1[11] * M2[12]);
        product[9] = (M1[8] * M2[1] + M1[9] * M2[5] + M1[10] * M2[9] + M1[11] * M2[13]);
        product[10] = (M1[8] * M2[2] + M1[9] * M2[6] + M1[10] * M2[10] + M1[11] * M2[14]);
        product[11] = (M1[8] * M2[3] + M1[9] * M2[7] + M1[10] * M2[11] + M1[11] * M2[15]);
        product[12] = (M1[12] * M2[0] + M1[13] * M2[4] + M1[14] * M2[8] + M1[15] * M2[12]);
        product[13] = (M1[12] * M2[1] + M1[13] * M2[5] + M1[14] * M2[9] + M1[15] * M2[13]);
        product[14] = (M1[12] * M2[2] + M1[13] * M2[6] + M1[14] * M2[10] + M1[15] * M2[14]);
        product[15] = (M1[12] * M2[3] + M1[13] * M2[7] + M1[14] * M2[11] + M1[15] * M2[15]);
        return product;
    }

    public static void applyTransform(double[] M, Vector vec) {
        double x = vec.X;
        double y = vec.Y;
        double z = vec.Z;
        vec.X = x * M[0] + y * M[1] + z * M[2] + M[3];
        vec.Y = x * M[4] + y * M[5] + z * M[6] + M[7];
        vec.Z = x * M[8] + y * M[9] + z * M[10] + M[11];
    }

    /**
     * Needs to be replaced with some more robust code.
     *
     * @param shipTransform
     * @param entity
     * @param transformType
     */
    @Deprecated
    public static void applyTransform(ShipTransform shipTransform, Entity entity, TransformType transformType) {
        if (entity instanceof PhysicsWrapperEntity) {
            throw new IllegalArgumentException(
                    "Tried applying a transform to the PhysicsWrapeerEntity, this creates instability so we crash here!");
        }
        ISubspacedEntity entitySubspaceTracker = (ISubspacedEntity) entity;

        // RIP
        if (false) {
            if (transformType == TransformType.SUBSPACE_TO_GLOBAL
                    && entitySubspaceTracker.currentSubspaceType() != CoordinateSpaceType.SUBSPACE_COORDINATES) {
                // throw new IllegalArgumentException(
                // "Entity " + entity.getName() + " is already in global coordinates. This is
                // wrong!");
                System.err.println("Entity " + entity.getName() + " is already in global coordinates. This is wrong!");
            }
            if (transformType == TransformType.GLOBAL_TO_SUBSPACE
                    && entitySubspaceTracker.currentSubspaceType() != CoordinateSpaceType.GLOBAL_COORDINATES) {
                throw new IllegalArgumentException(
                        "Entity " + entity.getName() + " is already in subspace coordinates. This is wrong!");
            }
        }

        Vector entityPos = new Vector(entity.posX, entity.posY, entity.posZ);
        Vector entityLook = new Vector(entity.getLook(1.0F));
        Vector entityMotion = new Vector(entity.motionX, entity.motionY, entity.motionZ);

        if (entity instanceof EntityFireball) {
            EntityFireball ball = (EntityFireball) entity;
            entityMotion.X = ball.accelerationX;
            entityMotion.Y = ball.accelerationY;
            entityMotion.Z = ball.accelerationZ;
        }

        shipTransform.transform(entityPos, transformType);
        shipTransform.rotate(entityLook, transformType);
        shipTransform.rotate(entityMotion, transformType);

        entityLook.normalize();

        // This is correct, works properly when tested with cows
        if (entity instanceof EntityLiving) {
            EntityLiving living = (EntityLiving) entity;
            living.rotationYawHead = entity.rotationYaw;
            living.prevRotationYawHead = entity.rotationYaw;
        }

        // ===== Fix change the entity rotation to be proper relative to ship space =====
        VectorImmutable entityLookImmutable = entityLook.toImmutable();
        double pitch = VWMath.getPitchFromVectorImmutable(entityLookImmutable);
        double yaw = VWMath.getYawFromVectorImmutable(entityLookImmutable, pitch);
        entity.rotationYaw = (float) yaw;
        entity.rotationPitch = (float) pitch;

        if (entity instanceof EntityFireball) {
            EntityFireball ball = (EntityFireball) entity;
            ball.accelerationX = entityMotion.X;
            ball.accelerationY = entityMotion.Y;
            ball.accelerationZ = entityMotion.Z;
        }

        entity.motionX = entityMotion.X;
        entity.motionY = entityMotion.Y;
        entity.motionZ = entityMotion.Z;

        entity.setPosition(entityPos.X, entityPos.Y, entityPos.Z);
    }

    public static BlockPos applyTransform(double[] M, BlockPos pos) {
        Vector blockPosVec = new Vector(pos.getX() + .5D, pos.getY() + .5D, pos.getZ() + .5D);
        applyTransform(M, blockPosVec);
        BlockPos newPos = new BlockPos(Math.round(blockPosVec.X - .5D), Math.round(blockPosVec.Y - .5D),
                Math.round(blockPosVec.Z - .5D));
        return newPos;
    }

    public static Vec3d applyTransform(double[] M, Vec3d vec) {
        double x = vec.x;
        double y = vec.y;
        double z = vec.z;
        return new Vec3d((x * M[0] + y * M[1] + z * M[2] + M[3]), (x * M[4] + y * M[5] + z * M[6] + M[7]),
                (x * M[8] + y * M[9] + z * M[10] + M[11]));
    }

    public static void applyTransform3by3(double[] M, Vector vec) {
        double xx = vec.X;
        double yy = vec.Y;
        double zz = vec.Z;
        vec.X = (xx * M[0] + yy * M[1] + zz * M[2]);
        vec.Y = (xx * M[3] + yy * M[4] + zz * M[5]);
        vec.Z = (xx * M[6] + yy * M[7] + zz * M[8]);
    }

    public static void doRotationOnly(double[] M, Vector vec) {
        double x = vec.X;
        double y = vec.Y;
        double z = vec.Z;
        vec.X = x * M[0] + y * M[1] + z * M[2];
        vec.Y = x * M[4] + y * M[5] + z * M[6];
        vec.Z = x * M[8] + y * M[9] + z * M[10];
    }

    public static float[] convertToFloat(double[] array) {
        float[] floatArray = new float[array.length];
        for (int i = 0; i < array.length; i++) {
            floatArray[i] = (float) array[i];
        }
        return floatArray;
    }

    public static void convertToFloat(double[] toConvert, float[] toFill) {
        for (int i = 0; i < toConvert.length; i++) {
            toFill[i] = (float) toConvert[i];
        }
    }

    public static double[] convertToDouble(float[] array) {
        double[] doubleArray = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            doubleArray[i] = array[i];
        }
        return doubleArray;
    }

    public static Vector get3by3TransformedVec(double[] M, Vector v) {
        Vector vec = new Vector(v);
        applyTransform3by3(M, vec);
        return vec;
    }

    public static Vector getTransformedVec(double[] M, Vector v) {
        Vector vec = new Vector(v);
        applyTransform(M, vec);
        return vec;
    }

    public static double[] inverse3by3(double[] matrix) {
        double[] inverse = new double[9];
        inverse[0] = (matrix[4] * matrix[8] - matrix[5] * matrix[7]);
        inverse[3] = (matrix[5] * matrix[6] - matrix[3] * matrix[8]);
        inverse[6] = (matrix[3] * matrix[7] - matrix[4] * matrix[6]);
        inverse[1] = (matrix[2] * matrix[6] - matrix[1] * matrix[8]);
        inverse[4] = (matrix[0] * matrix[8] - matrix[2] * matrix[6]);
        inverse[7] = (matrix[6] * matrix[1] - matrix[0] * matrix[7]);
        inverse[2] = (matrix[1] * matrix[5] - matrix[2] * matrix[4]);
        inverse[5] = (matrix[2] * matrix[3] - matrix[0] * matrix[5]);
        inverse[8] = (matrix[0] * matrix[4] - matrix[1] * matrix[3]);
        double det = matrix[0] * inverse[0] + matrix[1] * inverse[3] + matrix[2] * inverse[6];
        for (int i = 0; i < 9; i += 3) {
            inverse[i] /= det;
            inverse[i + 1] /= det;
            inverse[i + 2] /= det;
        }
        return inverse;
    }

    public static double[] inverse(double[] matrix) {
        double[] inverse = new double[16];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                inverse[(i * 4 + j)] = matrix[(i + j * 4)];
            }
            inverse[(i * 4 + 3)] = (-inverse[(i * 4)] * matrix[3] - inverse[(i * 4 + 1)] * matrix[7]
                    - inverse[(i * 4 + 2)] * matrix[11]);
        }
        inverse[12] = 0.0D;
        inverse[13] = 0.0D;
        inverse[14] = 0.0D;
        inverse[15] = 1.0D;
        return inverse;
    }

    public static Vec3d doRotationOnly(double[] transform, Vec3d vanilla) {
        Vector converted = new Vector(vanilla);
        doRotationOnly(transform, converted);
        return converted.toVec3d();
    }

}