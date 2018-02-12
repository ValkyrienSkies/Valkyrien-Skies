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

import valkyrienwarfare.api.RotationMatrices;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.math.Quaternion;
import valkyrienwarfare.physics.management.CoordTransformObject;
import valkyrienwarfare.physics.management.PhysicsObject;

public class PhysicsCalculationsOrbital extends PhysicsCalculations {

    public boolean isOrbitalPhased = true;
    private Vector setLinearVel = new Vector();
    private Vector setAngularVel = new Vector();

    public PhysicsCalculationsOrbital(PhysicsObject toProcess) {
        super(toProcess);
    }

    @Override
    public void processWorldCollision() {
        if (!isOrbitalPhased) {
            super.processWorldCollision();
        }
    }

    @Override
    public void calculateForces() {
        isOrbitalPhased = true;
        if (!isOrbitalPhased) {
            super.calculateForces();
        } else {
            double modifiedDrag = Math.pow(DRAG_CONSTANT, getPhysTickSpeed() / .05D);
            setLinearVel.multiply(modifiedDrag);
            setAngularVel.multiply(modifiedDrag);
        }
    }

    @Override
    public void addQueuedForces() {
        if (!isOrbitalPhased) {
            super.addQueuedForces();
        }
    }

    @Override
    public void applyLinearVelocity() {
        if (!isOrbitalPhased) {
            super.applyLinearVelocity();
        } else {
            parent.wrapper.posX += getPhysTickSpeed() * setLinearVel.X;
            parent.wrapper.posY += getPhysTickSpeed() * setLinearVel.Y;
            parent.wrapper.posZ += getPhysTickSpeed() * setLinearVel.Z;
        }
    }

    @Override
    public void applyAngularVelocity() {
        if (!isOrbitalPhased) {
            super.applyAngularVelocity();
        } else {
            CoordTransformObject coordTrans = parent.coordTransform;
            double[] rotationChange = RotationMatrices.getRotationMatrix(setAngularVel.X, setAngularVel.Y, setAngularVel.Z, angularVelocity.length() * getPhysTickSpeed());
            Quaternion transform = Quaternion.QuaternionFromMatrix(RotationMatrices.getMatrixProduct(rotationChange, coordTrans.lToWRotation));
            double[] radians = transform.toRadians();

            wrapperEnt.pitch = Double.isNaN(radians[0]) ? 0.0f : (float) Math.toDegrees(radians[0]);
            wrapperEnt.yaw = Double.isNaN(radians[1]) ? 0.0f : (float) Math.toDegrees(radians[1]);
            wrapperEnt.roll = Double.isNaN(radians[2]) ? 0.0f : (float) Math.toDegrees(radians[2]);
            coordTrans.updateAllTransforms();
        }
    }

    public void setLinearVel(Vector newLinearVel) {
        setLinearVel = newLinearVel;
    }

    public void setAngularVel(Vector newAngularVel) {
        setAngularVel = newAngularVel;
    }

}
