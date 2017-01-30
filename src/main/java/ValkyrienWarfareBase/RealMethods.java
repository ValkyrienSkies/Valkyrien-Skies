package ValkyrienWarfareBase;

import ValkyrienWarfareBase.API.DummyMethods;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RealMethods implements DummyMethods {

	@Override
	public Vector getLinearVelocity(Entity shipEnt, double secondsToApply) {
		PhysicsWrapperEntity wrapper = (PhysicsWrapperEntity) shipEnt;
		return wrapper.wrapping.physicsProcessor.linearMomentum.getProduct(secondsToApply * wrapper.wrapping.physicsProcessor.invMass);
	}

	@Override
	public Vector getAngularVelocity(Entity shipEnt) {
		PhysicsWrapperEntity wrapper = (PhysicsWrapperEntity) shipEnt;
		return wrapper.wrapping.physicsProcessor.angularVelocity;
	}

	// Returns the matrix which converts local coordinates (The positions of the blocks in the world) to the entity coordinates (The position in front of the player)
	@Override
	public double[] getShipTransformMatrix(Entity shipEnt) {
		PhysicsWrapperEntity wrapper = (PhysicsWrapperEntity) shipEnt;
		return wrapper.wrapping.coordTransform.lToWTransform;
	}

	// Note, do not call this from World coordinates; first subtract the world coords from the shipEntity xyz and then call!
	@Override
	public Vector getVelocityAtPoint(Entity shipEnt, Vector inBody, double secondsToApply) {
		PhysicsWrapperEntity wrapper = (PhysicsWrapperEntity) shipEnt;
		Vector toReturn = wrapper.wrapping.physicsProcessor.getMomentumAtPoint(inBody);
		toReturn.multiply(secondsToApply);
		return toReturn;
	}

	@Override
	public double getShipMass(Entity shipEnt) {
		PhysicsWrapperEntity wrapper = (PhysicsWrapperEntity) shipEnt;
		return wrapper.wrapping.physicsProcessor.mass;
	}

	@Override
	public Vector getPositionInShipFromReal(World worldObj, Entity shipEnt, Vector positionInWorld) {
		PhysicsWrapperEntity wrapper = (PhysicsWrapperEntity) shipEnt;
		Vector inLocal = new Vector(positionInWorld);
		wrapper.wrapping.coordTransform.fromLocalToGlobal(inLocal);
		return inLocal;
	}

	@Override
	public Vector getPositionInRealFromShip(World worldObj, Entity shipEnt, Vector pos) {
		PhysicsWrapperEntity wrapper = (PhysicsWrapperEntity) shipEnt;
		Vector inReal = new Vector(pos);
		wrapper.wrapping.coordTransform.fromLocalToGlobal(inReal);
		return inReal;
	}

	@Override
	public boolean isBlockPartOfShip(World worldObj, BlockPos pos) {
		return getShipEntityManagingPos(worldObj, pos) != null;
	}

	@Override
	public PhysicsWrapperEntity getShipEntityManagingPos(World worldObj, BlockPos pos) {
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(worldObj, pos);
		return wrapper;
	}

	@Override
	public Vector getShipCenterOfMass(Entity shipEnt) {
		return new Vector(((PhysicsWrapperEntity) shipEnt).wrapping.physicsProcessor.centerOfMass);
	}

	@Override
	public boolean isEntityAShip(Entity entityToTest) {
		return entityToTest instanceof PhysicsWrapperEntity;
	}
}
