package org.valkyrienskies.addon.control.tileentity;

import org.valkyrienskies.mod.common.physics.PhysicsCalculations;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;

public class TileEntityLiftValve extends TileEntityNodeControllerImpl {

    public static final int PHYSICS_PROCESSOR_PRIORITY = 10;

    public TileEntityLiftValve() {
        super(PHYSICS_PROCESSOR_PRIORITY);
    }

    @Override
    public void onPhysicsTick(PhysicsObject object, PhysicsCalculations calculations,
        double secondsToSimulate) {
        // Confirmed working
        /*
         * int size = 0; for (Object o : this.getNetworkedConnections()) { size++; }
         * System.out.println("debug2: " + size);
         */
    }

}
