package valkyrienwarfare.api;

import valkyrienwarfare.physicsmanagement.PhysicsWrapperEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface DummyMethods {
	// DO NOT RUN METHODS FROM HERE! USE PhysicsEntityHooks
	
	Vector getShipCenterOfMass(Entity shipEnt);
	
	boolean isEntityAShip(Entity entityToTest);
	
	Vector getPositionInShipFromReal(World worldObj, Entity shipEnt, Vector positionInWorld);
	
	Vector getPositionInRealFromShip(World worldObj, Entity shipEnt, Vector posInShip);
	
	boolean isBlockPartOfShip(World worldObj, BlockPos pos);
	
	PhysicsWrapperEntity getShipEntityManagingPos(World worldObj, BlockPos pos);
	
	Vector getLinearVelocity(Entity shipEnt, double secondsToApply);
	
	Vector getAngularVelocity(Entity shipEnt);
	
	// Returns the matrix which converts local coordinates (The positions of the blocks in the world) to the entity coordinates (The position in front of the player)
	double[] getShipTransformMatrix(Entity shipEnt);
	
	// Note, do not call this from World coordinates; first subtract the world coords from the shipEntity xyz and then call!
	Vector getVelocityAtPoint(Entity shipEnt, Vector inBody, double secondsToApply);
	
	double getShipMass(Entity shipEnt);
}
