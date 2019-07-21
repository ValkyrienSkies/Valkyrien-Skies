package valkyrienwarfare.fixes;

import valkyrienwarfare.mod.common.physics.management.PhysicsObject;

import java.util.Optional;

/**
 * Todo: Replace with a capability
 */
@Deprecated
public interface IPhysicsChunk {

    void setParentPhysicsObject(Optional<PhysicsObject> physicsObjectOptional);

    Optional<PhysicsObject> getPhysicsObjectOptional();

}
