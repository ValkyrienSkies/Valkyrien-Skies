package ValkyrienWarfareBase.Collision;

import ValkyrienWarfareBase.API.Vector;

public class PhysPolygonCollider {

	public Vector[] potentialSeperatingAxes = null;
	public boolean seperated = false;
	public PhysCollisionObject[] collisions = null;
	public int minDistanceIndex;
	public double minDistance;
	public Polygon entity;
	public Polygon block;

	public PhysPolygonCollider(Polygon movable, Polygon stationary, Vector[] axes) {
		potentialSeperatingAxes = axes;
		entity = movable;
		block = stationary;
		processData();
	}

	// TODO: Fix this, processes the penetration distances backwards from their reality
	public void processData() {
		collisions = new PhysCollisionObject[potentialSeperatingAxes.length];
		for (int i = 0; i < potentialSeperatingAxes.length && !seperated; i++) {
			collisions[i] = new PhysCollisionObject(entity, block, potentialSeperatingAxes[i]);
			seperated = collisions[i].seperated;
		}
		if (!seperated) {
			minDistance = 420;
			for (int i = 0; i < potentialSeperatingAxes.length; i++) {
				// Take the collision response closest to 0
				if (Math.abs(collisions[i].penetrationDistance) < minDistance) {
					minDistanceIndex = i;
					minDistance = Math.abs(collisions[i].penetrationDistance);
				}
			}
		}
	}

}