package org.valkyrienskies.mod.common.util.multithreaded;

/**
 * A task that will run on the physics thread, and know the time delta for that physics tick when it runs.
 */
public interface IPhysTimeTask {

    void runTask(double physTimeDelta);
}
