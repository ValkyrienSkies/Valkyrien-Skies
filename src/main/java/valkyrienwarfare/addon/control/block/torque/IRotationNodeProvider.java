package valkyrienwarfare.addon.control.block.torque;

import net.minecraft.world.WorldServer;
import valkyrienwarfare.physics.management.PhysicsObject;

import java.util.Optional;

public interface IRotationNodeProvider {

    /**
     * @return Optional.empty() if the rotation node isn't ready yet.
     */
    Optional<IRotationNode> getRotationNode();

    /**
     * By default this will just simulate applying friction to the system. This must NOT be run on the game tick thread.
     *
     * @param parent
     * @return
     */
    default double calculateInstantaneousTorque(PhysicsObject parent) {
        return calculateInstantaneousTorqueFromFriction(parent);
    }

    /**
     * This probably should not be overriden unless you're changing the way rotational friction is calculated.
     * @param parent
     * @return
     */
    default double calculateInstantaneousTorqueFromFriction(PhysicsObject parent) {
        assert !parent.getWorldObj().isRemote : "Client should not be calculating this!";
        assert !((WorldServer) parent.getWorldObj()).getMinecraftServer().isCallingFromMinecraftThread() : "This should NEVER be called on the game thread!";
        Optional<IRotationNode> rotationNodeOptional = getRotationNode();
        assert rotationNodeOptional.isPresent() : "How the heck did this get called without a rotation node present?";
        IRotationNode rotationNode = rotationNodeOptional.get();
        return rotationNode.getAngularVelocity() * -.1 * rotationNode.getRotationalInertia();
    }
}
