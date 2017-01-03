package ValkyrienWarfareBase.API;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Call whatever method you need from here. Outside of Vector, all the objects here are generic (Ships being Entities). Just be sure not to pass something wrong into here
 * @author thebest108
 *
 */
public class PhysicsEntityHooks {

	//Replaced with an object that does real work in runtime
	public static DummyMethods methods;
	
	public static Entity getShipEntityManagingPos(World worldObj, BlockPos pos){
		return methods.getShipEntityManagingPos(worldObj, pos);
	}
	
	public static boolean isBlockPartOfShip(World worldObj, BlockPos pos){
		return methods.isBlockPartOfShip(worldObj, pos);
	}
	
	public static Vector getPositionInShipFromReal(World worldObj, Entity shipEnt, Vector positionInWorld){
		return methods.getPositionInShipFromReal(worldObj, shipEnt, positionInWorld);
	}
	
	public static Vector getPositionInRealFromShip(World worldObj, Entity shipEnt, Vector posInShip){
		return methods.getPositionInRealFromShip(worldObj, shipEnt, posInShip);
	}
	
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
