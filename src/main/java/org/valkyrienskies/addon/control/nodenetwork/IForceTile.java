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

import org.valkyrienskies.mod.common.coordinates.VectorImmutable;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.management.PhysicsObject;

public interface IForceTile {

    /**
     * Used to tell what direction of force an engine will output at a given instant.
     */
    VectorImmutable getForceOutputNormal(double secondsToApply, PhysicsObject physicsObject);

    /**
     * Returns the current unoriented force output vector of this engine
     */
    default Vector getForceOutputUnoriented(double secondsToApply, PhysicsObject physicsObject) {
        VectorImmutable forceVectorNormal = getForceOutputNormal(secondsToApply, physicsObject);
        if (forceVectorNormal == null) {
            return new Vector();
        }
        Vector forceVector = new Vector(forceVectorNormal);
        forceVector.multiply(getThrustMagnitude() * secondsToApply);
        return forceVector;
    }

    /**
     * Returns the maximum magnitude of force this engine can provide at this instant under its
     * current conditions. This number should never be cached in any way is it is can always
     * change.
     */
    double getMaxThrust();

    void setMaxThrust(double maxThrust);

    /**
     * Returns magnitude of thrust in Newtons being produced.
     */
    double getThrustMagnitude();

    /**
     * Returns the current force multiplier goal.
     */
    double getThrustMultiplierGoal();

    /**
     * Sets the goal for the force output, multiplier must be between 0 and 1. The actual goal
     * thrust is the getMaxThrust() * getThrustMultiplierGoal();
     */
    void setThrustMultiplierGoal(double thrustMultiplierGoal);

}
