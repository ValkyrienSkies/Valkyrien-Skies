package org.valkyrienskies.mod.common.math;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4dc;
import org.joml.Vector3d;
import org.valkyrienskies.mod.common.coordinates.VectorImmutable;

/**
 * This is getting deleted. Do NOT Use!
 *
 * @deprecated Use JOML matrices instead, which have these methods built in
 */
@Deprecated
public class RotationMatrices {

    /**
     * Returns a rotation matrix with the described rotation and no translation.
     *
     * @param pitch in degrees
     * @param yaw   in degrees
     * @param roll  in degrees
     */
    public static double[] getRotationMatrix(double pitch, double yaw, double roll) {
        double[] input = RotationMatrices
                .getRotationMatrix(1.0D, 0.0D, 0.0D, Math.toRadians(pitch));
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

    private static double[] getDoubleIdentity() {
        return new double[]{
            1.0D, 0, 0, 0,
            0, 1.0D, 0, 0,
            0, 0, 1.0D, 0,
            0, 0, 0, 1.0D};
    }

    private static double[] getMatrixProduct(double[] M1, double[] M2) {
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
        double x = vec.x;
        double y = vec.y;
        double z = vec.z;
        vec.x = x * M[0] + y * M[1] + z * M[2] + M[3];
        vec.y = x * M[4] + y * M[5] + z * M[6] + M[7];
        vec.z = x * M[8] + y * M[9] + z * M[10] + M[11];
    }

    /**
     * Bad code.
     *
     * @param transform
     * @param entity
     */
    public static void applyTransform(Matrix4dc transform, Entity entity) {
        // ISubspacedEntity entitySubspaceTracker = (ISubspacedEntity) entity;

        Vec3d entityLookMc = entity.getLook(1.0F);

        Vector3d entityPos = new Vector3d(entity.posX, entity.posY, entity.posZ);
        Vector3d entityLook = new Vector3d(entityLookMc.x, entityLookMc.y, entityLookMc.z);
        Vector3d entityMotion = new Vector3d(entity.motionX, entity.motionY, entity.motionZ);

        if (entity instanceof EntityFireball) {
            EntityFireball ball = (EntityFireball) entity;
            entityMotion.x = ball.accelerationX;
            entityMotion.y = ball.accelerationY;
            entityMotion.z = ball.accelerationZ;
        }

        transform.transformPosition(entityPos);
        transform.transformDirection(entityLook);
        transform.transformDirection(entityMotion);

        entityLook.normalize();

        // This is correct, works properly when tested with cows
        if (entity instanceof EntityLiving) {
            EntityLiving living = (EntityLiving) entity;
            living.rotationYawHead = entity.rotationYaw;
            living.prevRotationYawHead = entity.rotationYaw;
        }

        // ===== Fix change the entity rotation to be proper relative to ship space =====
        VectorImmutable entityLookImmutable = new Vector(entityLook.x, entityLook.y, entityLook.z).toImmutable();
        double pitch = VSMath.getPitchFromVectorImmutable(entityLookImmutable);
        double yaw = VSMath.getYawFromVectorImmutable(entityLookImmutable, pitch);

        entity.rotationYaw = (float) yaw;
        entity.rotationPitch = (float) pitch;

        if (entity instanceof EntityFireball) {
            EntityFireball ball = (EntityFireball) entity;
            ball.accelerationX = entityMotion.x;
            ball.accelerationY = entityMotion.y;
            ball.accelerationZ = entityMotion.z;
        }

        entity.motionX = entityMotion.x;
        entity.motionY = entityMotion.y;
        entity.motionZ = entityMotion.z;

        entity.setPosition(entityPos.x, entityPos.y, entityPos.z);
    }

}