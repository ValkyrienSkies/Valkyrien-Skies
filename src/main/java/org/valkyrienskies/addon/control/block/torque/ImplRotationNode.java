package org.valkyrienskies.addon.control.block.torque;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.valkyrienskies.addon.control.block.torque.custom_torque_functions.SimpleTorqueFunction;
import org.valkyrienskies.addon.control.block.torque.custom_torque_functions.ValkyriumEngineTorqueFunction;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;

public class ImplRotationNode<T extends TileEntity & IRotationNodeProvider> implements
    IRotationNode {

    private final T tileEntity;
    private final Optional<Double>[] angularVelocityRatios;
    private final ConcurrentLinkedQueue<Runnable> queuedTasks;
    private double angularVelocity;
    private double angularRotation;
    private double rotationalInertia;
    private Optional<BlockPos> nodePos;
    private Optional<SimpleTorqueFunction> customTorqueFunction;
    private boolean initialized;
    private AtomicBoolean markedForDeletion;
    private AtomicBoolean hasBeenPlacedIntoNodeWorld;
    private int sortingPriority;

    public ImplRotationNode(T entity, double rotationalInertia) {
        this.tileEntity = entity;
        // Size 6 because there are 6 sides
        this.angularVelocityRatios = new Optional[]{Optional.empty(), Optional.empty(),
            Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()};
        this.angularVelocity = 0;
        this.angularRotation = 0;
        this.rotationalInertia = rotationalInertia;
        this.nodePos = Optional.empty();
        this.customTorqueFunction = Optional.empty();
        this.initialized = false;
        this.queuedTasks = new ConcurrentLinkedQueue<>();
        this.markedForDeletion = new AtomicBoolean(false);
        this.hasBeenPlacedIntoNodeWorld = new AtomicBoolean(false);
        this.sortingPriority = 0;
    }

    public ImplRotationNode(T entity, double rotationalInertia, int sortingPriority) {
        this(entity, rotationalInertia);
        this.setSortingPriority(sortingPriority);
    }

    @PhysicsThreadOnly
    @Override
    public Optional<SimpleTorqueFunction> getCustomTorqueFunction() {
        PhysicsAssert.assertPhysicsThread();
        return customTorqueFunction;
    }

    @PhysicsThreadOnly
    @Override
    public void setCustomTorqueFunction(SimpleTorqueFunction customTorqueFunction) {
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

        TileEntity sideTile;
        try {
            // This is in case of a race condition that results form world.removeTileEntity() and
            // world.getTileEntity() being called at the same time.
            sideTile = ValkyrienUtils.getTileEntitySafe(tileEntity.getWorld(), sideTilePos);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
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

    @PhysicsThreadOnly
    @Override
    public void setAngularVelocity(double angularVelocity) {
        this.angularVelocity = angularVelocity;
    }

    @Override
    public double getAngularRotation() {
        assertInitialized();
        return angularRotation;
    }

    @PhysicsThreadOnly
    @Override
    public void setAngularRotation(double angularRotation) {
        this.angularRotation = angularRotation;
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

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        compound.setFloat("a_vel", (float) angularVelocity);
        compound.setFloat("a_pos", (float) angularRotation);
        compound.setFloat("a_inert", (float) rotationalInertia);
        compound.setBoolean("a_has_pos", getNodePos().isPresent());
        compound.setInteger("a_sort_prior", getSortingPriority());
        if (getNodePos().isPresent()) {
            compound.setInteger("a_posX", getNodePos().get().getX());
            compound.setInteger("a_posY", getNodePos().get().getY());
            compound.setInteger("a_posZ", getNodePos().get().getZ());
        }

        for (int i = 0; i < 6; i++) {
            if (angularVelocityRatios[i].isPresent()) {
                compound.setFloat("a_vel_ratios_" + i, angularVelocityRatios[i].get().floatValue());
            }
        }

        if (customTorqueFunction.isPresent()) {
            this.customTorqueFunction = Optional.of(new ValkyriumEngineTorqueFunction(this));
            compound
                .setString("custom_torque_funct", customTorqueFunction.get().getClass().getName());
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        this.angularVelocity = compound.getFloat("a_vel");
        this.angularRotation = compound.getFloat("a_pos");
        this.rotationalInertia = compound.getFloat("a_inert");

        if (compound.hasKey("a_sort_prior")) {
            this.setSortingPriority(compound.getInteger("a_sort_prior"));
        }

        if (compound.getBoolean("a_has_pos")) {
            this.nodePos = Optional
                .of(new BlockPos(compound.getInteger("a_posX"), compound.getInteger("a_posY"),
                    compound.getInteger("a_posZ")));
        }

        for (int i = 0; i < 6; i++) {
            if (compound.hasKey("a_vel_ratios_" + i)) {
                angularVelocityRatios[i] = Optional
                    .of((double) compound.getFloat("a_vel_ratios_" + i));
            }
        }

        if (compound.hasKey("custom_torque_funct")) {
            String className = compound.getString("custom_torque_funct");
            try {
                Class<?> c = Class.forName(className);
                Constructor<?> cons = c.getConstructor(IRotationNode.class);
                Object customTorqueFunction = cons.newInstance(this);
                this.customTorqueFunction = Optional
                    .of((SimpleTorqueFunction) customTorqueFunction);
            } catch (Exception e) {
                System.err.println("Failed to load class: " + className);
                e.printStackTrace();
            }
        }
//        this.initialized = true;
    }

    @Override
    public Optional<BlockPos> getNodePos() {
        return nodePos;
    }

    @Override
    public void queueTask(Runnable task) {
        queuedTasks.add(task);
    }

    @Override
    public ConcurrentLinkedQueue<Runnable> getQueuedTasks() {
        return queuedTasks;
    }

    @Override
    public double getAngularRotationUnsynchronized() {
        return angularRotation;
    }

    private void assertInitialized() {
        assert isInitialized() : "We are not yet initialized!";
        assert nodePos.isPresent() : "There is NO node pos!";
    }

    @Override
    public void queueNodeForDeletion() {
        this.markedForDeletion.set(true);
    }

    @Override
    public boolean markedForDeletion() {
        return markedForDeletion.get();
    }

    @Override
    public double getAngularVelocityUnsynchronized() {
        return this.angularVelocity;
    }

    @Override
    public boolean hasBeenPlacedIntoNodeWorld() {
        return hasBeenPlacedIntoNodeWorld.get();
    }

    @Override
    public void setPlacedIntoNodeWorld(boolean status) {
        hasBeenPlacedIntoNodeWorld.set(status);
    }

    @Override
    public Optional<Double>[] connectedRotationRatiosUnsychronized() {
        return angularVelocityRatios.clone();
    }

    @Override
    public Optional<IRotationNode> getTileOnSideUnsynchronized(EnumFacing side) {
        assertInitialized();
        if (!nodePos.isPresent()) {
            return Optional.empty();
        }
        BlockPos sideTilePos = nodePos.get().add(side.getDirectionVec());
        TileEntity sideTile = tileEntity.getWorld().getTileEntity(sideTilePos);
        if (!(sideTile instanceof IRotationNodeProvider)) {
            return Optional.empty();
        } else {
            return ((IRotationNodeProvider) sideTile).getRotationNode();
        }
    }

    @Override
    public Optional<Double> getAngularVelocityRatioForUnsynchronized(EnumFacing side) {
        assertInitialized();
        return angularVelocityRatios[side.ordinal()];
    }

    @PhysicsThreadOnly
    @Override
    public int getSortingPriority() {
        return this.sortingPriority;
    }

    @PhysicsThreadOnly
    @Override
    public void setSortingPriority(int newPriority) {
        this.sortingPriority = newPriority;
    }
}
