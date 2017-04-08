package ValkyrienWarfareBase.PhysCollision;

import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.Collision.PhysCollisionObject;
import ValkyrienWarfareBase.Collision.PhysPolygonCollider;

public class PolygonCollisionPointFinder {

	public static Vector[] getPointsOfCollisionForPolygons(PhysPolygonCollider collider, PhysCollisionObject toCollideWith, Vector velocity){
		return new Vector[]{
				toCollideWith.firstContactPoint,
				toCollideWith.getSecondContactPoint()
		};
	}


}
