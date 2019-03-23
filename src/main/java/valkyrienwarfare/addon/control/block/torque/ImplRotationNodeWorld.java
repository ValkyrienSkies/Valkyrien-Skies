package valkyrienwarfare.addon.control.block.torque;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.Sys;
import valkyrienwarfare.physics.management.PhysicsObject;

import java.util.*;
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
        queuedTasks.add(() -> {
            IRotationNode nodeAtPos = getNodeFromPos(taskPos);
            if (nodeAtPos != null) {
                task.accept(nodeAtPos);
            }
        });
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
        for (IRotationNode node : posToNodeMap.values()) {
            while (!node.getQueuedTasks().isEmpty()) {
                node.getQueuedTasks().remove().run();
            }
        }
    }

    /**
     * This can only be called by the physics thread.
     */
    @PhysicsThreadOnly
    @Override
    public void processTorquePhysics(double timeDelta) {
        // System.out.println("Hi");
        PhysicsAssert.assertPhysicsThread();

        Iterator<Map.Entry<BlockPos, IRotationNode>> iter = posToNodeMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<BlockPos, IRotationNode> entry = iter.next();
            if (entry.getValue().markedForDeletion()) {
                System.out.println("Deleted");
                iter.remove();
            }
        }

        processQueuedTasks();
        // Write da code here!
        List<IRotationNode> nodesToVisit = new ArrayList<>(posToNodeMap.values());
        while (nodesToVisit.size() > 0) {
            IRotationNode start = nodesToVisit.remove(nodesToVisit.size() - 1);
            try {
                NodeTaskProcessed nodeNetworkResult = processNodeNetwork(start, null, null, nodesToVisit, timeDelta);
                // Then set the crap
                processNodeNetworkPhase2(start, Math.sqrt(nodeNetworkResult.totalEnergy * 2D / nodeNetworkResult.v_sqr_coefficent), new HashSet<>());
            } catch (Exception e) {
                processNodeNetworkPhase2(start, 0, new HashSet<>());
                e.printStackTrace();
            }
        }
    }

    private NodeTaskProcessed processNodeNetwork(IRotationNode start, IRotationNode from, EnumFacing sideFrom, List<IRotationNode> nodesToVisit, double timeDelta) {
        if (!nodesToVisit.contains(start) && from != null) {
            throw new IllegalStateException("This is not an acyclic graph!");
        }
        // This first
        nodesToVisit.remove(start);
        // Then simulate torque added energy
        start.simulate(timeDelta, this.parent);
        // Then add energy to the count
        double totalEnergy = start.getEnergy();
        // Then calculate the coefficient
        double coefficient = start.getRotationalInertia();
        for (Tuple<IRotationNode, EnumFacing> connectedNode : start.connectedTorqueTilesList()) {
            if (nodesToVisit.contains(connectedNode.getFirst())) {
                NodeTaskProcessed subTask = processNodeNetwork(connectedNode.getFirst(), start, connectedNode.getSecond(), nodesToVisit, timeDelta);
                totalEnergy += subTask.totalEnergy;
                coefficient += subTask.v_sqr_coefficent;
            }
        }

        // Finally multiply by the input ratio (Except in the case of the first one)
        if (from != null) {
//            coefficient *= (from.getAngularVelocityRatioFor(sideFrom).get() / start.getAngularVelocityRatioFor(sideFrom.getOpposite()).get());
        }
        return new NodeTaskProcessed(totalEnergy, coefficient);
    }

    private void processNodeNetworkPhase2(IRotationNode start, double newAngularVel, Set<IRotationNode> visitedNodes) {
        assert !visitedNodes.contains(start) : "This isn't right!";
        visitedNodes.add(start);
        start.setAngularVelocity(newAngularVel);
        for (Tuple<IRotationNode, EnumFacing> connectedNode : start.connectedTorqueTilesList()) {
            if (!visitedNodes.contains(connectedNode.getFirst())) {
                processNodeNetworkPhase2(connectedNode.getFirst(), newAngularVel * -1 * start.getAngularVelocityRatioFor(connectedNode.getSecond()).get() / connectedNode.getFirst().getAngularVelocityRatioFor(connectedNode.getSecond().getOpposite()).get(), visitedNodes);
            }
        }
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

    @Override
    public void readFromNBTTag(NBTTagCompound compound) {

    }

    @Override
    public void writeToNBTTag(NBTTagCompound compound) {

    }

    private static class NodeTaskProcessed {
        private double totalEnergy;
        private double v_sqr_coefficent;

        private NodeTaskProcessed(double totalEnergy, double v_sqr_coefficent) {
            this.totalEnergy = totalEnergy;
            this.v_sqr_coefficent = v_sqr_coefficent;
        }
    }
}
