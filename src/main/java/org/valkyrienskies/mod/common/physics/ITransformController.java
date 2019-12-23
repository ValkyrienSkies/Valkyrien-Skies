package org.valkyrienskies.mod.common.physics;

/**
 * This class accepts multiple IRigidBody objects and calls their onTransformUpdate methods to move
 * them around
 */
public interface ITransformController {

    /**
     * Adds a rigid body to this transform controller, should NEVER be called by the user
     */
    void addRigidBody(AbstractRigidBody body);

    void tick(float delta);

    default void unload() {}

}
