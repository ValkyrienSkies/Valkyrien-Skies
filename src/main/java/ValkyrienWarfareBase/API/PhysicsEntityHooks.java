package ValkyrienWarfareBase.API;

import net.minecraft.entity.Entity;

public class PhysicsEntityHooks {

	//Replaced with an object that does real work in runtime
	public static DummyMethods methods;
	
	public static Vector getLinearVelocity(Entity shipEnt,double secondsToApply){
		return methods.getLinearVelocity(shipEnt, secondsToApply);
	}
	
	public static Vector getAngularVelocity(Entity shipEnt){
		return methods.getAngularVelocity(shipEnt);
	}
	
	//Returns the matrix which converts local coordinates (The positions of the blocks in the world) to the entity coordinates (The position in front of the player)
	public static double[] getShipTransformMatrix(Entity shipEnt){
		return methods.getShipTransformMatrix(shipEnt);
	}
	
	//Note, do not call this from World coordinates; first subtract the world coords from the shipEntity xyz and then call!
	public static Vector getVelocityAtPoint(Entity shipEnt,Vector inBody,double secondsToApply){
		return methods.getVelocityAtPoint(shipEnt, inBody, secondsToApply);
	}
	
	public static double getShipMass(Entity shipEnt){
		return methods.getShipMass(shipEnt);
	}
}
