package ValkyrienWarfareBase.Math;

/**
 * Used in rendering interpolation and in applying Angular Velocity in the Physics Controller
 * 
 * @author thebest108
 *
 */
public class Quaternion {

	public double x, y, z, w;

	public Quaternion(double xx, double yy, double zz, double ww) {
		x = xx;
		y = yy;
		z = zz;
		w = ww;
	}

	private Quaternion() {
	}

	public static Quaternion QuaternionFromMatrix(final double[] matrix) {
		Quaternion q = new Quaternion();
		q.w = Math.sqrt(Math.max(0, 1 + matrix[0] + matrix[5] + matrix[10])) / 2;
		q.x = Math.sqrt(Math.max(0, 1 + matrix[0] - matrix[5] - matrix[10])) / 2;
		q.y = Math.sqrt(Math.max(0, 1 - matrix[0] + matrix[5] - matrix[10])) / 2;
		q.z = Math.sqrt(Math.max(0, 1 - matrix[0] - matrix[5] + matrix[10])) / 2;
		q.x *= Math.signum(q.x * (matrix[6] - matrix[9]));
		q.y *= Math.signum(q.y * (matrix[8] - matrix[2]));
		q.z *= Math.signum(q.z * (matrix[1] - matrix[4]));
		return q;
	}

	public static Quaternion getBetweenQuat(Quaternion old, Quaternion newOne, double timeStep) {
		double dotProduct = dotProduct(old, newOne);
		boolean makeNegative = dotProduct < 0;
		if (makeNegative) {
			old.x *= -1;
			old.y *= -1;
			old.z *= -1;
			old.w *= -1;
			dotProduct *= -1;
		}
		double betweenAngle = Math.acos(dotProduct);
		double sinMod = Math.sin((float) betweenAngle);
		double oldMod = 1.0D - timeStep;
		double newMod = timeStep;
		if (Math.abs(sinMod) > 0) {
			oldMod = Math.sin(oldMod * betweenAngle) / sinMod;
			newMod = Math.sin(timeStep * betweenAngle) / sinMod;
		}
		Quaternion betweenQuat = new Quaternion(old.x * oldMod + newOne.x * newMod, old.y * oldMod + newOne.y * newMod, old.z * oldMod + newOne.z * newMod, old.w * oldMod + newOne.w * newMod);
		double betweenLength = Math.sqrt(betweenQuat.x * betweenQuat.x + betweenQuat.y * betweenQuat.y + betweenQuat.z * betweenQuat.z + betweenQuat.w * betweenQuat.w);
		betweenQuat.x /= betweenLength;
		betweenQuat.y /= betweenLength;
		betweenQuat.z /= betweenLength;
		betweenQuat.w /= betweenLength;
		if (makeNegative) {
			old.x *= -1;
			old.y *= -1;
			old.z *= -1;
			old.w *= -1;
		}
		return betweenQuat;
	}

	public static Quaternion fromAxisAngle(double x, double y, double z, double angle) {
		double axismag = Math.sqrt(x * x + y * y + z * z);
		x /= axismag;
		y /= axismag;
		z /= axismag;
		Quaternion q = new Quaternion();
		double sinMod = Math.sin(angle / 2D);
		q.x = x * sinMod;
		q.y = y * sinMod;
		q.z = z * sinMod;
		q.w = Math.cos(angle / 2D);
		return q;
	}

	public static Quaternion fromEuler(double pitch, double yaw, double roll) {
		double heading = Math.toRadians(pitch);
		double attitude = Math.toRadians(yaw);
		double bank = Math.toRadians(roll);
		double c1 = Math.cos(heading / 2);
		double s1 = Math.sin(heading / 2);
		double c2 = Math.cos(attitude / 2);
		double s2 = Math.sin(attitude / 2);
		double c3 = Math.cos(bank / 2);
		double s3 = Math.sin(bank / 2);
		double c1c2 = c1 * c2;
		double s1s2 = s1 * s2;
		Quaternion quat = new Quaternion();
		quat.x = s1 * c2 * c3 + c1 * s2 * s3;
		quat.y = c1 * s2 * c3 - s1 * c2 * s3;
		quat.z = c1c2 * s3 + s1s2 * c3;
		quat.w = c1c2 * c3 - s1s2 * s3;
		return quat;
	}

	// MicroOptimization; avoid creating an extra double
	public double[] toRadians() {
		// double test = x*y + z*w;
		double sqw = w * w;
		double sqx = x * x;
		double sqy = y * y;
		double sqz = z * z;
		double pitch = -Math.atan2(2.0 * (y * z + x * w), (-sqx - sqy + sqz + sqw));
		double yaw = -Math.asin(-2.0 * (x * z - y * w) / (sqx + sqy + sqz + sqw));
		double roll = -Math.atan2(2.0 * (x * y + z * w), (sqx - sqy - sqz + sqw));
		sqw = x * y + z * w;
		if (sqw > .9) {
			System.out.println("Quaternion singularity at North Pole");
			roll = 2 * Math.atan2(x, w);
			yaw = Math.PI / 2;
			pitch = 0;
		}
		if (sqw < -.9) {
			System.out.println("Quaternion singularity at South Pole");
			roll = -2 * Math.atan2(x, w);
			yaw = -Math.PI / 2;
			pitch = 0;
		}
		return new double[] { pitch, yaw, roll };
	}

	public void multiply(Quaternion q1) {
		double oldw = w;
		double oldx = x;
		double oldy = y;
		double oldz = z;
		w = oldw * q1.w - oldx * q1.x - oldy * q1.y - oldz * q1.z;
		x = oldw * q1.x + q1.w * oldx + oldy * q1.z - oldz * q1.y;
		y = oldw * q1.y + q1.w * oldy - oldx * q1.z + oldz * q1.x;
		z = oldw * q1.z + q1.w * oldz + oldx * q1.y - oldy * q1.x;
		oldw = x * x + y * y + z * z + w * w;
		if (Math.abs(1D - oldw) > .0000000000000001) {
			oldw = Math.sqrt(oldw);
			w /= oldw;
			x /= oldw;
			y /= oldw;
			z /= oldw;
		}
	}

	public static double dotProduct(Quaternion first, Quaternion second) {
		return (first.x * second.x) + (first.y * second.y) + (first.z * second.z) + (first.w * second.w);
	}

}