package valkyrienwarfare.addon.control.block.torque.custom_torque_functions;

import valkyrienwarfare.addon.control.block.torque.IRotationNode;
import valkyrienwarfare.mod.common.physics.management.PhysicsObject;

/**
 * Torque functions are used by the rotation nodes to determine their torque each physics tick. They must not be
 * replaced by lambdas otherwise undefined behavior may occur.
 */
public class SimpleTorqueFunction {

    protected final IRotationNode rotationNode;

    public SimpleTorqueFunction(IRotationNode rotationNode) {
        this.rotationNode = rotationNode;
    }

    public double apply(PhysicsObject object) {
        return rotationNode.getAngularVelocity() * -.4 * rotationNode.getRotationalInertia();
    }

}
