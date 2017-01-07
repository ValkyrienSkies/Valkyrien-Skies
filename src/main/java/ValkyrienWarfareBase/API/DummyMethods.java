package ValkyrienWarfareBase.API;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface DummyMethods {
	// DO NOT RUN METHODS FROM HERE! USE PhysicsEntityHooks

	public Vector getShipCenterOfMass(Entity shipEnt);

	public boolean isEntityAShip(Entity entityToTest);

	public Vector getPositionInShipFromReal(World worldObj, Entity shipEnt, Vector positionInWorld);

	public Vector getPositionInRealFromShip(World worldObj, Entity shipEnt, Vector posInShip);

	public boolean isBlockPartOfShip(World worldObj, BlockPos pos);

	public Entity getShipEntityManagingPos(World worldObj, BlockPos pos);

	public Vector getLinearVelocity(Entity shipEnt, double secondsToApply);

	public Vector getAngularVelocity(Entity shipEnt);

	// Returns the matrix which converts local coordinates (The positions of the blocks in the world) to the entity coordinates (The position in front of the player)
	public double[] getShipTransformMatrix(Entity shipEnt);

	// Note, do not call this from World coordinates; first subtract the world coords from the shipEntity xyz and then call!
	public Vector getVelocityAtPoint(Entity shipEnt, Vector inBody, double secondsToApply);

	public double getShipMass(Entity shipEnt);
}
