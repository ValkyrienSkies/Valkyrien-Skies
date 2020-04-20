package org.valkyrienskies.addon.control.block.torque.custom_torque_functions;

import org.valkyrienskies.addon.control.block.torque.IRotationNode;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;

public class ValkyriumEngineTorqueFunction extends SimpleTorqueFunction {

    public static final double MAX_TORQUE = 10000; // Units, Newtons * meters
    public static final double MAX_ROTATION_VELOCITY = Math.PI; // Units, Radians / second
    public static final double SLOWDOWN_RATIO = 0; // Unit-less, must be between 0 and 1.

    public ValkyriumEngineTorqueFunction(IRotationNode rotationNode) {
        super(rotationNode);
    }

    /**
     * Calculate torque using a torque vs rotational velocity curve.
     *
     * @param object
     * @return
     */
    @Override
    public double calculateTorque(PhysicsObject object) {
        double currentRotVel = rotationNode.getAngularVelocity();

        // Engine reverse polarity, engine should break, but for now just return the same amount of
        // torque.
        if (currentRotVel < 0) {
            return 100;
        }

        // In this case the engine is rotation velocity limited.
        if (currentRotVel > MAX_ROTATION_VELOCITY) {
            return 0;
        } else if (currentRotVel > SLOWDOWN_RATIO * MAX_ROTATION_VELOCITY) {
            // Use a line to calculate new torque.
            double ratio = currentRotVel / MAX_ROTATION_VELOCITY;
            double outputTorqueRatio = 1 - ((ratio - SLOWDOWN_RATIO) / (1 - SLOWDOWN_RATIO));
            if (outputTorqueRatio < -.99 || outputTorqueRatio > 1.01) {
                throw new IllegalStateException("Error calculating ether engine torque function");
            }
            return outputTorqueRatio * MAX_TORQUE;
        } else {
            return MAX_TORQUE;
        }
    }
}
