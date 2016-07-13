package ValkyrienWarfareBase.Collision;

import ValkyrienWarfareBase.Math.Vector;

public class PhysPolygonCollider{

	public Vector[] potentialSeperatingAxes;
	public boolean seperated = false;
	public PhysCollisionObject[] collisions;
	public int minDistanceIndex;
	public double minDistance;
	public Polygon entity;
	public Polygon block;
	public Vector entityVelocity;
	public boolean originallySeperated;

	public PhysPolygonCollider(Polygon movable,Polygon stationary,Vector[] axes,Vector entityVel){
		potentialSeperatingAxes = axes;
		entity=movable;
		block=stationary;
		entityVelocity=entityVel;
		processData();
	}

	public void processData(){
		seperated = false;
		collisions = new PhysCollisionObject[potentialSeperatingAxes.length];
		for(int i=0;i<collisions.length;i++){
			if(!seperated){
				collisions[i] = new PhysCollisionObject(entity,block,potentialSeperatingAxes[i],entityVelocity);
				if(collisions[i].seperated){
					seperated=true;
					break;
				}
				if(!collisions[i].originallyCollided){
					originallySeperated = true;
				}
			}
		}
		if(!seperated){
			minDistance = 420;
			for(int i=0;i<collisions.length;i++){
				if(originallySeperated){
					if(Math.abs((collisions[i].penetrationDistance-collisions[i].velDot)/collisions[i].velDot)<minDistance&&!collisions[i].originallyCollided){
						minDistanceIndex=i;
						minDistance = Math.abs((collisions[i].penetrationDistance-collisions[i].velDot)/collisions[i].velDot);
					}
				}else{
//					System.out.println("wtf happened here");
					if(Math.abs(collisions[i].penetrationDistance)<minDistance){
						minDistanceIndex=i;
						minDistance = Math.abs(collisions[i].penetrationDistance);
					}
				}
			}
		}
	}
	
	public PhysCollisionObject getIdealCollisionObject(){
		double penDist = 420D;
		PhysCollisionObject ideal = null;
		for(PhysCollisionObject object:collisions){
			if(Math.abs(object.penetrationDistance)<penDist){
				penDist = Math.abs(object.penetrationDistance);
				ideal = object;
			}
		}
		return ideal;
	}

}