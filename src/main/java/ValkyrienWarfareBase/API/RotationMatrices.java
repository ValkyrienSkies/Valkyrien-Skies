package ValkyrienWarfareBase.API;

import ValkyrienWarfareBase.Math.BigBastardMath;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * This class creates and processes rotation matrix transforms used by Valkyrien Warfare
 * 
 * @author thebest108
 *
 */
public class RotationMatrices {

	public static final float[] transpose(float[] matrix) {
		float[] transpose = new float[16];
		transpose[0] = matrix[0];
		transpose[1] = matrix[4];
		transpose[2] = matrix[8];
		transpose[3] = matrix[12];
		transpose[4] = matrix[1];
		transpose[5] = matrix[5];
		transpose[6] = matrix[9];
		transpose[7] = matrix[13];
		transpose[8] = matrix[2];
		transpose[9] = matrix[6];
		transpose[10] = matrix[10];
		transpose[11] = matrix[14];
		transpose[12] = matrix[3];
		transpose[13] = matrix[7];
		transpose[14] = matrix[11];
		transpose[15] = matrix[15];
		return transpose;
	}

	public static final double[] getTranslationMatrix(double x, double y, double z) {
		double[] matrix = getDoubleIdentity();
		matrix[3] = x;
		matrix[7] = y;
		matrix[11] = z;
		return matrix;
	}

	public static final double[] rotateAndTranslate(double[] input, double pitch, double yaw, double roll, Vector localOrigin) {
		input = RotationMatrices.getMatrixProduct(input, RotationMatrices.getRotationMatrix(1.0D, 0.0D, 0.0D, Math.toRadians(pitch)));
		input = RotationMatrices.getMatrixProduct(input, RotationMatrices.getRotationMatrix(0.0D, 1.0D, 0.0D, Math.toRadians(yaw)));
		input = RotationMatrices.getMatrixProduct(input, RotationMatrices.getRotationMatrix(0.0D, 0.0D, 1.0D, Math.toRadians(roll)));
		input = RotationMatrices.getMatrixProduct(input, RotationMatrices.getTranslationMatrix(-localOrigin.X, -localOrigin.Y, -localOrigin.Z));
		return input;
	}

	public static final double[] rotateOnly(double[] input, double pitch, double yaw, double roll) {
		input = RotationMatrices.getMatrixProduct(input, RotationMatrices.getRotationMatrix(1.0D, 0.0D, 0.0D, Math.toRadians(pitch)));
		input = RotationMatrices.getMatrixProduct(input, RotationMatrices.getRotationMatrix(0.0D, 1.0D, 0.0D, Math.toRadians(yaw)));
		input = RotationMatrices.getMatrixProduct(input, RotationMatrices.getRotationMatrix(0.0D, 0.0D, 1.0D, Math.toRadians(roll)));
		return input;
	}

	public static final double[] getRotationMatrix(double pitch, double yaw, double roll) {
		double[] input = RotationMatrices.getRotationMatrix(1.0D, 0.0D, 0.0D, Math.toRadians(pitch));
		input = RotationMatrices.getMatrixProduct(input, RotationMatrices.getRotationMatrix(0.0D, 1.0D, 0.0D, Math.toRadians(yaw)));
		input = RotationMatrices.getMatrixProduct(input, RotationMatrices.getRotationMatrix(0.0D, 0.0D, 1.0D, Math.toRadians(roll)));
		return input;
	}

	public static final double[] getRotationMatrix(double ux, double uy, double uz, double angle) {
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

	public static final double[] getDoubleIdentity() {
		return new double[] { 1.0D, 0, 0, 0, 0, 1.0D, 0, 0, 0, 0, 1.0D, 0, 0, 0, 0, 1.0D };
	}

	public static final double[] getDoubleIdentity(int size) {
		double[] identity = new double[size * size];
		for (int i = 0; i < identity.length; i += size + 1) {
			identity[i] = 1.0D;
			for (int j = i + 1; (j < i + size + 1) && (j < identity.length); j++) {
				identity[j] = 0.0D;
			}
		}
		return identity;
	}

	public static final double[] getZeroMatrix(int size) {
		double[] zero = new double[size * size];
		for (int i = 0; i < zero.length; i++) {
			zero[i] = 0.0D;
		}
		return zero;
	}

	public static final double[] getMatrixProduct(double[] M1, double[] M2) {
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

	public static final void applyTransform(double[] M, Vector vec) {
		double x = vec.X;
		double y = vec.Y;
		double z = vec.Z;
		vec.X = x * M[0] + y * M[1] + z * M[2] + M[3];
		vec.Y = x * M[4] + y * M[5] + z * M[6] + M[7];
		vec.Z = x * M[8] + y * M[9] + z * M[10] + M[11];
	}

	public static final void applyTransform(double[] wholeTransform, double[] rotationTransform, Entity ent) {
		Vector entityPos = new Vector(ent.posX, ent.posY, ent.posZ);
		Vector entityLook = new Vector(ent.getLook(1.0F));
		Vector entityMotion = new Vector(ent.motionX, ent.motionY, ent.motionZ);

		if (ent instanceof EntityFireball) {
			EntityFireball ball = (EntityFireball) ent;
			entityMotion.X = ball.accelerationX;
			entityMotion.Y = ball.accelerationY;
			entityMotion.Z = ball.accelerationZ;
		}

		applyTransform(wholeTransform, entityPos);
		doRotationOnly(rotationTransform, entityLook);
		doRotationOnly(rotationTransform, entityMotion);

		entityLook.normalize();

		// This is correct
		ent.rotationPitch = (float) MathHelper.wrapDegrees(BigBastardMath.getPitchFromVec3d(entityLook));
		ent.prevRotationPitch = ent.rotationPitch;

		ent.rotationYaw = (float) MathHelper.wrapDegrees(BigBastardMath.getYawFromVec3d(entityLook, ent.rotationPitch));
		ent.prevRotationYaw = ent.rotationYaw;

		if (ent instanceof EntityLiving) {
			EntityLiving living = (EntityLiving) ent;
			living.rotationYawHead = ent.rotationYaw;
			living.prevRotationYawHead = ent.rotationYaw;
		}

		if (ent instanceof EntityFireball) {
			EntityFireball ball = (EntityFireball) ent;
			ball.accelerationX = entityMotion.X;
			ball.accelerationY = entityMotion.Y;
			ball.accelerationZ = entityMotion.Z;
		}

		ent.motionX = entityMotion.X;
		ent.motionY = entityMotion.Y;
		ent.motionZ = entityMotion.Z;

		ent.setPosition(entityPos.X, entityPos.Y, entityPos.Z);
	}

	public static final BlockPos applyTransform(double[] M, BlockPos pos) {
		Vector blockPosVec = new Vector(pos.getX() + .5D, pos.getY() + .5D, pos.getZ() + .5D);
		applyTransform(M, blockPosVec);
		BlockPos newPos = new BlockPos(Math.round(blockPosVec.X - .5D), Math.round(blockPosVec.Y - .5D), Math.round(blockPosVec.Z - .5D));
		return newPos;
	}

	public static final Vec3d applyTransform(double[] M, Vec3d vec) {
		double x = vec.xCoord;
		double y = vec.yCoord;
		double z = vec.zCoord;
		return new Vec3d((x * M[0] + y * M[1] + z * M[2] + M[3]), (x * M[4] + y * M[5] + z * M[6] + M[7]), (x * M[8] + y * M[9] + z * M[10] + M[11]));
	}

	public static final void applyTransform3by3(double[] M, Vector vec) {
		double xx = vec.X;
		double yy = vec.Y;
		double zz = vec.Z;
		vec.X = (xx * M[0] + yy * M[1] + zz * M[2]);
		vec.Y = (xx * M[3] + yy * M[4] + zz * M[5]);
		vec.Z = (xx * M[6] + yy * M[7] + zz * M[8]);
	}

	public static final void doRotationOnly(double[] M, Vector vec) {
		double x = vec.X;
		double y = vec.Y;
		double z = vec.Z;
		vec.X = x * M[0] + y * M[1] + z * M[2];
		vec.Y = x * M[4] + y * M[5] + z * M[6];
		vec.Z = x * M[8] + y * M[9] + z * M[10];
	}

	public static final float[] convertToFloat(double[] array) {
		float[] floatArray = new float[array.length];
		for (int i = 0; i < array.length; i++) {
			floatArray[i] = (float) array[i];
		}
		return floatArray;
	}

	public static final Vector get3by3TransformedVec(double[] M, Vector v) {
		Vector vec = new Vector(v);
		applyTransform3by3(M, vec);
		return vec;
	}

	public static final Vector getTransformedVec(double[] M, Vector v) {
		Vector vec = new Vector(v);
		applyTransform(M, vec);
		return vec;
	}

	public static final double[] inverse3by3(double[] matrix) {
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
		for (int i = 0; i < 9; i++) {
			inverse[i] /= det;
		}
		return inverse;
	}

	public static final double[] inverse(double[] matrix) {
		double[] inverse = new double[16];
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				inverse[(i * 4 + j)] = matrix[(i + j * 4)];
			}
			inverse[(i * 4 + 3)] = (-inverse[(i * 4)] * matrix[3] - inverse[(i * 4 + 1)] * matrix[7] - inverse[(i * 4 + 2)] * matrix[11]);
		}
		inverse[12] = 0.0D;
		inverse[13] = 0.0D;
		inverse[14] = 0.0D;
		inverse[15] = 1.0D;
		return inverse;
	}

}