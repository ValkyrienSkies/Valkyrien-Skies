package ValkyrienWarfareBase.PhysicsManagement;

import java.util.concurrent.Callable;

public class PhysCollisionCallable implements Callable<Void> {

    private final PhysicsObject toRun;

    public PhysCollisionCallable(PhysicsObject physicsCalculations) {
        toRun = physicsCalculations;
    }

    @Override
    public Void call() throws Exception {
        toRun.physicsProcessor.processWorldCollision();
        ;
        toRun.physicsProcessor.rawPhysTickPostCol();
        return null;
    }

}
