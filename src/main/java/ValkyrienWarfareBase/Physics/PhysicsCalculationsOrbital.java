package ValkyrienWarfareBase.Physics;

import ValkyrienWarfareBase.PhysicsManagement.PhysicsObject;

public class PhysicsCalculationsOrbital extends PhysicsCalculations {

	public boolean isOrbitalPhased = true;

	public PhysicsCalculationsOrbital(PhysicsObject toProcess) {
		super(toProcess);
	}

	@Override
	public void processWorldCollision() {
		if(!isOrbitalPhased){
			super.processWorldCollision();
		}
	}

	@Override
	public void calculateForces() {
		isOrbitalPhased = true;
		if(!isOrbitalPhased){
			super.calculateForces();
		}
	}

	@Override
	public void addQueuedForces() {
		if(!isOrbitalPhased){
			super.addQueuedForces();
		}
	}

}
