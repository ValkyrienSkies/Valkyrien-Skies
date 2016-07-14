package ValkyrienWarfareBase.PhysicsManagement;

import ValkyrienWarfareBase.Physics.PhysicsCalculations;

public class PhysCollisionRunnable implements Runnable{

	private final PhysicsCalculations toRun;
	
	public PhysCollisionRunnable(PhysicsCalculations physicsCalculations) {
		toRun = physicsCalculations;
	}

	@Override
	public void run() {
		toRun.worldCollision.runPhysCollision();
	}

}
