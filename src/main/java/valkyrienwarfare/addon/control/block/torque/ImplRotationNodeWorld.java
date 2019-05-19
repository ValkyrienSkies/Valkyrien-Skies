package valkyrienwarfare.addon.control.block.torque;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
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
     *
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
        PhysicsAssert.assertPhysicsThread();

        Iterator<Map.Entry<BlockPos, IRotationNode>> nodeIterator = posToNodeMap.entrySet().iterator();
        while (nodeIterator.hasNext()) {
            Map.Entry<BlockPos, IRotationNode> entry = nodeIterator.next();
            if (entry.getValue().markedForDeletion()) {
                System.out.println("Deleted");
                nodeIterator.remove();
            }
        }

        processQueuedTasks();
        // Write da code here!
        List<IRotationNode> nodesToVisit = new ArrayList<>(posToNodeMap.values());
        Collections.sort(nodesToVisit);
        // System.out.println(nodesToVisit.size());
        while (nodesToVisit.size() > 0) {
            IRotationNode start = nodesToVisit.remove(nodesToVisit.size() - 1);
            try {
                NodeTaskProcessed nodeNetworkResult = processNodeNetwork(start, null, null, nodesToVisit, timeDelta, 1D);
                double firstNodeNewVelocity = Math.sqrt(nodeNetworkResult.totalEnergy * 2D / nodeNetworkResult.v_sqr_coefficent);
                if (nodeNetworkResult.momentumMultDotProduct != 0D) {
                    firstNodeNewVelocity *= Math.signum(nodeNetworkResult.momentumMultDotProduct);
                }

                processNodeNetworkPhase2(start, firstNodeNewVelocity, new HashSet<>());
            } catch (Exception e) {
                // Otherwise we shall set everything to zero!
                processNodeNetworkPhase2(start, 0, new HashSet<>());
                e.printStackTrace();
            }
        }
    }

    /**
     * @param start
     * @param from
     * @param sideFrom
     * @param nodesToVisit
     * @param timeDelta
     * @param multiplier   The relative ratio of w_i / w_0.
     * @return
     */
    private NodeTaskProcessed processNodeNetwork(IRotationNode start, IRotationNode from, EnumFacing sideFrom, List<IRotationNode> nodesToVisit, double timeDelta, double multiplier) {
        if (!nodesToVisit.contains(start) && from != null) {
            throw new IllegalStateException("This is not an acyclic graph!");
        }

        // This first
        nodesToVisit.remove(start);
        // Then simulate torque added energy
        start.simulate(timeDelta, this.parent);
        // Then add energy to the count
        double totalEnergy = start.getEnergy();
        // Calculate the dot; TODO change this to simulate entire gear networks as just 1 gear.
        double dotProduct = start.getRotationalInertia() * start.getAngularVelocity() * Math.signum(multiplier);
        // Then calculate the coefficient
        double coefficientAdded = start.getRotationalInertia() * multiplier * multiplier;
        for (Tuple<IRotationNode, EnumFacing> connectedNode : start.connectedTorqueTilesList()) {
            if (nodesToVisit.contains(connectedNode.getFirst())) {
                double newMultiplier = multiplier * start.getAngularVelocityRatioFor(connectedNode.getSecond()).get() / connectedNode.getFirst().getAngularVelocityRatioFor(connectedNode.getSecond().getOpposite()).get();

                NodeTaskProcessed subTask = processNodeNetwork(connectedNode.getFirst(), start, connectedNode.getSecond(), nodesToVisit, timeDelta, newMultiplier);
                totalEnergy += subTask.totalEnergy;
                coefficientAdded += subTask.v_sqr_coefficent;
                dotProduct += subTask.momentumMultDotProduct;
            }
        }

        // Finally multiply by the input ratio (Except in the case of the first one)
        if (from != null) {
//            coefficient *= (from.getAngularVelocityRatioFor(sideFrom).get() / start.getAngularVelocityRatioFor(sideFrom.getOpposite()).get());
        }
        return new NodeTaskProcessed(totalEnergy, coefficientAdded, dotProduct);
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
     *
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
     *
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
     *
     * @param pos
     * @param node
     * @return The prevous node if there was one, null otherwise.
     */
    @PhysicsThreadOnly()
    @Override
    public IRotationNode setNodeFromPos(BlockPos pos, IRotationNode node) {
        PhysicsAssert.assertPhysicsThread();
        assert (node == null) || node.isInitialized() : "NodeAtPos " + pos + " was not initialized!";
        node.setPlacedIntoNodeWorld(true);
        return posToNodeMap.put(pos, node);
    }

    /**
     * This can only be called by the physics thread.
     *
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
        private double momentumMultDotProduct;

        private NodeTaskProcessed(double totalEnergy, double v_sqr_coefficent, double momentumMultDotProduct) {
            this.totalEnergy = totalEnergy;
            this.v_sqr_coefficent = v_sqr_coefficent;
            this.momentumMultDotProduct = momentumMultDotProduct;
        }
    }
}
