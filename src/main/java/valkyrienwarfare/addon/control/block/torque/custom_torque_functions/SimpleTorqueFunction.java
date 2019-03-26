package valkyrienwarfare.addon.control.block.torque.custom_torque_functions;

import valkyrienwarfare.addon.control.block.torque.IRotationNode;
import valkyrienwarfare.physics.management.PhysicsObject;

public class SimpleTorqueFunction {

    protected final IRotationNode rotationNode;

    public SimpleTorqueFunction(IRotationNode rotationNode) {
        this.rotationNode = rotationNode;
    }

    public double apply(PhysicsObject object) {
        return rotationNode.getAngularVelocity() * -.4 * rotationNode.getRotationalInertia();
    }

}
