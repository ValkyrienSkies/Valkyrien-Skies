package org.valkyrienskies.addon.control.nodenetwork;

import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;

public interface IForceTile {

    /**
     * Used to tell what direction of force an engine will output at a given instant.
     */
    Vector3dc getForceOutputNormal(double secondsToApply, PhysicsObject physicsObject);

    /**
     * Returns the current unoriented force output vector of this engine
     */
    default Vector3dc getForceOutputUnoriented(double secondsToApply, PhysicsObject physicsObject) {
        Vector3dc forceVectorNormal = getForceOutputNormal(secondsToApply, physicsObject);
        if (forceVectorNormal == null) {
            return new Vector3d();
        }
        double thrustMag = getThrustMagnitude(physicsObject) * secondsToApply;
        return forceVectorNormal.mul(thrustMag, new Vector3d());
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
     * @param physicsObject
     */
    double getThrustMagnitude(PhysicsObject physicsObject);

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
