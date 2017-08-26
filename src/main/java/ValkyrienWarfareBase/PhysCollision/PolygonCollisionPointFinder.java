package ValkyrienWarfareBase.PhysCollision;

import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.Collision.PhysCollisionObject;
import ValkyrienWarfareBase.Collision.PhysPolygonCollider;

public class PolygonCollisionPointFinder {

	//TODO: This algorithm isn't correct, fix it later on!
	public static Vector[] getPointsOfCollisionForPolygons(PhysPolygonCollider collider, PhysCollisionObject somethingElse, Vector velocity) {

//		Vector axis = new Vector(toCollideWith.axis);

		double minSecondsAgo = 69D;
		int minCollisionIndex = 0;
		int minCollisionIndexIndex = 0;

		for (int cont = 0; cont < collider.collisions.length; cont++) {
			PhysCollisionObject toCollideWith = collider.collisions[cont];

			Vector axis = somethingElse.axis;
			double reverseVelocityAlongAxis = -velocity.dot(axis);
			double minDot = 9999999999999D;
			int minIndex = 0;

			for (int i = 0; i < 8; i++) {
				Vector vertice = toCollideWith.movable.vertices[i];
				double dot = vertice.dot(axis) * reverseVelocityAlongAxis;
				if (dot < minDot) {
					minDot = dot;
					minIndex = i;
				}
			}

			Vector contactPoint = toCollideWith.movable.vertices[minIndex];

			double secondsAgo = 69;

			if (Math.signum(reverseVelocityAlongAxis) == 1.0D) {
				secondsAgo = toCollideWith.movMinFixMax / reverseVelocityAlongAxis;
			} else {
				secondsAgo = toCollideWith.movMaxFixMin / reverseVelocityAlongAxis;
			}

			if (secondsAgo < minSecondsAgo) {
				minSecondsAgo = secondsAgo;
				minCollisionIndex = cont;
				minCollisionIndexIndex = minIndex;
			}
		}


//		System.out.println(contactPoint);

		Vector contactPoint = collider.collisions[minCollisionIndex].movable.vertices[minCollisionIndexIndex];//contactPoint.getAddition(axis.getProduct(reverseVelocityAlongAxis * secondsAgo));

		return new Vector[]{
				contactPoint
//				toCollideWith.firstContactPoint,
//				toCollideWith.getSecondContactPoint()
		};
	}


}
