package ValkyrienWarfareBase.Collision;

import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.Math.BigBastardMath;

public class PhysCollisionObject {

	public final Vector axis;
	public double penetrationDistance;
	public final Polygon movable, fixed;
	public boolean seperated;
	public double[] playerMinMax;
	public double[] blockMinMax;
	public double movMaxFixMin;
	public double movMinFixMax;
	public Vector firstContactPoint;

	public PhysCollisionObject(Polygon movable_, Polygon stationary, Vector axes) {
		axis = axes;
		movable = movable_;
		fixed = stationary;
		generateCollision();
	}

	public void generateCollision() {
		playerMinMax = BigBastardMath.getMinMaxOfArray(movable.getProjectionOnVector(axis));
		blockMinMax = BigBastardMath.getMinMaxOfArray(fixed.getProjectionOnVector(axis));
		movMaxFixMin = playerMinMax[0] - blockMinMax[1];
		movMinFixMax = playerMinMax[1] - blockMinMax[0];
		if (movMaxFixMin > 0 || movMinFixMax < 0) {
			seperated = true;
			penetrationDistance = 0.0D;
			return;
		}
		// Set the penetration to be the smaller distance
		if (Math.abs(movMaxFixMin) > Math.abs(movMinFixMax)) {
			penetrationDistance = movMinFixMax;
			for (Vector v : movable.vertices) {
				if (v.dot(axis) == playerMinMax[1]) {
					firstContactPoint = v;
				}
			}
		} else {
			penetrationDistance = movMaxFixMin;
			for (Vector v : movable.vertices) {
				if (v.dot(axis) == playerMinMax[0]) {
					firstContactPoint = v;
				}
			}
		}
		seperated = false;
	}

	public Vector getSecondContactPoint() {
		if (Math.abs(movMaxFixMin) > Math.abs(movMinFixMax)) {
			for (Vector v : fixed.vertices) {
				if (v.dot(axis) == blockMinMax[0]) {
					return v;
				}
			}
		} else {
			for (Vector v : fixed.vertices) {
				if (v.dot(axis) == blockMinMax[1]) {
					return v;
				}
			}
		}
		return null;
	}

	public Vector getResponse() {
		return axis.getProduct(penetrationDistance);
	}

	public void setResponse(Vector v) {
		v.X = axis.X * penetrationDistance;
		v.Y = axis.Y * penetrationDistance;
		v.Z = axis.Z * penetrationDistance;
	}
}