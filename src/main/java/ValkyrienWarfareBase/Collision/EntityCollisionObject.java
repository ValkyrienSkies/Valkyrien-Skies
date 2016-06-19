package ValkyrienWarfareBase.Collision;

import ValkyrienWarfareBase.Math.BigBastardMath;
import ValkyrienWarfareBase.Math.Vector;

/**
 * An enhanced version of the collision Object, designed to prevent entities from moving through
 * a polygon
 * @author thebest108
 *
 */
public class EntityCollisionObject{

	public final Vector axis;
	public double penetrationDistance;
	public final Polygon movable,fixed;
	public boolean seperated;
	public double[] playerMinMax;
	public double[] blockMinMax;
	public Vector entityVelocity;
	public boolean originallyCollided = false;
	public double velDot;

	public EntityCollisionObject(Polygon movable_,Polygon stationary,Vector axes,Vector entityVel){
		axis = axes;
		movable = movable_;
		fixed = stationary;
		entityVelocity = entityVel;
		generateCollision();
	}

	public void generateCollision(){
		velDot = -entityVelocity.dot(axis);
		playerMinMax = BigBastardMath.getMinMaxOfArray(movable.getProjectionOnVector(axis));
		blockMinMax = BigBastardMath.getMinMaxOfArray(fixed.getProjectionOnVector(axis));
		double movMaxFixMin = playerMinMax[0]-blockMinMax[1];
		double movMinFixMax = playerMinMax[1]-blockMinMax[0];
		boolean useDefault = true;
		if(movMaxFixMin>0||movMinFixMax<0){
			//Original position not colliding, use velocity based bastards
			useDefault = false;
		}else{
			originallyCollided = true;
		}
		if(velDot>0){
			movMaxFixMin-=velDot;
		}else{
			movMinFixMax-=velDot;
		}
		if(movMaxFixMin>0||movMinFixMax<0){
			seperated = true;
			penetrationDistance=0.0D;
			return;
		}
		//Set the penetration to be the smaller distance
		if(useDefault||velDot==0D){
			if(Math.abs(movMaxFixMin)<Math.abs(movMinFixMax)){
				penetrationDistance = movMaxFixMin;
			}else{
				penetrationDistance = movMinFixMax;
			}
		}else{
			if(Math.signum(velDot)!=Math.signum(movMinFixMax)){
				penetrationDistance = movMinFixMax;
			}else{
				penetrationDistance = movMaxFixMin;
			}
		}
		seperated= false;
	}

	public Vector getResponse(){
		return axis.getProduct(-penetrationDistance);
	}
}