package ValkyrienWarfareBase.Physics;

import ValkyrienWarfareBase.API.Vector;

public class PhysicsQueuedForce {

	public Vector force;
	public Vector inBodyPos;
	public boolean isLocal;
	public int ticksToApply;

	public PhysicsQueuedForce(Vector force, Vector inBodyPosNoRotation, boolean isLocal, int ticksToApply) {
		this.force = force;
		this.inBodyPos = inBodyPosNoRotation;
		this.isLocal = isLocal;
		this.ticksToApply = ticksToApply;
	}

}
