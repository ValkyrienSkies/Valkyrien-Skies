package org.valkyrienskies.mod.common.physics;

import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;

public interface IPhysicsEngine {

    void addPhysicsObject(PhysicsObject obj);

    void tick(float delta);

    void unload();

}
