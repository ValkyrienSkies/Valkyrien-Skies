package valkyrienwarfare.addon.control.block.torque;

import net.minecraft.world.WorldServer;
import valkyrienwarfare.physics.management.PhysicsObject;

import java.util.Optional;

public interface IRotationNodeProvider {

    /**
     * @return Optional.empty() if the rotation node isn't ready yet.
     */
    Optional<IRotationNode> getRotationNode();

}
