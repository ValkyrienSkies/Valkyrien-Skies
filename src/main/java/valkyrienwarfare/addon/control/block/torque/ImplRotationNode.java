package valkyrienwarfare.addon.control.block.torque;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import valkyrienwarfare.physics.management.PhysicsObject;

import java.util.Arrays;
import java.util.Optional;

public class ImplRotationNode<T extends TileEntity & IRotationNodeProvider> implements IRotationNode {

    private final T tileEntity;
    private final Optional<Double>[] angularVelocityRatios;
    private double angularTorque;
    private double angularVelocity;
    private double angularRotation;
    private double rotationalInertia;
    private boolean initialized;

    public ImplRotationNode(T entity) {
        this.tileEntity = entity;
        // Size 6 because there are 6 sides
        this.angularVelocityRatios = new Optional[] {Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()};
        this.angularTorque = 0;
        this.angularVelocity = 0;
        this.angularRotation = 0;
        this.rotationalInertia = 0;
        this.initialized = false;
    }

    @Override
    public double calculateInstantaneousTorque(PhysicsObject parent) {
        assertInitialized();
        return tileEntity.calculateInstantaneousTorque(parent);
    }

    @Override
    public Optional<Double> getAngularVelocityRatioFor(EnumFacing side) {
        assertInitialized();
        return angularVelocityRatios[side.ordinal()];
    }

    @Override
    public Optional<IRotationNode> getTileOnSide(EnumFacing side) {
        assertInitialized();
        TileEntity sideTile = tileEntity.getWorld().getTileEntity(tileEntity.getPos().add(side.getDirectionVec()));
        if (!(sideTile instanceof IRotationNodeProvider)) {
            return Optional.empty();
        } else {
            return ((IRotationNodeProvider) sideTile).getRotationNode();
        }
    }

    @Override
    public double getAngularTorque() {
        assertInitialized();
        return angularTorque;
    }

    @Override
    public double getAngularVelocity() {
        assertInitialized();
        return angularVelocity;
    }

    @Override
    public double getAngularRotation() {
        assertInitialized();
        return angularRotation;
    }

    @Override
    public double getRotationalInertia() {
        assertInitialized();
        return rotationalInertia;
    }

    @Override
    public void setRotationalInertia(double newInertia) {
        this.rotationalInertia = newInertia;
    }

    @Override
    public void resetNodeData() {
        Arrays.fill(angularVelocityRatios, Optional.empty());
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
        assertInitialized();
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

    private void assertInitialized() {
        assert isInitialized() : "We are not yet initialized!";
    }

}
