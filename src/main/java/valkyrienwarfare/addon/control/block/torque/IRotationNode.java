package valkyrienwarfare.addon.control.block.torque;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import valkyrienwarfare.physics.management.PhysicsObject;

import java.util.*;
import java.util.function.Function;

public interface IRotationNode extends Comparator<IRotationNode> {

    @PhysicsThreadOnly
    default double calculateInstantaneousTorque(PhysicsObject parent) {
        if (!getCustomTorqueFunction().isPresent()) {
            // Default friction calculation
            return getAngularVelocity() * -.1 * getRotationalInertia();
        } else {
            return getCustomTorqueFunction().get().apply(parent);
        }
    }

    @PhysicsThreadOnly
    void setCustomTorqueFunction(Function<PhysicsObject, Double> customTorqueFunction);

    @PhysicsThreadOnly
    Optional<Function<PhysicsObject, Double>> getCustomTorqueFunction();

    @PhysicsThreadOnly
    default boolean isConnectedToSide(EnumFacing side) {
        return getAngularVelocityRatioFor(side).isPresent();
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
    default int compare(IRotationNode o1, IRotationNode o2) {
        return o1.getSortingPriority() - o2.getSortingPriority();
    }

    @PhysicsThreadOnly
    default List<IRotationNode> connectedTorqueTilesList() {
        List<IRotationNode> connectedTiles = new ArrayList<>();
        for (EnumFacing facing : EnumFacing.values()) {
            if (isConnectedToSide(facing)) {
                connectedTiles.add(getTileOnSide(facing).get());
            }
        }
        return connectedTiles;
    }

    Optional<BlockPos> getNodePos();
}
