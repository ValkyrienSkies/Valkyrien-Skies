package org.valkyrienskies.addon.control.nodenetwork;

import org.valkyrienskies.mod.common.coordinates.VectorImmutable;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;

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
