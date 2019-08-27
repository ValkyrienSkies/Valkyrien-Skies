package org.valkyrienskies.fixes;

import org.valkyrienskies.mod.common.physics.management.PhysicsObject;

import java.util.Optional;

/**
 * Todo: Replace with a capability
 */
@Deprecated
public interface IPhysicsChunk {

    void setParentPhysicsObject(Optional<PhysicsObject> physicsObjectOptional);

    Optional<PhysicsObject> getPhysicsObjectOptional();

}
