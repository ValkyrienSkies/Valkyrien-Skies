package ValkyrienWarfareBase.Collision;

import ValkyrienWarfareBase.API.Vector;

/**
 * Heavily modified version of the original Polygon Collider, with checks on polygons passing through eachother, normals not to be considered, and a consideration for Net Velocity between the polygons
 * 
 * @author thebest108
 *
 */
public class EntityPolygonCollider {

	public Vector[] potentialSeperatingAxes;
	public boolean seperated = false;
	public EntityCollisionObject[] collisions;
	public int minDistanceIndex;
	public double minDistance;
	public EntityPolygon entity;
	public Polygon block;
	public Vector entityVelocity;
	public boolean originallySeperated;

	public EntityPolygonCollider(EntityPolygon movable, Polygon stationary, Vector[] axes, Vector entityVel) {
		potentialSeperatingAxes = axes;
		entity = movable;
		block = stationary;
		entityVelocity = entityVel;
		processData();
	}

	public void processData() {
		seperated = false;
		collisions = new EntityCollisionObject[potentialSeperatingAxes.length];
		for (int i = 0; i < collisions.length; i++) {
			if (!seperated) {
				collisions[i] = new EntityCollisionObject(entity, block, potentialSeperatingAxes[i], entityVelocity);
				if (collisions[i].seperated) {
					seperated = true;
					break;
				}
				if (!collisions[i].originallyCollided) {
					originallySeperated = true;
				}
			}
		}
		if (!seperated) {
			minDistance = 420;
			for (int i = 0; i < collisions.length; i++) {
				if (originallySeperated) {
					if (Math.abs((collisions[i].penetrationDistance - collisions[i].velDot) / collisions[i].velDot) < minDistance && !collisions[i].originallyCollided) {
						minDistanceIndex = i;
						minDistance = Math.abs((collisions[i].penetrationDistance - collisions[i].velDot) / collisions[i].velDot);
					}
				} else {
					// System.out.println("wtf happened here");
					if (Math.abs(collisions[i].penetrationDistance) < minDistance) {
						minDistanceIndex = i;
						minDistance = Math.abs(collisions[i].penetrationDistance);
					}
				}
			}
		}
	}

}