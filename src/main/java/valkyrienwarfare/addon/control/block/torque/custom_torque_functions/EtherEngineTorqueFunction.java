package valkyrienwarfare.addon.control.block.torque.custom_torque_functions;

import valkyrienwarfare.addon.control.block.torque.IRotationNode;
import valkyrienwarfare.physics.management.PhysicsObject;

public class EtherEngineTorqueFunction extends SimpleTorqueFunction {

    public EtherEngineTorqueFunction(IRotationNode rotationNode) {
        super(rotationNode);
    }

    @Override
    public double apply(PhysicsObject object) {
        // Engines will try to rotate 180 degrees every second.
        return 100 * (Math.PI - rotationNode.getAngularVelocity());
    }
}
