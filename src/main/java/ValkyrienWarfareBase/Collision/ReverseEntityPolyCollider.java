package ValkyrienWarfareBase.Collision;

import ValkyrienWarfareBase.API.Vector;

/**
 * Heavily modified version of the original Polygon Collider, with checks on polygons passing through eachother, normals not to be considered, and a consideration for Net Velocity between the polygons
 * 
 * @author thebest108
 *
 */
public class ReverseEntityPolyCollider {

	public Vector[] potentialSeperatingAxes;
	public boolean seperated = false;
	public ReverseEntityCollisionObject[] collisions;
	public int minDistanceIndex;
	public double minDistance;
	public Polygon entity;
	public Polygon block;
	public Vector entityVelocity;
	public boolean originallySeperated;
	public int minIndexInReverse;

	public ReverseEntityPolyCollider(Polygon movable, Polygon stationary, Vector[] axes, Vector entityVel) {
		potentialSeperatingAxes = axes;
		entity = movable;
		block = stationary;
		entityVelocity = entityVel;
		processData();
	}

	public void processData() {
		seperated = false;
		collisions = new ReverseEntityCollisionObject[potentialSeperatingAxes.length];
		for (int i = 0; i < collisions.length; i++) {
			if (!seperated) {
				collisions[i] = new ReverseEntityCollisionObject(entity, block, potentialSeperatingAxes[i], entityVelocity);
				if (collisions[i].seperated) {
					seperated = true;
					break;
				}
			}
		}
		if (!seperated) {
			minDistance = 420;
			double minNegativeImpactTime = 0D;
			for (int i = 0; i < collisions.length; i++) {
				// Take the collision response closest to 0
				if (Math.abs(collisions[i].penetrationDistance) < minDistance) {
					minDistanceIndex = i;
					minDistance = Math.abs(collisions[i].penetrationDistance);
				}
				double velDot = collisions[i].axis.dot(entityVelocity);
				double negativeTime = -velDot / collisions[i].penetrationDistance;
				if (negativeTime < minNegativeImpactTime) {
					minNegativeImpactTime = negativeTime;
					minIndexInReverse = i;
				}
			}
			if (entityVelocity.isZero()) {
				minIndexInReverse = minDistanceIndex;
			}
		}
	}

}