package valkyrienwarfare.addon.control.block.torque;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import valkyrienwarfare.addon.control.block.torque.custom_torque_functions.SimpleTorqueFunction;
import valkyrienwarfare.physics.management.PhysicsObject;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

public interface IRotationNode extends Comparable<IRotationNode> {

    @PhysicsThreadOnly
    default double getEnergy() {
        return getAngularVelocity() * getAngularVelocity() * getRotationalInertia() / 2D;
    }

    @PhysicsThreadOnly
    void setAngularVelocity(double angularVelocity);

    @PhysicsThreadOnly
    void setAngularRotation(double angularRotation);

    @PhysicsThreadOnly
    default void simulate(double timeStep, PhysicsObject parent) {
        double torque = calculateInstantaneousTorque(parent);
        double deltaVelocity = (torque / getRotationalInertia()) * timeStep;
        this.setAngularRotation(this.getAngularRotation() + (this.getAngularVelocity() * timeStep) + ((torque / getRotationalInertia()) * timeStep * timeStep / 2D));
        this.setAngularVelocity(this.getAngularVelocity() + deltaVelocity);
    }

    @PhysicsThreadOnly
    default double calculateInstantaneousTorque(PhysicsObject parent) {
        if (!getCustomTorqueFunction().isPresent()) {
            // Default friction calculation
            return getAngularVelocity() * -.4 * getRotationalInertia();
        } else {
            // System.out.println("test");
            return getCustomTorqueFunction().get().apply(parent);
        }
    }

    @PhysicsThreadOnly
    void setCustomTorqueFunction(SimpleTorqueFunction customTorqueFunction);

    @PhysicsThreadOnly
    Optional<SimpleTorqueFunction> getCustomTorqueFunction();

    @PhysicsThreadOnly
    default boolean isConnectedToSide(EnumFacing side) {
        Optional<IRotationNode> connectedTo = getTileOnSide(side);
        if (!connectedTo.isPresent()) {
            return false;
        }
        return getAngularVelocityRatioFor(side).isPresent() && connectedTo.get().getAngularVelocityRatioFor(side.getOpposite()).isPresent();
    }

    @PhysicsThreadOnly
    Optional<Double> getAngularVelocityRatioFor(EnumFacing side);

    /**
     * The optional is only present if there is an attached tile for that side.
     * @param side
     * @return
     */
    @PhysicsThreadOnly
    Optional<IRotationNode> getTileOnSide(EnumFacing side);

    double getAngularVelocity();

    double getAngularRotation();

    double getAngularRotationUnsynchronized();

    double getAngularVelocityUnsynchronized();

    double getRotationalInertia();

    @PhysicsThreadOnly
    void setRotationalInertia(double newInertia);

    void markInitialized();

    boolean isInitialized();

    @PhysicsThreadOnly
    void setAngularVelocityRatio(EnumFacing side, Optional<Double> newRatio);

    @PhysicsThreadOnly
    void resetNodeData();

    void writeToNBT(NBTTagCompound compound);

    void readFromNBT(NBTTagCompound compound);

    /**
     * Higher values are more likely to be chosen for building the torque network.
     */
    default int getSortingPriority() {
        return 0;
    }

    @Override
    default int compareTo(IRotationNode o2) {
        return getSortingPriority() - o2.getSortingPriority();
    }

    @PhysicsThreadOnly
    default List<Tuple<IRotationNode, EnumFacing>> connectedTorqueTilesList() {
        List<Tuple<IRotationNode, EnumFacing>> connectedTiles = new ArrayList<>();
        for (EnumFacing facing : EnumFacing.values()) {
            if (isConnectedToSide(facing)) {
                connectedTiles.add(new Tuple(getTileOnSide(facing).get(), facing));
            }
        }
        return connectedTiles;
    }

    Optional<BlockPos> getNodePos();

    void queueTask(Runnable task);

    ConcurrentLinkedQueue<Runnable> getQueuedTasks();

    void queueNodeForDeletion();

    boolean markedForDeletion();

    boolean hasBeenPlacedIntoNodeWorld();

    void setPlacedIntoNodeWorld(boolean status);

    /**
     *
     * @return a copy of the internal data that is safe to modify.
     */
    Optional<Double>[] connectedRotationRatiosUnsychronized();

    default boolean isConnectedToSideUnsynchronized(EnumFacing side) {
        Optional<IRotationNode> connectedTo = getTileOnSideUnsynchronized(side);
        if (!connectedTo.isPresent()) {
            return false;
        }
        return getAngularVelocityRatioForUnsynchronized(side).isPresent() && connectedTo.get().getAngularVelocityRatioForUnsynchronized(side.getOpposite()).isPresent();
    }

    Optional<IRotationNode> getTileOnSideUnsynchronized(EnumFacing side);

    Optional<Double> getAngularVelocityRatioForUnsynchronized(EnumFacing side);
}
