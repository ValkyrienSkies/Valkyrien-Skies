package org.valkyrienskies.addon.control.block.torque;

import org.valkyrienskies.mod.common.multithreaded.VWThread;

public class PhysicsAssert {

    public static void assertPhysicsThread() {
        // This is technically incorrect because any changes to VWThread will break this, but why not its easy.
        assert Thread.currentThread() instanceof VWThread : "We are not running on a VW thread!";
    }
}
