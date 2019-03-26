package valkyrienwarfare.addon.control.block.torque.custom_torque_functions;

import valkyrienwarfare.addon.control.block.torque.IRotationNode;
import valkyrienwarfare.physics.management.PhysicsObject;

public class EtherEngineTorqueFunction extends SimpleTorqueFunction {

    public EtherEngineTorqueFunction(IRotationNode rotationNode) {
        super(rotationNode);
    }

    @Override
    public double apply(PhysicsObject object) {
        return 3 - rotationNode.getAngularVelocity();
    }
}
