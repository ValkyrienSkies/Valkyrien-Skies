package valkyrienwarfare.addon.control.block.torque;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import valkyrienwarfare.physics.management.PhysicsObject;

import java.util.*;

public interface IRotationNode extends Comparator<IRotationNode> {

    double calculateInstantaneousTorque(PhysicsObject parent);

    default boolean isConnectedToSide(EnumFacing side) {
        return getAngularVelocityRatioFor(side).isPresent();
    }

    Optional<Double> getAngularVelocityRatioFor(EnumFacing side);

    /**
     * The optional is only present if there is an attached tile for that side.
     * @param side
     * @return
     */
    Optional<IRotationNode> getTileOnSide(EnumFacing side);

    double getAngularTorque();

    double getAngularVelocity();

    double getAngularRotation();

    double getRotationalInertia();

    void setRotationalInertia(double newInertia);

    void markInitialized();

    boolean isInitialized();

    void setAngularVelocityRatio(EnumFacing side, Optional<Double> newRatio);

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

    default List<IRotationNode> connectedTorqueTilesIterator() {
        List<IRotationNode> connectedTiles = new ArrayList<>();
        for (EnumFacing facing : EnumFacing.values()) {
            if (isConnectedToSide(facing)) {
                connectedTiles.add(getTileOnSide(facing).get());
            }
        }
        return connectedTiles;
    }
}
