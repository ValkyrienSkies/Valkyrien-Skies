package ValkyrienWarfareBase.PhysCollision;

import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.Collision.PhysCollisionObject;
import ValkyrienWarfareBase.Collision.PhysPolygonCollider;

public class PolygonCollisionPointFinder {

	//TODO: This algorithm isn't correct, fix it later on!
	public static Vector[] getPointsOfCollisionForPolygons(PhysPolygonCollider collider, PhysCollisionObject toCollideWith, Vector velocity){

//		Vector axis = new Vector(toCollideWith.axis);

		Vector axis = collider.collisions[collider.minDistanceIndex].axis;

		double reverseVelocityAlongAxis = -velocity.dot(axis);

		double minDot = 9999999999999D;
		int minIndex = 0;

		for(int i = 0; i < 8; i++){
			Vector vertice = toCollideWith.movable.vertices[i];
			double dot = vertice.dot(axis) * reverseVelocityAlongAxis;
			if(dot < minDot){
				minDot = dot;
				minIndex = i;
			}
		}

		Vector contactPoint = toCollideWith.movable.vertices[minIndex];

		double secondsAgo = 69;

		if(Math.signum(reverseVelocityAlongAxis) == 1.0D){
			secondsAgo = toCollideWith.movMinFixMax / reverseVelocityAlongAxis;
		}else{
			secondsAgo = toCollideWith.movMaxFixMin / reverseVelocityAlongAxis;
		}

//		System.out.println(contactPoint);

//		contactPoint = contactPoint.getAddition(axis.getProduct(reverseVelocityAlongAxis * secondsAgo));

		return new Vector[]{
				contactPoint
//				toCollideWith.firstContactPoint,
//				toCollideWith.getSecondContactPoint()
		};
	}


}
