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

package valkyrienwarfare.physics.calculations;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import valkyrienwarfare.addon.control.nodenetwork.INodePhysicsProcessor;
import valkyrienwarfare.addon.control.nodenetwork.Node;
import valkyrienwarfare.api.RotationMatrices;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.physics.management.PhysicsObject;
import valkyrienwarfare.util.PhysicsSettings;

public class PhysicsCalculationsManualControl extends PhysicsCalculations {

    private boolean useLinearMomentumForce;
    private double yawRate;
    private double forwardRate;
    private double upRate;

    public PhysicsCalculationsManualControl(PhysicsObject toProcess) {
        super(toProcess);
    }

    public PhysicsCalculations downgradeToNormalCalculations() {
        PhysicsCalculations normalCalculations = new PhysicsCalculations(this);
        return normalCalculations;
    }

    @Override
    public void calculateForces() {
        applyAirDrag();

        if (PhysicsSettings.doPhysicsBlocks) {
            for (Node node : parent.nodesWithinShip) {
                TileEntity nodeTile = node.getParentTile();
                if (nodeTile instanceof INodePhysicsProcessor) {
//					System.out.println("test");
                    ((INodePhysicsProcessor) nodeTile).onPhysicsTick(parent, this, this.getPhysicsTimeDeltaPerPhysTick());
                }
            }
        }
    }

    @Override
    public void rawPhysTickPostCol() {
        applyLinearVelocity();
        double previousYaw = parent.wrapper.getYaw();
        applyAngularVelocity();

        //We don't want the up normal to exactly align with the world normal, it causes problems with collision
        if (!this.actAsArchimedes) {
            parent.wrapper.setPitch(0.01F);
            parent.wrapper.setRoll(0.01F);
            parent.wrapper.setYaw(previousYaw);
            parent.wrapper.setYaw(parent.wrapper.getYaw() - (getYawRate() * getPhysicsTimeDeltaPerPhysTick()));
        }

        double[] existingRotationMatrix = RotationMatrices.getRotationMatrix(0, parent.wrapper.getYaw(), 0);
        Vector linearForce = new Vector(getForwardRate(), getUpRate(), 0, existingRotationMatrix);
        if (isUseLinearMomentumForce()) {
            linearForce = new Vector(linearMomentum, getInvMass());
        }

        linearForce.multiply(getPhysicsTimeDeltaPerPhysTick());

        parent.wrapper.posX += linearForce.X;
        parent.wrapper.posY += linearForce.Y;
        parent.wrapper.posZ += linearForce.Z;

        parent.coordTransform.updateAllTransforms(true, true);
    }

    @Override
    public void writeToNBTTag(NBTTagCompound compound) {
        super.writeToNBTTag(compound);
        compound.setDouble("yawRate", getYawRate());
        compound.setDouble("forwardRate", getForwardRate());
        compound.setDouble("upRate", getUpRate());
    }

    @Override
    public void readFromNBTTag(NBTTagCompound compound) {
        super.readFromNBTTag(compound);
        setYawRate(compound.getDouble("yawRate"));
		setForwardRate(compound.getDouble("forwardRate"));
		setUpRate(compound.getDouble("upRate"));
    }

    public boolean isUseLinearMomentumForce() {
        return useLinearMomentumForce;
    }

    public void setUseLinearMomentumForce(boolean useLinearMomentumForce) {
        this.useLinearMomentumForce = useLinearMomentumForce;
    }

    public double getUpRate() {
        return upRate;
    }

    public void setUpRate(double upRate) {
        this.upRate = upRate;
    }

    public double getYawRate() {
        return yawRate;
    }

    public void setYawRate(double yawRate) {
        this.yawRate = yawRate;
    }

    public double getForwardRate() {
        return forwardRate;
    }

    public void setForwardRate(double forwardRate) {
        this.forwardRate = forwardRate;
    }

}
