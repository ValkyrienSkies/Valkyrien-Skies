package org.valkyrienskies.addon.control.nodenetwork;

import net.minecraft.nbt.NBTTagCompound;
import org.valkyrienskies.mod.common.coordinates.VectorImmutable;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;
import org.valkyrienskies.mod.common.util.ValkyrienNBTUtils;

public abstract class BasicForceNodeTileEntity extends BasicNodeTileEntity implements IForceTile {

    protected double maxThrust;
    protected double currentThrust;
    private double thrusGoalMultiplier;
    private Vector forceOutputVector;
    private Vector normalVelocityUnoriented;
    private int ticksSinceLastControlSignal;
    // Tells if the tile is in Ship Space, if it isn't then it doesn't try to find a
    // parent Ship object
    private boolean hasAlreadyCheckedForParent;

    /**
     * Only used for the NBT creation, other <init> calls should go through the other constructors
     * first
     */
    public BasicForceNodeTileEntity() {
        this.maxThrust = 5000D;
        this.currentThrust = 0D;
        this.thrusGoalMultiplier = 0D;
        this.forceOutputVector = new Vector();
        this.ticksSinceLastControlSignal = 0;
        this.hasAlreadyCheckedForParent = false;
    }

    public BasicForceNodeTileEntity(Vector normalVelocityUnoriented, boolean isForceOutputOriented,
        double maxThrust) {
        this();
        this.normalVelocityUnoriented = normalVelocityUnoriented;
        this.maxThrust = maxThrust;
    }

    /**
     * True for all engines except for Valkyrium Compressors
     */
    public boolean isForceOutputOriented() {
        return true;
    }

    @Override
    public VectorImmutable getForceOutputNormal(double secondsToApply, PhysicsObject object) {
        return normalVelocityUnoriented.toImmutable();
    }

    @Override
    public double getMaxThrust() {
        return maxThrust;
    }

    public void setMaxThrust(double maxThrust) {
        this.maxThrust = maxThrust;
    }

    @Override
    public double getThrustMagnitude() {
        return this.getMaxThrust() * this.getThrustMultiplierGoal();
    }

    @Override
    public double getThrustMultiplierGoal() {
        return thrusGoalMultiplier;
    }

    @Override
    public void setThrustMultiplierGoal(double multiplier) {
        thrusGoalMultiplier = multiplier;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        maxThrust = compound.getDouble("maxThrust");
        currentThrust = compound.getDouble("currentThrust");
        normalVelocityUnoriented = ValkyrienNBTUtils
            .readVectorFromNBT("normalVelocityUnoriented", compound);
        ticksSinceLastControlSignal = compound.getInteger("ticksSinceLastControlSignal");
        super.readFromNBT(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setDouble("maxThrust", maxThrust);
        compound.setDouble("currentThrust", currentThrust);
        ValkyrienNBTUtils
            .writeVectorToNBT("normalVelocityUnoriented", normalVelocityUnoriented, compound);
        compound.setInteger("ticksSinceLastControlSignal", ticksSinceLastControlSignal);
        return super.writeToNBT(compound);
    }

    /**
     * Returns false if a parent Ship exists, and true if otherwise
     */
    public boolean updateParentShip() {
        return true;
    }

    public void updateTicksSinceLastRecievedSignal() {
        ticksSinceLastControlSignal = 0;
    }

    @Override
    public void update() {
        super.update();
        ticksSinceLastControlSignal++;
        if (ticksSinceLastControlSignal > 5 && getThrustMultiplierGoal() != 0) {
            setThrustMultiplierGoal(this.getThrustMultiplierGoal() * .9D);
        }
    }

}
