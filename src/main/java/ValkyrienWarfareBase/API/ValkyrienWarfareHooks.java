package ValkyrienWarfareBase.API;

import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Call whatever method you need from here. Outside of Vector, all the objects here are generic (Ships being Entities). Just be sure not to pass something wrong into here
 * 
 * @author thebest108
 *
 */
public class ValkyrienWarfareHooks {

	// Replaced with an object that does real work in runtime
	public static DummyMethods methods = null;
	public static boolean isValkyrienWarfareInstalled = false;

	public static PhysicsWrapperEntity getShipEntityManagingPos(World worldObj, BlockPos pos) {
		return methods.getShipEntityManagingPos(worldObj, pos);
	}

	/**
	 * Tells you if a block position is being used by a Ship
	 * 
	 * @param worldObj
	 * @param pos
	 * @return
	 */
	public static boolean isBlockPartOfShip(World worldObj, BlockPos pos) {
		return methods.isBlockPartOfShip(worldObj, pos);
	}

	/**
	 * Converts a coordinate from the World space to the Ship space
	 * 
	 * @param worldObj
	 * @param shipEnt
	 * @param positionInWorld
	 * @return A new Vector object, not the same as the one inputed
	 */
	public static Vector getPositionInShipFromReal(World worldObj, Entity shipEnt, Vector positionInWorld) {
		return methods.getPositionInShipFromReal(worldObj, shipEnt, positionInWorld);
	}

	/**
	 * Converts a coordinate from the Ship space to the World space
	 * 
	 * @param worldObj
	 * @param shipEnt
	 * @param positionInWorld
	 * @return A new Vector object, not the same as the one inputed
	 */
	public static Vector getPositionInRealFromShip(World worldObj, Entity shipEnt, Vector posInShip) {
		return methods.getPositionInRealFromShip(worldObj, shipEnt, posInShip);
	}

	/**
	 * 
	 * @param entityToTest
	 * @return True if the entity is a Ship, false if it isn't
	 */
	public static boolean isEntityAShip(Entity entityToTest) {
		return methods.isEntityAShip(entityToTest);
	}

	/**
	 * 
	 * @param shipEnt
	 * @return The Center of Mass coordinates of a Ship in Ship space
	 */
	public static Vector getShipCenterOfMass(Entity shipEnt) {
		return methods.getShipCenterOfMass(shipEnt);
	}

	/**
	 * 
	 * @param shipEnt
	 * @param secondsToApply
	 * @return A vector with the linear velocity of a Ship at that instant
	 */
	public static Vector getLinearVelocity(Entity shipEnt, double secondsToApply) {
		return methods.getLinearVelocity(shipEnt, secondsToApply);
	}

	/**
	 * 
	 * @param shipEnt
	 * @return A vector with the angular velocty of a Ship at that instant
	 */
	public static Vector getAngularVelocity(Entity shipEnt) {
		return methods.getAngularVelocity(shipEnt);
	}

	/**
	 * 
	 * @param shipEnt
	 * @return The matrix which converts local coordinates (The positions of the blocks in the world) to the entity coordinates (The position in front of the player)
	 */
	public static double[] getShipTransformMatrix(Entity shipEnt) {
		return methods.getShipTransformMatrix(shipEnt);
	}

	/**
	 * 
	 * @param shipEnt
	 * @param inBody
	 * @param secondsToApply
	 * @return A Vector with the velocity of a point in Ship space relative to the real world, at that instant
	 */
	public static Vector getVelocityAtPoint(Entity shipEnt, Vector inBody, double secondsToApply) {
		return methods.getVelocityAtPoint(shipEnt, inBody, secondsToApply);
	}

	/**
	 * 
	 * @param shipEnt
	 * @return The mass of the Ship
	 */
	public static double getShipMass(Entity shipEnt) {
		return methods.getShipMass(shipEnt);
	}
}
