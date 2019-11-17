/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2019 the Valkyrien Skies team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Skies team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Skies team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package org.valkyrienskies.addon.control.nodenetwork;

import net.minecraft.nbt.NBTTagCompound;
import org.valkyrienskies.mod.common.coordinates.VectorImmutable;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.management.PhysicsObject;
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
