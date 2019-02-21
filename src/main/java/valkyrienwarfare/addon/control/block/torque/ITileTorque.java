package valkyrienwarfare.addon.control.block.torque;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import java.util.Comparator;
import java.util.Optional;

public interface ITileTorque extends Comparator<ITileTorque> {

    double calculateInstantaneousTorque();

    Optional<Double> getAngularVelocityRatioFor(EnumFacing side);

    ITileTorque getTileOnSide(EnumFacing side);

    boolean hasTileOnSide(EnumFacing side);

    double getAngularTorque();

    double getAngularVelocity();

    double getAngularRotation();

    void writeToNBT(NBTTagCompound compound);

    void readFromNBT(NBTTagCompound compound);

    /**
     * Higher values are more likely to be chosen for building the torque network.
     */
    default int getSortingPriority() {
        return 0;
    }

    @Override
    default int compare(ITileTorque o1, ITileTorque o2) {
        return o1.getSortingPriority() - o2.getSortingPriority();
    }
}
