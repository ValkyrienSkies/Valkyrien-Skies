package org.valkyrienskies.mod.common.physics;

public interface IChunkRigidBodyHelper {

    TerrainRigidBody getRigidBody(int yIndex);

    void setRigidBody(int yIndex, TerrainRigidBody rigidBody);
}
