package ValkyrienWarfareBase.PhysicsManagement;

import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.Network.PhysWrapperPositionMessage;

/**
 * Used by the client to manage all the transformations sent to it by the server, and queues them for smooth delivery and presentation on screen
 *
 * @author thebest108
 */
public class ShipTransformationStack {
	
	public ShipTransformData[] recentTransforms = new ShipTransformData[20];
	// Number of ticks the parent ship has been active for
	// Increases by 1 for every message pushed onto the stack
	
	public void pushMessage(PhysWrapperPositionMessage toPush) {
		// Shift whole array to the right
		for (int index = recentTransforms.length - 2; index >= 0; index--) {
			recentTransforms[index + 1] = recentTransforms[index];
		}
		recentTransforms[0] = new ShipTransformData(toPush);
		
	}
	
	// TODO: Make this auto-adjust to best settings for the server
	public ShipTransformData getDataForTick(int lastTick) {
		if (recentTransforms[0] == null) {
			System.err.println("A SHIP JUST RETURNED NULL FOR 'recentTransforms[0]==null'; ANY WEIRD ERRORS PAST HERE ARE DIRECTLY LINKED TO THAT!");
			return null;
		}
		int tickToGet = lastTick + 1;
		
		int realtimeTick = recentTransforms[0].relativeTick;
		
		if (realtimeTick - lastTick > 3) {
			tickToGet = realtimeTick - 2;
//			System.out.println("Too Slow");
		}
		
		for (ShipTransformData transform : recentTransforms) {
			if (transform != null) {
				if (transform.relativeTick == tickToGet) {
					return transform;
				}
			}
		}

//		System.out.println("Couldnt find the needed transform");
		
		if (recentTransforms[1] != null) {
			return recentTransforms[1];
		}
		
		return recentTransforms[0];
	}
	
}

class ShipTransformData {
	
	public int relativeTick;
	
	public double posX, posY, posZ;
	public double pitch, yaw, roll;
	public Vector centerOfRotation;
	
	public ShipTransformData(PhysWrapperPositionMessage wrapperMessage) {
		posX = wrapperMessage.posX;
		posY = wrapperMessage.posY;
		posZ = wrapperMessage.posZ;
		
		pitch = wrapperMessage.pitch;
		yaw = wrapperMessage.yaw;
		roll = wrapperMessage.roll;
		
		centerOfRotation = wrapperMessage.centerOfMass;
		
		relativeTick = wrapperMessage.relativeTick;
	}
	
	// Apply all the position/rotation variables accordingly onto the passed physObject
	public void applyToPhysObject(PhysicsObject physObj) {
		physObj.wrapper.posX = posX;
		physObj.wrapper.posY = posY;
		physObj.wrapper.posZ = posZ;
		
		physObj.wrapper.pitch = pitch;
		physObj.wrapper.yaw = yaw;
		physObj.wrapper.roll = roll;
		
		physObj.centerCoord = centerOfRotation;
	}
}