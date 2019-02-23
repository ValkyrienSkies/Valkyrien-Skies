package valkyrienwarfare.addon.control.block.torque;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import java.util.Optional;

public class ImplRotationNode<T extends TileEntity & IRotationNodeProvider> implements IRotationNode {

    private final T tileEntity;
    private final Optional<Double>[] angularVelocityRatios;
    private double angularTorque;
    private double angularVelocity;
    private double angularRotation;
    private boolean initialized;

    public ImplRotationNode(T entity) {
        this.tileEntity = entity;
        // Size 6 because there are 6 sides
        this.angularVelocityRatios = new Optional[] {Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()};
        this.angularTorque = 0;
        this.angularVelocity = 0;
        this.angularRotation = 0;
        this.initialized = false;
    }

    @Override
    public double calculateInstantaneousTorque() {
        // TODO: Implement
        return 0;
    }

    @Override
    public Optional<Double> getAngularVelocityRatioFor(EnumFacing side) {
        return angularVelocityRatios[side.ordinal()];
    }

    @Override
    public Optional<IRotationNode> getTileOnSide(EnumFacing side) {
        TileEntity sideTile = tileEntity.getWorld().getTileEntity(tileEntity.getPos().add(side.getDirectionVec()));
        if (!(sideTile instanceof IRotationNodeProvider)) {
            return Optional.empty();
        } else {
            return ((IRotationNodeProvider) sideTile).getRotationNode();
        }
    }

    @Override
    public double getAngularTorque() {
        return angularTorque;
    }

    @Override
    public double getAngularVelocity() {
        return angularVelocity;
    }

    @Override
    public double getAngularRotation() {
        return angularRotation;
    }

    @Override
    public void markInitialized() {
        this.initialized = true;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void setAngularVelocityRatio(EnumFacing side, Optional<Double> newRatio) {
        angularVelocityRatios[side.ordinal()] = newRatio;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        // TODO: Implement
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        // TODO: Implement
    }

}
