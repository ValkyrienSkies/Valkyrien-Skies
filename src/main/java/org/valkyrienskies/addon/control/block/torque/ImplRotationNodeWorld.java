package org.valkyrienskies.addon.control.block.torque;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.ships.ship_world.IHasShipManager;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;
import org.valkyrienskies.mod.common.ships.ship_world.WorldServerShipManager;

public class ImplRotationNodeWorld implements IRotationNodeWorld {

    // Null IFF parentWorld != null
    @Nullable
    private final WeakReference<PhysicsObject> parent;
    // Null IFF parent != null
    @Nullable
    private final WeakReference<World> parentWorld;
    @Nonnull
    private final Map<BlockPos, IRotationNode> posToNodeMap;
    @Nonnull
    private final ConcurrentLinkedQueue<Runnable> queuedTasks;

    public ImplRotationNodeWorld(@Nonnull PhysicsObject parent) {
        this.parent = new WeakReference<>(parent);
        this.parentWorld = null;
        this.posToNodeMap = new HashMap<>();
        this.queuedTasks = new ConcurrentLinkedQueue<>();

        ((WorldServerShipManager) ((IHasShipManager) parent.getWorld()).getManager())
                .getPhysicsThread().addRecurringTask(this::processTorquePhysics);
        ((WorldServerShipManager) ((IHasShipManager) parent.getWorld()).getManager())
                .getPhysicsThread().addRecurringTask(physTimeDelta -> processQueuedTasks());
    }

    public ImplRotationNodeWorld(@Nonnull World parentWorld) {
        this.parent = null;
        this.parentWorld = new WeakReference<>(parentWorld);
        this.posToNodeMap = new HashMap<>();
        this.queuedTasks = new ConcurrentLinkedQueue<>();

        ((WorldServerShipManager) ((IHasShipManager) parentWorld).getManager())
                .getPhysicsThread().addRecurringTask(this::processTorquePhysics);
        ((WorldServerShipManager) ((IHasShipManager) parentWorld).getManager())
                .getPhysicsThread().addRecurringTask(physTimeDelta -> processQueuedTasks());
    }

    @Override
    public void enqueueTaskOntoWorld(Runnable task) {
        queuedTasks.add(task);
    }

    /**
     * This can be run by any thread.
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
     * Advances the gear train simulation forward by 1 timestep.
     *
     * @param timeDelta The time step that will be simulated.
     */
    @PhysicsThreadOnly
    @Override
    public void processTorquePhysics(double timeDelta) {
        PhysicsAssert.assertPhysicsThread();
        // Remove rotation nodes that were marked for deletion
        posToNodeMap.entrySet().removeIf(entry -> entry.getValue().markedForDeletion());

        processQueuedTasks();

        List<IRotationNode> nodesToVisit = new ArrayList<>(posToNodeMap.values());
        Collections.shuffle(nodesToVisit);

        // Very naive inefficient algorithm, but I think it works pretty well.
        // For reference, omega is angular velocity.
        while (nodesToVisit.size() > 0) {
            IRotationNode startNode = nodesToVisit.get(0);
            Set<IRotationNode> visitedNodes = new HashSet<>();
            double apparentTorque = calculateApparentTorque(startNode, visitedNodes);
            visitedNodes.clear(); // not the best practice
            double apparentInertia = calculateApparentInertia(startNode, visitedNodes);
            visitedNodes.clear(); // not the best practice
            // Maybe I should replace apparent omega with apparent angular momentum.
            double apparentOmega = calculateApparentOmega(startNode, visitedNodes);
            visitedNodes.clear(); // not the best practice
            double gearTrainEnergy = calculateTotalEnergy(startNode, visitedNodes);
            visitedNodes.clear(); // not the best practice
            double apparentAngularAcceleration = apparentTorque / apparentInertia;
            double deltaOmega = apparentAngularAcceleration * timeDelta;
            // Try to estimate the best guess for the current omega based on gear train energy
            double omegaGuess = Math.sqrt(2 * gearTrainEnergy / apparentInertia);
            // Guess the direction of rotation based on apparent omega.
            omegaGuess = omegaGuess * Math.signum(apparentOmega);
            double newOmega = omegaGuess + deltaOmega;
            if (!Double.isNaN(newOmega)) {
                // Apply deltaOmega to all rotation nodes.
                applyNewOmega(startNode, newOmega, timeDelta, visitedNodes);
            } else {
                System.err.println(
                    "Gear Train Simulation Error, Resetting Rotation Nodes.\nOmega guess is "
                        + omegaGuess
                        + "\nDelta omega is " + deltaOmega);
                resetGearTrain(startNode, visitedNodes);
            }

            // Remove the nodes we just processed from those that must be visited.
            nodesToVisit.removeAll(visitedNodes);
        }
    }

    /**
     * @param start        The node where we start our traversal of the gear train graph to reset
     *                     the train.
     * @param visitedNodes Nodes that have already been reset.
     */
    private void resetGearTrain(IRotationNode start, Set<IRotationNode> visitedNodes) {
        visitedNodes.add(start); // kind of a bad spot to put this
        start.setAngularRotation(0);
        start.setAngularVelocity(0);

        for (Tuple<IRotationNode, EnumFacing> connectedNode : start.connectedTorqueTilesList()) {
            IRotationNode endNode = connectedNode.getFirst();
            if (visitedNodes.contains(connectedNode.getFirst())) {
                continue;
            }
            resetGearTrain(endNode, visitedNodes);
        }
    }

    /**
     * Set the angular velocity for an entire gear train based on the gear ratios.
     *
     * @param start        The node that will get get angular velocity of newOmega.
     * @param newOmega     The new angular velocity of the node start.
     * @param deltaTime    The timestep used in our gear train simulation.
     * @param visitedNodes Nodes that won't have their angular velocity changed.
     */
    private void applyNewOmega(IRotationNode start, double newOmega, double deltaTime,
        Set<IRotationNode> visitedNodes) {
        visitedNodes.add(start); // kind of a bad spot to put this

        start.setAngularRotation(
            start.getAngularRotation() + (start.getAngularVelocity() * deltaTime) + (
                (newOmega - start.getAngularVelocity()) * deltaTime / 2D));

        start.setAngularVelocity(newOmega);

        for (Tuple<IRotationNode, EnumFacing> connectedNode : start.connectedTorqueTilesList()) {
            IRotationNode endNode = connectedNode.getFirst();
            EnumFacing exploreDirection = connectedNode.getSecond();
            if (visitedNodes.contains(connectedNode.getFirst())) {
                continue;
            }
            double ratioStart = start.getAngularVelocityRatioFor(exploreDirection).get();
            double ratioEnd = endNode.getAngularVelocityRatioFor(exploreDirection.getOpposite())
                .get();
            double multiplier = -ratioStart / ratioEnd;

            applyNewOmega(endNode, newOmega * multiplier, deltaTime, visitedNodes);
        }
    }

    /**
     * Calculate the rotational total energy stored in a gear train (not including any nodes in
     * visitedNodes).
     *
     * @param start
     * @param visitedNodes Nodes we ignore in our calculations.
     * @return
     */
    private double calculateTotalEnergy(IRotationNode start, Set<IRotationNode> visitedNodes) {
        visitedNodes.add(start); // kind of a bad spot to put this
        // actual code start
        double totalEnergy = start.getEnergy();
        for (Tuple<IRotationNode, EnumFacing> connectedNode : start.connectedTorqueTilesList()) {
            IRotationNode endNode = connectedNode.getFirst();
            EnumFacing exploreDirection = connectedNode.getSecond();
            if (visitedNodes.contains(connectedNode.getFirst())) {
                continue;
            }
            // double ratioStart = start.getAngularVelocityRatioFor(exploreDirection).get();
            // double ratioEnd = endNode.getAngularVelocityRatioFor(exploreDirection.getOpposite()).get();
            // double multiplier = -ratioStart / ratioEnd;

            totalEnergy += calculateTotalEnergy(endNode, visitedNodes);
        }
        return totalEnergy;
    }

    /**
     * Calculate the rotational inertia of the start node with the gear train (not including any
     * nodes in visitedNodes) attached. Reference: https://www.engineersedge.com/motors/gear_drive_system.htm
     *
     * @param start        The node which we calculate the inertia relative to.
     * @param visitedNodes Nodes we ignore in our calculations.
     * @return
     */
    private double calculateApparentInertia(IRotationNode start, Set<IRotationNode> visitedNodes) {
        visitedNodes.add(start); // kind of a bad spot to put this
        // actual code start
        double apparentInertia = start.getRotationalInertia();
        for (Tuple<IRotationNode, EnumFacing> connectedNode : start.connectedTorqueTilesList()) {
            IRotationNode endNode = connectedNode.getFirst();
            EnumFacing exploreDirection = connectedNode.getSecond();
            if (visitedNodes.contains(connectedNode.getFirst())) {
                continue;
            }
            double ratioStart = start.getAngularVelocityRatioFor(exploreDirection).get();
            double ratioEnd = endNode.getAngularVelocityRatioFor(exploreDirection.getOpposite())
                .get();
            double multiplier = -ratioStart / ratioEnd;

            apparentInertia += (multiplier * multiplier * calculateApparentInertia(endNode,
                visitedNodes));
        }
        return apparentInertia;
    }

    /**
     * Calculate the 'apparent' angular velocity of a gear train (not including any nodes in
     * visitedNodes) relative to start. This probably isn't physically correct, but its good enough
     * for our purposes.
     *
     * @param start        The node we calculate the apparent angular velocity relative to.
     * @param visitedNodes Nodes we ignore in our calculations.
     * @return
     */
    private double calculateApparentOmega(IRotationNode start, Set<IRotationNode> visitedNodes) {
        visitedNodes.add(start); // kind of a bad spot to put this
        // actual code start
        double apparentOmega = start.getAngularVelocity();
        for (Tuple<IRotationNode, EnumFacing> connectedNode : start.connectedTorqueTilesList()) {
            IRotationNode endNode = connectedNode.getFirst();
            EnumFacing exploreDirection = connectedNode.getSecond();
            if (visitedNodes.contains(connectedNode.getFirst())) {
                continue;
            }
            double ratioStart = start.getAngularVelocityRatioFor(exploreDirection).get();
            double ratioEnd = endNode.getAngularVelocityRatioFor(exploreDirection.getOpposite())
                .get();
            double multiplier = -ratioStart / ratioEnd;
            apparentOmega += (multiplier * calculateApparentOmega(endNode, visitedNodes));
        }
        return apparentOmega;
    }

    /**
     * Calculate the net torque experienced by the gear train (not including any nodes in
     * visitedNodes) relative to start.
     *
     * @param start        The node we calculate the apparent torque relative to.
     * @param visitedNodes Nodes we ignore in our calculations.
     * @return
     */
    private double calculateApparentTorque(IRotationNode start, Set<IRotationNode> visitedNodes) {
        visitedNodes.add(start); // kind of a bad spot to put this
        PhysicsObject ship = null;
        if (parent != null) {
            ship = parent.get();
        }
        // actual code start
        double apparentTorque = start.calculateInstantaneousTorque(ship);
        for (Tuple<IRotationNode, EnumFacing> connectedNode : start.connectedTorqueTilesList()) {
            IRotationNode endNode = connectedNode.getFirst();
            EnumFacing exploreDirection = connectedNode.getSecond();
            if (visitedNodes.contains(connectedNode.getFirst())) {
                continue;
            }
            double ratioStart = start.getAngularVelocityRatioFor(exploreDirection).get();
            double ratioEnd = endNode.getAngularVelocityRatioFor(exploreDirection.getOpposite())
                .get();
            double multiplier = -ratioStart / ratioEnd;
            apparentTorque += (multiplier * calculateApparentTorque(endNode, visitedNodes));
        }
        return apparentTorque;
    }

    /**
     * This can only be called by the physics thread.
     *
     * @param pos The pos of the node we want.
     * @return The node at that pos if there is one, null otherwise.
     */
    @PhysicsThreadOnly
    @Override
    public IRotationNode getNodeFromPos(BlockPos pos) {
        PhysicsAssert.assertPhysicsThread();
        IRotationNode nodeAtPos = posToNodeMap.get(pos);
        assert
            (nodeAtPos == null) || nodeAtPos.isInitialized() :
            "NodeAtPos " + nodeAtPos + " was not initialized!";
        return nodeAtPos;
    }

    /**
     * This can only be called by the physics thread.
     *
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
     * @return The previous node if there was one, null otherwise.
     */
    @PhysicsThreadOnly()
    @Override
    public IRotationNode setNodeFromPos(BlockPos pos, IRotationNode node) {
        PhysicsAssert.assertPhysicsThread();
        assert
            (node == null) || node.isInitialized() : "NodeAtPos " + pos + " was not initialized!";
        node.setPlacedIntoNodeWorld(true);
        return posToNodeMap.put(pos, node);
    }

    /**
     * This can only be called by the physics thread.
     *
     * @return The previous node if there was one, null otherwise.
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

}
