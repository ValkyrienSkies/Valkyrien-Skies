package ValkyrienWarfareBase;

import ValkyrienWarfareBase.API.DummyMethods;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import net.minecraft.entity.Entity;

public class RealMethods extends DummyMethods{
	
	@Override
	public Vector getLinearVelocity(Entity shipEnt,double secondsToApply){
		PhysicsWrapperEntity wrapper = (PhysicsWrapperEntity) shipEnt;
		return wrapper.wrapping.physicsProcessor.linearMomentum.getProduct(secondsToApply*wrapper.wrapping.physicsProcessor.invMass);
	}
	
	public Vector getAngularVelocity(Entity shipEnt){
		PhysicsWrapperEntity wrapper = (PhysicsWrapperEntity) shipEnt;
		return wrapper.wrapping.physicsProcessor.angularVelocity;
	}
	
	//Returns the matrix which converts local coordinates (The positions of the blocks in the world) to the entity coordinates (The position in front of the player)
	public double[] getShipTransformMatrix(Entity shipEnt){
		PhysicsWrapperEntity wrapper = (PhysicsWrapperEntity) shipEnt;
		return wrapper.wrapping.coordTransform.lToWTransform;
	}
	
	//Note, do not call this from World coordinates; first subtract the world coords from the shipEntity xyz and then call!
	public Vector getVelocityAtPoint(Entity shipEnt,Vector inBody,double secondsToApply){
		PhysicsWrapperEntity wrapper = (PhysicsWrapperEntity) shipEnt;
		Vector toReturn = wrapper.wrapping.physicsProcessor.getMomentumAtPoint(inBody);
		toReturn.multiply(secondsToApply);
		return toReturn;
	}
	
	public double getShipMass(Entity shipEnt){
		PhysicsWrapperEntity wrapper = (PhysicsWrapperEntity) shipEnt;
		return wrapper.wrapping.physicsProcessor.mass;
	}
}
