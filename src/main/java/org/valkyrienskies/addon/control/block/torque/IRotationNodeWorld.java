package org.valkyrienskies.addon.control.block.torque;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import java.util.function.Consumer;

/**
 * This class is designed to carefully organize synchronization across the physics and game tick threads. Some of the
 * methods will be designed to only be called on the physics thread, and others to only be called on the game thread.
 * If these conventions are not respected then the game will likely break, so PLEASE follow each methods instructions!
 */
public interface IRotationNodeWorld {

    /**
     * This can be run by any thread.
     *
     * @param task
     */
    void enqueueTaskOntoWorld(Runnable task);

    /**
     * This can be run by any thread.
     *
     * @param task
     * @param taskPos
     */
    void enqueueTaskOntoNode(Consumer<IRotationNode> task, BlockPos taskPos);

    /**
     * This can only be called by the physics thread.
     */
    @PhysicsThreadOnly
    void processQueuedTasks();

    /**
     * This can only be called by the physics thread.
     */
    @PhysicsThreadOnly
    void processTorquePhysics(double timeDelta);

    /**
     * This can only be called by the physics thread.
     *
     * @param pos
     * @return The node at that pos if there is one, null otherwise.
     */
    @PhysicsThreadOnly
    IRotationNode getNodeFromPos(BlockPos pos);

    /**
     * This can only be called by the physics thread.
     *
     * @param pos
     * @return The node at that pos if there is one, null otherwise.
     */
    @PhysicsThreadOnly
    boolean hasNodeAtPos(BlockPos pos);

    /**
     * This can only be called by the physics thread.
     *
     * @param pos
     * @param node
     * @return The prevous node if there was one, null otherwise.
     */
    @PhysicsThreadOnly()
    IRotationNode setNodeFromPos(BlockPos pos, IRotationNode node);

    /**
     * This can only be called by the physics thread.
     *
     * @param pos
     * @return The prevous node if there was one, null otherwise.
     */
    @PhysicsThreadOnly()
    IRotationNode removePos(BlockPos pos);

    void readFromNBTTag(NBTTagCompound compound);

    void writeToNBTTag(NBTTagCompound compound);

}
