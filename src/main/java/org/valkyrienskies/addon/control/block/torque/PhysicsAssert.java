package org.valkyrienskies.addon.control.block.torque;

import org.valkyrienskies.mod.common.util.multithreaded.VSThread;

public class PhysicsAssert {

    public static void assertPhysicsThread() {
        // This is technically incorrect because any changes to VSThread will break this, but why not its easy.
        assert Thread.currentThread() instanceof VSThread : "We are not running on a VW thread!";
    }
}
