package ValkyrienWarfareBase.API;

import net.minecraft.entity.Entity;

public class DummyMethods {

	public Vector getLinearVelocity(Entity shipEnt,double secondsToApply){
		return new Vector();
	}
	
	public Vector getAngularVelocity(Entity shipEnt){
		return new Vector();
	}
	
	//Returns the matrix which converts local coordinates (The positions of the blocks in the world) to the entity coordinates (The position in front of the player)
	public double[] getShipTransformMatrix(Entity shipEnt){
		return RotationMatrices.getDoubleIdentity();
	}
	
	//Note, do not call this from World coordinates; first subtract the world coords from the shipEntity xyz and then call!
	public Vector getVelocityAtPoint(Entity shipEnt,Vector inBody,double secondsToApply){
		return new Vector();
	}
	
	public double getShipMass(Entity shipEnt){
		return 420;
	}
}
