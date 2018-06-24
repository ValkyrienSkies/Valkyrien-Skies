/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2018 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package valkyrienwarfare.addon.control.nodenetwork;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.math.Vector;
import valkyrienwarfare.mod.coordinates.TransformType;
import valkyrienwarfare.physics.PhysicsCalculations;
import valkyrienwarfare.physics.management.PhysicsObject;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;
import valkyrienwarfare.util.NBTUtils;

public abstract class BasicForceNodeTileEntity extends BasicNodeTileEntity implements IForceTile {

    protected double maxThrust;
    protected double currentThrust;
    private Vector forceOutputVector;
    private Vector normalVelocityUnoriented;
    private int ticksSinceLastControlSignal;
    // Tells if the tile is in Ship Space, if it isn't then it doesn't try to find a
    // parent Ship object
    private boolean hasAlreadyCheckedForParent;

    /**
     * Only used for the NBT creation, other <init> calls should go through the
     * other constructors first
     */
    public BasicForceNodeTileEntity() {
        this.maxThrust = 5000D;
        this.currentThrust = 0D;
        this.forceOutputVector = new Vector();
        this.ticksSinceLastControlSignal = 0;
        this.hasAlreadyCheckedForParent = false;
    }

    public BasicForceNodeTileEntity(Vector normalVeclocityUnoriented, boolean isForceOutputOriented, double maxThrust) {
        this();
        this.normalVelocityUnoriented = normalVeclocityUnoriented;
        this.maxThrust = maxThrust;
    }

    /**
     * True for all engines except for Ether Compressors
     *
     * @return
     */
    public boolean isForceOutputOriented() {
        return true;
    }

    @Override
    public Vector getForceOutputNormal() {
        // TODO Auto-generated method stub
        return normalVelocityUnoriented;
    }

    @Override
    public Vector getForceOutputUnoriented(double secondsToApply, PhysicsObject physicsObject) {
        return normalVelocityUnoriented.getProduct(currentThrust * secondsToApply);
    }

    @Override
    public Vector getForceOutputOriented(double secondsToApply, PhysicsObject physicsObject) {
        Vector outputForce = getForceOutputUnoriented(secondsToApply, physicsObject);
        if (isForceOutputOriented()) {
            if (updateParentShip()) {
                getNode().getPhysicsObject().getShipTransformationManager().getCurrentTickTransform().rotate(outputForce, TransformType.SUBSPACE_TO_GLOBAL);
//                RotationMatrices.doRotationOnly(getNode().getPhysicsObject().coordTransform.lToWTransform, outputForce);
            }
        }
        return outputForce;
    }

    @Override
    public double getMaxThrust() {
        return maxThrust;
    }

    @Override
    public double getThrustActual() {
        return currentThrust;
    }

    @Override
    public double getThrustGoal() {
        return currentThrust;
    }

    @Override
    public void setThrustGoal(double newMagnitude) {
        currentThrust = newMagnitude;
    }

    @Override
    public Vector getPositionInLocalSpaceWithOrientation() {
        if (updateParentShip()) {
            return null;
        }
        PhysicsWrapperEntity parentShip = getNode().getPhysicsObject().getWrapperEntity();
        Vector engineCenter = new Vector(getPos().getX() + .5D, getPos().getY() + .5D, getPos().getZ() + .5D);
//        RotationMatrices.applyTransform(parentShip.wrapping.coordTransform.lToWTransform, engineCenter);
        parentShip.getPhysicsObject().getShipTransformationManager().getCurrentTickTransform().transform(engineCenter, TransformType.SUBSPACE_TO_GLOBAL);
        engineCenter.subtract(parentShip.posX, parentShip.posY, parentShip.posZ);
        return engineCenter;
    }

    @Override
    public Vector getVelocityAtEngineCenter() {
        if (updateParentShip()) {
            return null;
        }
        PhysicsCalculations calculations = getNode().getPhysicsObject().getPhysicsProcessor();
        return calculations.getVelocityAtPoint(getPositionInLocalSpaceWithOrientation());
    }

    @Override
    public Vector getLinearVelocityAtEngineCenter() {
        if (updateParentShip()) {
            return null;
        }
        PhysicsCalculations calculations = getNode().getPhysicsObject().getPhysicsProcessor();
        return calculations.linearMomentum;
    }

    @Override
    public Vector getAngularVelocityAtEngineCenter() {
        if (updateParentShip()) {
            return null;
        }
        PhysicsCalculations calculations = getNode().getPhysicsObject().getPhysicsProcessor();
        return calculations.angularVelocity.cross(getPositionInLocalSpaceWithOrientation());
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        maxThrust = compound.getDouble("maxThrust");
        currentThrust = compound.getDouble("currentThrust");
        normalVelocityUnoriented = NBTUtils.readVectorFromNBT("normalVelocityUnoriented", compound);
        ticksSinceLastControlSignal = compound.getInteger("ticksSinceLastControlSignal");
        super.readFromNBT(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setDouble("maxThrust", maxThrust);
        compound.setDouble("currentThrust", currentThrust);
        NBTUtils.writeVectorToNBT("normalVelocityUnoriented", normalVelocityUnoriented, compound);
        compound.setInteger("ticksSinceLastControlSignal", ticksSinceLastControlSignal);
        return super.writeToNBT(compound);
    }

    /**
     * Returns false if a parent Ship exists, and true if otherwise
     *
     * @return
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
        if (ticksSinceLastControlSignal > 5) {
            setThrustGoal(getThrustActual() * .9D);
        }
    }

}
