package valkyrienwarfare.addon.control.block.torque;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import valkyrienwarfare.mod.multithreaded.VWThread;
import valkyrienwarfare.physics.management.PhysicsObject;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ImplRotationNode<T extends TileEntity & IRotationNodeProvider> implements IRotationNode {

    private final T tileEntity;
    private final Optional<Double>[] angularVelocityRatios;
    private double angularVelocity;
    private double angularRotation;
    private double rotationalInertia;
    private Optional<BlockPos> nodePos;
    private Optional<Function<PhysicsObject, Double>> customTorqueFunction;
    private boolean initialized;

    public ImplRotationNode(T entity) {
        this.tileEntity = entity;
        // Size 6 because there are 6 sides
        this.angularVelocityRatios = new Optional[] {Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()};
        this.angularVelocity = 0;
        this.angularRotation = 0;
        this.rotationalInertia = 1;
        this.nodePos = Optional.empty();
        this.customTorqueFunction = Optional.empty();
        this.initialized = false;
    }

    @PhysicsThreadOnly
    @Override
    public Optional<Function<PhysicsObject, Double>> getCustomTorqueFunction() {
        PhysicsAssert.assertPhysicsThread();
        return customTorqueFunction;
    }

    @PhysicsThreadOnly
    @Override
    public void setCustomTorqueFunction(Function<PhysicsObject, Double> customTorqueFunction) {
        PhysicsAssert.assertPhysicsThread();
        this.customTorqueFunction = Optional.of(customTorqueFunction);
    }

    @PhysicsThreadOnly
    @Override
    public Optional<Double> getAngularVelocityRatioFor(EnumFacing side) {
        PhysicsAssert.assertPhysicsThread();
        assertInitialized();
        return angularVelocityRatios[side.ordinal()];
    }

    @PhysicsThreadOnly
    @Override
    public Optional<IRotationNode> getTileOnSide(EnumFacing side) {
        PhysicsAssert.assertPhysicsThread();
        assertInitialized();
        BlockPos sideTilePos = nodePos.get().add(side.getDirectionVec());
        TileEntity sideTile = tileEntity.getWorld().getTileEntity(sideTilePos);
        if (!(sideTile instanceof IRotationNodeProvider)) {
            return Optional.empty();
        } else {
            return ((IRotationNodeProvider) sideTile).getRotationNode();
        }
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

    @PhysicsThreadOnly
    @Override
    public void setRotationalInertia(double newInertia) {
        PhysicsAssert.assertPhysicsThread();
        this.rotationalInertia = newInertia;
    }

    @PhysicsThreadOnly
    @Override
    public void resetNodeData() {
        PhysicsAssert.assertPhysicsThread();
        Arrays.fill(angularVelocityRatios, Optional.empty());
    }

    @Override
    public void markInitialized() {
        assert !initialized : "We cannot initialize the same node TWICE!";
        this.initialized = true;
        this.nodePos = Optional.of(tileEntity.getPos());
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @PhysicsThreadOnly
    @Override
    public void setAngularVelocityRatio(EnumFacing side, Optional<Double> newRatio) {
        PhysicsAssert.assertPhysicsThread();
        assertInitialized();
        angularVelocityRatios[side.ordinal()] = newRatio;
    }

    @PhysicsThreadOnly
    @Override
    public void setAngularVelocity(double angularVelocity) {
        this.angularVelocity = angularVelocity;
    }

    @PhysicsThreadOnly
    @Override
    public void setAngularRotation(double angularRotation) {
        this.angularRotation = angularRotation;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        // TODO: Implement
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        // TODO: Implement
    }

    @Override
    public Optional<BlockPos> getNodePos() {
        return nodePos;
    }

    private void assertInitialized() {
        assert isInitialized() : "We are not yet initialized!";
        assert nodePos.isPresent(): "There is NO node pos!";
    }

}
