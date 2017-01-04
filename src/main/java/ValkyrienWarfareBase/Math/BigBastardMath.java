package ValkyrienWarfareBase.Math;

import java.util.Arrays;
import java.util.List;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * A lot of useful math functions belong here
 * 
 * @author thebest108
 *
 */
public class BigBastardMath {

	public static final int maxPasses = 5;
	public static final int[] primes = { 3, 31, 19, 2, 5, 7, 11, 13, 17, 23, 29 };

	public static double getPitchFromVec3d(Vector vec) {
		double pitchFromRotVec = -Math.asin(vec.Y) / 0.017453292F;
		return pitchFromRotVec;
	}

	public static double getYawFromVec3d(Vector vec, double rotPitch) {
		double f2 = -Math.cos(-rotPitch * 0.017453292F);
		double yawFromRotVec = Math.atan2(vec.X / f2, vec.Z / f2);
		yawFromRotVec += Math.PI;
		yawFromRotVec /= -0.017453292F;
		return yawFromRotVec;
	}

	// Assuming they're colliding, OR ELSE!
	public static AxisAlignedBB getBetweenAABB(AxisAlignedBB ship1, AxisAlignedBB ship2) {
		if (!ship1.intersectsWith(ship2)) {
			System.out.println("Tried getting relevent BB's for 2 ships not colliding!!!");
			System.out.println("Fix it faggot");
			return null;
		}
		final double[] xVals = new double[4];
		final double[] yVals = new double[4];
		final double[] zVals = new double[4];
		xVals[0] = ship1.minX;
		xVals[1] = ship1.maxX;
		xVals[2] = ship2.minX;
		xVals[3] = ship2.maxX;
		yVals[0] = ship1.minY;
		yVals[1] = ship1.maxY;
		yVals[2] = ship2.minY;
		yVals[3] = ship2.maxY;
		zVals[0] = ship1.minZ;
		zVals[1] = ship1.maxZ;
		zVals[2] = ship2.minZ;
		zVals[3] = ship2.maxZ;
		Arrays.sort(xVals);
		Arrays.sort(yVals);
		Arrays.sort(zVals);
		return new AxisAlignedBB(xVals[1], yVals[1], zVals[1], xVals[2], yVals[2], zVals[2]);
	}

	// Maybe update to use Arrays.sort() but that could actually be slower because I
	// only need the min and max arranged
	public static double[] getMinMaxOfArray(double[] distances) {
		double[] minMax = new double[2];
		// Min at 0
		// Max at 1
		minMax[0] = minMax[1] = distances[0];
		for (int i = 1; i < distances.length; i++) {
			if (distances[i] < minMax[0]) {
				minMax[0] = distances[i];
			}
			if (distances[i] > minMax[1]) {
				minMax[1] = distances[i];
			}
		}
		return minMax;
	}

	public static Vector getBodyPosWithOrientation(BlockPos pos, Vector centerOfMass, double[] rotationTransform) {
		final Vector inBody = new Vector(pos.getX() + .5D - centerOfMass.X, pos.getY() + .5D - centerOfMass.Y, pos.getZ() + .5D - centerOfMass.Z);
		RotationMatrices.doRotationOnly(rotationTransform, inBody);
		return inBody;
	}

	public static void getBodyPosWithOrientation(BlockPos pos, Vector centerOfMass, double[] rotationTransform, Vector inBody) {
		inBody.X = pos.getX() + .5D - centerOfMass.X;
		inBody.Y = pos.getY() + .5D - centerOfMass.Y;
		inBody.Z = pos.getZ() + .5D - centerOfMass.Z;
		RotationMatrices.doRotationOnly(rotationTransform, inBody);
	}

	public static void getBodyPosWithOrientation(Vector pos, Vector centerOfMass, double[] rotationTransform, Vector inBody) {
		inBody.X = pos.X - centerOfMass.X;
		inBody.Y = pos.Y - centerOfMass.Y;
		inBody.Z = pos.Z - centerOfMass.Z;
		RotationMatrices.doRotationOnly(rotationTransform, inBody);
	}

	public static Vector getBodyPosWithOrientation(Vector pos, Vector centerOfMass, double[] rotationTransform) {
		Vector inBody = new Vector(pos.X - centerOfMass.X, pos.Y - centerOfMass.Y, pos.Z - centerOfMass.Z);
		RotationMatrices.applyTransform(rotationTransform, inBody);
		return inBody;
	}

	public static Vector getInBodyWOFromInWorld(Vector inWorld, Vector centerOfMass, double[] rotationTransform, double[] wToLTrasform) {
		Vector inBody = new Vector(inWorld);
		RotationMatrices.applyTransform(wToLTrasform, inBody);
		inBody.subtract(centerOfMass);
		RotationMatrices.applyTransform(rotationTransform, inBody);
		return inBody;
	}

	public static final void setInBodyWOFromInWorld(final Vector inWorld, final Vector centerOfMass, final double[] rotationTransform, final double[] wToLTrasform, final Vector toSet) {
		toSet.X = inWorld.X;
		toSet.Y = inWorld.Y;
		toSet.Z = inWorld.Z;
		RotationMatrices.applyTransform(wToLTrasform, toSet);
		toSet.X -= centerOfMass.X;
		toSet.Y -= centerOfMass.Y;
		toSet.Z -= centerOfMass.Z;
		RotationMatrices.doRotationOnly(rotationTransform, toSet);
	}

	/**
	 * Used to get a semi-random distribution of numbers. These numbers are then applied to stabilize the physics simulation.
	 * 
	 * @param relativeTo
	 * @return A number relatively prime to the input
	 */
	public static final int getRelativePrime(int relativeTo) {
		if (relativeTo < 2) {
			return 1;
		}
		for (int i : primes) {
			if (relativeTo > i) {
				if (coptime(relativeTo, i)) {
					return i;
				}
			}
		}
		return 1;
	}

	public static final boolean coptime(int u, int v) {
		if (((u | v) & 1) == 0)
			return false;
		while ((u & 1) == 0)
			u >>= 1;
		if (u == 1)
			return true;

		do {
			while ((v & 1) == 0)
				v >>= 1;
			if (v == 1)
				return true;
			if (u > v) {
				int t = v;
				v = u;
				u = t;
			}
			v -= u;
		} while (v != 0);

		return false;
	}

	public static double bezierThatBastard(double x0, double x1, double x2, double x3, double t) {
		double it = 1D - t;
		double beziered = (it * it * it * x0) + (3D * t * it * it * x1) + (3D * t * t * it * x2) + (t * t * t * x3);
		return beziered;
	}

	/*
	 * public static Quaternion bezierThoseQuats(double t,Quaternion... quats){ double qX = bezierThatBastard(quats[0].x,quats[1].x,quats[2].x,quats[3].x,t); double qY = bezierThatBastard(quats[0].y,quats[1].y,quats[2].y,quats[3].y,t); double qZ = bezierThatBastard(quats[0].z,quats[1].z,quats[2].z,quats[3].z,t); double qW = bezierThatBastard(quats[0].w,quats[1].w,quats[2].w,quats[3].w,t); double length = Math.sqrt(qX*qX+qY*qY+qZ*qZ+qW*qW); qX/=length;qY/=length;qZ/=length;qW/=length; return new Quaternion(qX,qY,qZ,qW); }
	 * 
	 * public static double[] bezierThoseAngles(double t,Quaternion... quats){ double[] angles0 = quats[0].toRadians(); double[] angles1 = quats[1].toRadians(); double[] angles2 = quats[2].toRadians(); double[] angles3 = quats[3].toRadians(); double[] angles = new double[3]; angles[0] = bezierThatBastard(angles0[0],angles1[0],angles2[0],angles3[0],t); angles[1] = bezierThatBastard(angles0[1],angles1[1],angles2[1],angles3[1],t); angles[2] = bezierThatBastard(angles0[2],angles1[2],angles2[2],angles3[2],t); return angles; }
	 */
	/**
	 * Prevents sliding when moving on small angles dictated by the tolerance set in the ValkyrianWarfareMod class
	 * 
	 * @param Normalized
	 *            direction Vector
	 * @return true/false
	 */
	public static boolean canStandOnNormal(Vector normal) {
		// if(normal.Y<0){
		// return false;
		// }
		double radius = normal.X * normal.X + normal.Z * normal.Z;
		return radius < ValkyrienWarfareMod.standingTolerance;
	}

	/**
	 * Takes an arrayList of AABB's and merges them into larger AABB's
	 * 
	 * @param bbs
	 * @return
	 */
	public static void mergeAABBList(List<AxisAlignedBB> toFuse) {
		boolean changed = true;
		int passes = 0;
		while (changed && passes < maxPasses) {
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

	public static AxisAlignedBB getFusedBoundingBox(AxisAlignedBB bb1, AxisAlignedBB bb2) {
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

	public static boolean connected(AxisAlignedBB bb1, AxisAlignedBB bb2) {
		return (connectedInX(bb1, bb2) || connectedInY(bb1, bb2) || connectedInZ(bb1, bb2));
	}

	public static boolean connectedInX(AxisAlignedBB bb1, AxisAlignedBB bb2) {
		return (intersectInX(bb1, bb2)) && (areXAligned(bb1, bb2));
	}

	public static boolean connectedInY(AxisAlignedBB bb1, AxisAlignedBB bb2) {
		return (intersectInY(bb1, bb2)) && (areYAligned(bb1, bb2));
	}

	public static boolean connectedInZ(AxisAlignedBB bb1, AxisAlignedBB bb2) {
		return (intersectInZ(bb1, bb2)) && (areZAligned(bb1, bb2));
	}

	public static boolean intersectInX(AxisAlignedBB bb1, AxisAlignedBB bb2) {
		return ((bb1.maxX >= bb2.minX) && (bb1.maxX < bb2.maxX)) || ((bb1.minX > bb2.minX) && (bb1.minX <= bb2.maxX));
	}

	public static boolean intersectInY(AxisAlignedBB bb1, AxisAlignedBB bb2) {
		return ((bb1.maxY >= bb2.minY) && (bb1.maxY < bb2.maxY)) || ((bb1.minY > bb2.minY) && (bb1.minY <= bb2.maxY));
	}

	public static boolean intersectInZ(AxisAlignedBB bb1, AxisAlignedBB bb2) {
		return ((bb1.maxZ >= bb2.minZ) && (bb1.maxZ < bb2.maxZ)) || ((bb1.minZ > bb2.minZ) && (bb1.minZ <= bb2.maxZ));
	}

	public static boolean areXAligned(AxisAlignedBB bb1, AxisAlignedBB bb2) {
		return (bb1.minY == bb2.minY) && (bb1.minZ == bb2.minZ) && (bb1.maxY == bb2.maxY) && (bb1.maxZ == bb2.maxZ);
	}

	public static boolean areYAligned(AxisAlignedBB bb1, AxisAlignedBB bb2) {
		return (bb1.minX == bb2.minX) && (bb1.minZ == bb2.minZ) && (bb1.maxX == bb2.maxX) && (bb1.maxZ == bb2.maxZ);
	}

	public static boolean areZAligned(AxisAlignedBB bb1, AxisAlignedBB bb2) {
		return (bb1.minX == bb2.minX) && (bb1.minY == bb2.minY) && (bb1.maxX == bb2.maxX) && (bb1.maxY == bb2.maxY);
	}

}