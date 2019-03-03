package valkyrienwarfare.addon.control.block.torque;

import net.minecraft.util.math.BlockPos;
import valkyrienwarfare.physics.management.PhysicsObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class ImplRotationNodeWorld implements IRotationNodeWorld {

    private final PhysicsObject parent;
    private final Map<BlockPos, IRotationNode> posToNodeMap;
    private final ConcurrentLinkedQueue<Runnable> queuedTasks;

    public ImplRotationNodeWorld(PhysicsObject parent) {
        this.parent = parent;
        this.posToNodeMap = new HashMap<>();
        this.queuedTasks = new ConcurrentLinkedQueue();
    }

    @Override
    public void enqueueTaskOntoWorld(Runnable task) {
        queuedTasks.add(task);
    }

    /**
     * This can be run by any thread.
     * @param task
     */
    @Override
    public void enqueueTaskOntoNode(Consumer<IRotationNode> task, BlockPos taskPos) {
        queuedTasks.add(() -> task.accept(getNodeFromPos(taskPos)));
    }

    /**
     * This can only be called by the physics thread.
     */
    @PhysicsThreadOnly
    @Override
    public void processQueuedTasks() {
        PhysicsAssert.assertPhysicsThread();
        while (!queuedTasks.isEmpty()) {
            Runnable queuedTask = queuedTasks.remove();
            queuedTask.run();
        }
    }

    /**
     * This can only be called by the physics thread.
     */
    @PhysicsThreadOnly
    @Override
    public void processTorquePhysics(double timeDelta) {
        PhysicsAssert.assertPhysicsThread();
        processQueuedTasks();
        // Write da code here!
    }

    /**
     * This can only be called by the physics thread.
     * @param pos
     * @return The node at that pos if there is one, null otherwise.
     */
    @PhysicsThreadOnly
    @Override
    public IRotationNode getNodeFromPos(BlockPos pos) {
        PhysicsAssert.assertPhysicsThread();
        IRotationNode nodeAtPos = posToNodeMap.get(pos);
        assert (nodeAtPos == null) || nodeAtPos.isInitialized() : "NodeAtPos " + nodeAtPos + " was not initialized!";
        return nodeAtPos;
    }

    /**
     * This can only be called by the physics thread.
     * @param pos
     * @return The node at that pos if there is one, null otherwise.
     */
    @PhysicsThreadOnly
    public boolean hasNodeAtPos(BlockPos pos) {
        PhysicsAssert.assertPhysicsThread();
        return posToNodeMap.containsKey(pos);
    }

    /**
     * This can only be called by the physics thread.
     * @param pos
     * @param node
     * @return The prevous node if there was one, null otherwise.
     */
    @PhysicsThreadOnly()
    @Override
    public IRotationNode setNodeFromPos(BlockPos pos, IRotationNode node) {
        PhysicsAssert.assertPhysicsThread();
        assert (node == null) || node.isInitialized() : "NodeAtPos " + pos + " was not initialized!";
        return posToNodeMap.put(pos, node);
    }

    /**
     * This can only be called by the physics thread.
     * @param pos
     * @return The prevous node if there was one, null otherwise.
     */
    @PhysicsThreadOnly()
    @Override
    public IRotationNode removePos(BlockPos pos) {
        PhysicsAssert.assertPhysicsThread();
        return posToNodeMap.remove(pos);
    }
}
