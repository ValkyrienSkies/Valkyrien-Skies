package ValkyrienWarfareBase.PhysicsManagement;

import java.util.concurrent.Callable;

import ValkyrienWarfareBase.Physics.PhysicsCalculations;

public class PhysCollisionCallable implements Callable<Void> {

	private final PhysicsObject toRun;

	public PhysCollisionCallable(PhysicsObject physicsCalculations) {
		toRun = physicsCalculations;
	}

	@Override
	public Void call() throws Exception {
		toRun.physicsProcessor.worldCollision.runPhysCollision();
		toRun.physicsProcessor.rawPhysTickPostCol();
		return null;
	}

}
