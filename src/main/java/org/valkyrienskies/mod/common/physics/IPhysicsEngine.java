package org.valkyrienskies.mod.common.physics;

import org.joml.Vector3dc;

public interface IPhysicsEngine extends ITransformController {

    void applyForce(AbstractRigidBody body, Vector3dc force, Vector3dc position);

    void addCentralForce(AbstractRigidBody body, Vector3dc force);

}
