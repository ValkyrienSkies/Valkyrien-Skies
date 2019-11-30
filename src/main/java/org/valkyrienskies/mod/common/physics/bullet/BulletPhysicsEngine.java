package org.valkyrienskies.mod.common.physics.bullet;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btGImpactMeshShape;
import com.badlogic.gdx.physics.bullet.collision.btTriangleMesh;
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody.btRigidBodyConstructionInfo;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.world.World;
import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.valkyrienskies.addon.control.block.torque.PhysicsThreadOnly;
import org.valkyrienskies.mod.common.config.VSConfig;
import org.valkyrienskies.mod.common.coordinates.ShipTransform;
import org.valkyrienskies.mod.common.physics.PhysicsEngine;
import org.valkyrienskies.mod.common.physics.bullet.MeshCreator.Triangle;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;
import org.valkyrienskies.mod.common.ship_handling.IPhysObjectWorld;
import org.valkyrienskies.mod.common.util.JOML;

public class BulletPhysicsEngine implements PhysicsEngine {

    /**
     * Maps the hashcode of a physics object's UUID to it's BulletData. There are two copies
     * of a physics object (client & server side), this ensures that we are only storing data once
     * per physics object.
     */
    private TIntObjectMap<BulletData> dataMap = new TIntObjectHashMap<>();

    private World mcWorld = null; // please remove this I hate it

    private btDynamicsWorld bulletWorld;
    private btConstraintSolver constraintSolver;
    private btCollisionConfiguration collisionConfig;
    private btCollisionDispatcher collisionDispatcher;
    private btDbvtBroadphase broadphase;

    public BulletPhysicsEngine() {
        // Note that things are initialized here and not in the object initializer because
        // I don't think it's a good practice to use the object initializer when we're dealing with
        // objects that manually need to be deconstructed

        collisionConfig = new btDefaultCollisionConfiguration();
        broadphase = new btDbvtBroadphase();
        constraintSolver = new btSequentialImpulseConstraintSolver();
        collisionDispatcher = new btCollisionDispatcher(collisionConfig);
        bulletWorld = new btDiscreteDynamicsWorld(collisionDispatcher, broadphase,
            constraintSolver, collisionConfig);
        bulletWorld.setGravity(JOML.toGDX(JOML.convert(VSConfig.gravity())));
    }

    @Override
    public void addPhysicsObject(@Nonnull PhysicsObject obj) {
        mcWorld = obj.getWorld(); // this is the worst code ever

        // Create BulletData and add to dataMap
        BulletData data = new BulletData();
        dataMap.put(obj.hashCode(), data);

        // Create the 'triangle list' from the block positions
        data.triangleList = MeshCreator.getMeshTriangles(obj.getBlockPositions());
        // Create a mesh from the 'triangle list'
        data.trimesh =  MeshCreator.getMesh(data.triangleList);
        // Create a collision shape from the mesh
        data.collisionShape = new btGImpactMeshShape(data.trimesh);

        // Generate the construction info for a rigid body from the collision shape and mass
        float mass = (float) obj.getInertiaData().getGameTickMass();
        btRigidBodyConstructionInfo constructionInfo = new btRigidBodyConstructionInfo(
            mass, null, data.collisionShape, getLocalInertia(data.collisionShape, mass));

        // Create a motion state for the rigid body
        data.motionState = new VSMotionState(obj);

        // Create a rigid body from the construction info and collision shape
        data.rigidBody = new btRigidBody(constructionInfo);
        data.rigidBody.setCollisionShape(data.collisionShape);
        data.rigidBody.setMotionState(data.motionState);

        bulletWorld.addRigidBody(data.rigidBody);
    }

    @Override
    @PhysicsThreadOnly
    public void tick(float delta) {
        bulletWorld.stepSimulation(delta, 2, 1/60f);
        for (PhysicsObject obj : ((IPhysObjectWorld) mcWorld).getAllLoadedPhysObj()) {
            BulletData data = getData(obj);
            btRigidBody body = data.rigidBody;
            Matrix4d transform = JOML.convertDouble(body.getWorldTransform());
            Vector3d centerCoord = JOML.convert(obj.getCenterCoord());
            obj.getShipTransformationManager()
                .updateAllTransforms(new ShipTransform(transform, centerCoord), false, true);
        }
    }

    private static Vector3 getLocalInertia(btCollisionShape shape, float mass) {
        Vector3 localInertia = new Vector3();
        shape.calculateLocalInertia(mass, localInertia);
        return localInertia;
    }

    @Nullable
    public BulletData getData(PhysicsObject obj) {
        return dataMap.get(obj.hashCode());
    }

    @RequiredArgsConstructor
    private static class VSMotionState extends btMotionState {
        private final PhysicsObject obj;

        @Getter
        private Matrix4 transform;

        @Override
        public void getWorldTransform(Matrix4 worldTrans) {
            worldTrans.set(transform);
        }

        @Override
        public void setWorldTransform(Matrix4 worldTrans) {
            transform.set(worldTrans);
        }
    }

    /**
     * Data used by the Bullet Physics Engine, uniquely assigned to each PhysicsObject
     */
    public static class BulletData {
        public List<Triangle> triangleList;
        public btTriangleMesh trimesh;
        public btCollisionShape collisionShape;
        public btRigidBody rigidBody;
        public btMotionState motionState;
    }

}
