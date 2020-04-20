package org.valkyrienskies.addon.control.block.torque.custom_torque_functions;

import org.valkyrienskies.addon.control.block.torque.IRotationNode;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;

/**
 * Torque functions are used by the rotation nodes to determine their torque each physics tick. They
 * must not be replaced by lambdas otherwise undefined behavior may occur.
 */
public class SimpleTorqueFunction {

    public static final double DEFAULT_ANGULAR_FRICTION = .4;
    protected final IRotationNode rotationNode;

    public SimpleTorqueFunction(IRotationNode rotationNode) {
        this.rotationNode = rotationNode;
    }

    /**
     * Calculate the torque for the rotationNode this function manages. This method gets executed by
     * the physics thread and should not reference anything outside of rotationNode for the sake of
     * avoiding race conditions.
     *
     * @param object The PhysicsObject that rotationNode lives in.
     * @return The torque created by this node.
     */
    public double calculateTorque(PhysicsObject object) {
        // Default torque implementation.
        return rotationNode.getAngularVelocity() * rotationNode.getRotationalInertia()
            * DEFAULT_ANGULAR_FRICTION;
    }

}
