package org.valkyrienskies.fixes;

import java.util.Optional;
import org.valkyrienskies.mod.common.physics.management.PhysicsObject;

/**
 * Todo: Replace with a capability
 */
@Deprecated
public interface IPhysicsChunk {

    void setParentPhysicsObject(Optional<PhysicsObject> physicsObjectOptional);

    Optional<PhysicsObject> getPhysicsObjectOptional();

}
