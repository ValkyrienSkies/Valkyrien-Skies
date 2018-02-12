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
import valkyrienwarfare.addon.control.nodenetwork.IPhysicsProcessorNode;
import valkyrienwarfare.addon.control.nodenetwork.Node;
import valkyrienwarfare.api.RotationMatrices;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.physics.management.PhysicsObject;
import valkyrienwarfare.util.PhysicsSettings;

public class PhysicsCalculationsManualControl extends PhysicsCalculations {

    public double yawRate;
    public double forwardRate;
    public double upRate;

    public double setRoll = 0.01D;
    public double setPitch = 0.01D;

    public boolean useLinearMomentumForce;

    public PhysicsCalculationsManualControl(PhysicsObject toProcess) {
        super(toProcess);
    }

    public PhysicsCalculations downgradeToNormalCalculations() {
        PhysicsCalculations normalCalculations = new PhysicsCalculations(this);
        return normalCalculations;
    }

    @Override
    public void calculateForces() {
        double modifiedDrag = Math.pow(DRAG_CONSTANT, getPhysTickSpeed() / .05D);
        linearMomentum.multiply(modifiedDrag);
        angularVelocity.multiply(modifiedDrag);

        if (PhysicsSettings.doPhysicsBlocks) {
            for (Node node : parent.nodesWithinShip) {
                TileEntity nodeTile = node.getParentTile();
                if (nodeTile instanceof IPhysicsProcessorNode) {
//					System.out.println("test");
                    ((IPhysicsProcessorNode) nodeTile).onPhysicsTick(parent, this, physRawSpeed);
                }
            }
        }
    }

    @Override
    public void applyGravity() {

    }

    @Override
    public void rawPhysTickPostCol() {
        applyLinearVelocity();

        double previousYaw = parent.wrapper.yaw;

        applyAngularVelocity();

        //We don't want the up normal to exactly align with the world normal, it causes problems with collision

        if (!this.actAsArchimedes) {
            parent.wrapper.pitch = setPitch;
            parent.wrapper.roll = setRoll;
            parent.wrapper.yaw = previousYaw;
            parent.wrapper.yaw -= (yawRate * getPhysTickSpeed());
        }

        double[] existingRotationMatrix = RotationMatrices.getRotationMatrix(0, parent.wrapper.yaw, 0);

        Vector linearForce = new Vector(forwardRate, upRate, 0, existingRotationMatrix);

        if (useLinearMomentumForce) {
            linearForce = new Vector(linearMomentum, getInvMass());
        }

        linearForce.multiply(getPhysTickSpeed());

        parent.wrapper.posX += linearForce.X;
        parent.wrapper.posY += linearForce.Y;
        parent.wrapper.posZ += linearForce.Z;

        parent.coordTransform.updateAllTransforms();
    }

    @Override
    public void writeToNBTTag(NBTTagCompound compound) {
        super.writeToNBTTag(compound);
        compound.setDouble("yawRate", yawRate);
        compound.setDouble("forwardRate", forwardRate);
        compound.setDouble("upRate", upRate);
    }

    @Override
    public void readFromNBTTag(NBTTagCompound compound) {
        super.readFromNBTTag(compound);
        yawRate = compound.getDouble("yawRate");
//		forwardRate = compound.getDouble("forwardRate");
//		upRate = compound.getDouble("upRate");
    }

}
