package ValkyrienWarfareBase.PhysicsManagement;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.PhysicsManagement.Network.PhysWrapperPositionMessage;

/**
 * Used by the client to manage all the transformations sent to it by the server, and queues them for smooth delivery and presentation on screen
 * 
 * @author thebest108
 *
 */
public class ShipTransformationStack {

	public ShipTransformData[] recentTransforms = new ShipTransformData[20];
	// Number of ticks the parent ship has been active for
	// Increases by 1 for every message pushed onto the stack
	public int messageRelativeTick;

	public void pushMessage(PhysWrapperPositionMessage toPush) {
		// Shift whole array to the right
		for (int index = recentTransforms.length - 2; index >= 0; index--) {
			recentTransforms[index + 1] = recentTransforms[index];
		}
		recentTransforms[0] = new ShipTransformData(toPush, messageRelativeTick);
		messageRelativeTick++;

	}

	// TODO: Make this auto-adjust to best settings for the server
	public ShipTransformData getDataForTick(int lastTick) {
		// TODO: This may cause some horrific error; watch out for recentTransforms[0]==null !
		if (ValkyrienWarfareMod.shipTickDelay == 0) {
			return recentTransforms[0];
		}
		if (recentTransforms[0] == null) {
			System.err.println("A SHIP JUST RETURNED NULL FOR 'recentTransforms[0]==null'; ANY WEIRD ERRORS PAST HERE ARE DIRECTLY LINKED TO THAT!");
			return null;
		}
		int tickToGet = lastTick + 1;
		int idealTick = recentTransforms[0].relativeTick - ValkyrienWarfareMod.shipTickDelay;
		if (recentTransforms[0].relativeTick - tickToGet + ValkyrienWarfareMod.shipTickDelay > ValkyrienWarfareMod.maxMissedPackets) {
			// System.out.println("bad"+ValkyrienWarfareMod.maxMissedPackets);
			tickToGet = idealTick;
		}
		for (ShipTransformData transform : recentTransforms) {
			if (transform != null) {
				if (transform.relativeTick == tickToGet) {
					return transform;
				}
			}
		}

		return recentTransforms[0];
	}

}

class ShipTransformData {

	public int relativeTick;

	public double posX, posY, posZ;
	public double pitch, yaw, roll;
	public Vector centerOfRotation;

	public ShipTransformData(PhysWrapperPositionMessage wrapperMessage, int relTick) {
		posX = wrapperMessage.posX;
		posY = wrapperMessage.posY;
		posZ = wrapperMessage.posZ;

		pitch = wrapperMessage.pitch;
		yaw = wrapperMessage.yaw;
		roll = wrapperMessage.roll;

		centerOfRotation = wrapperMessage.centerOfMass;

		relativeTick = relTick;
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