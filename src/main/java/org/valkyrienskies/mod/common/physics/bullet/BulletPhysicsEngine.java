package org.valkyrienskies.mod.common.physics.bullet;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody.btRigidBodyConstructionInfo;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.google.common.collect.ImmutableSet;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.Value;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.addon.control.block.torque.PhysicsThreadOnly;
import org.valkyrienskies.mod.common.coordinates.ShipTransform;
import org.valkyrienskies.mod.common.physics.IPhysicsEngine;
import org.valkyrienskies.mod.common.physics.bullet.MeshCreator.Triangle;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;
import org.valkyrienskies.mod.common.physics.management.physo.ShipData;
import org.valkyrienskies.mod.common.physmanagement.shipdata.QueryableShipData;
import org.valkyrienskies.mod.common.util.JOML;

public class BulletPhysicsEngine implements IPhysicsEngine {

    /**
     * Maps the hashcode of a physics object's UUID to it's BulletData. There are two copies of a
     * physics object (client & server side), this ensures that we are only storing data once per
     * physics object.
     */
    private final Map<Integer, BulletData> dataMap = new ConcurrentHashMap<>(); // TIntObjectMap<BulletData> dataMap = new TIntObjectHashMap<>();

    private final World mcWorld; // please remove this I hate it. // No ~Tri0de

    private btDynamicsWorld bulletWorld;
    private btConstraintSolver constraintSolver;
    private btCollisionConfiguration collisionConfig;
    private btCollisionDispatcher collisionDispatcher;
    private btDbvtBroadphase broadphase;

    private final Queue<btRigidBody> queuedToAdd = new ConcurrentLinkedQueue<>();

    public BulletPhysicsEngine(World mcWorld) {
        this.mcWorld = mcWorld;
        // Note that things are initialized here and not in the object initializer because
        // I don't think it's a good practice to use the object initializer when we're dealing with
        // objects that manually need to be deconstructed
        collisionConfig = new btDefaultCollisionConfiguration();
        broadphase = new btDbvtBroadphase();
        constraintSolver = new btSequentialImpulseConstraintSolver();
        collisionDispatcher = new btCollisionDispatcher(collisionConfig);
        bulletWorld = new btDiscreteDynamicsWorld(collisionDispatcher, broadphase,
            constraintSolver, collisionConfig);
        bulletWorld.setGravity(new Vector3(0f, -9.8f, 0f));

        btGImpactCollisionAlgorithm.registerAlgorithm(collisionDispatcher);


        btCollisionShape groundShape = new btStaticPlaneShape(new Vector3(0, 1, 0), 4); // new btBoxShape(new Vector3(10000, 4, 10000));
        btCollisionObject groundObject = new btCollisionObject();
        groundObject.setCollisionShape(groundShape);

        btRigidBodyConstructionInfo constructionInfo = new btRigidBodyConstructionInfo(
                0, null, groundShape);

        btRigidBody rigidBody = new btRigidBody(constructionInfo);

        bulletWorld.addRigidBody(rigidBody);
        // groundObject.setWorldTransform();
    }

    @Override
    public void addPhysicsObject(@Nonnull PhysicsObject obj) {
        // Create the collision shape
        btCompoundShape collisionShape = new btCompoundShape();

        for (BlockPos pos : obj.getBlockPositions()) {
            float offsetX = pos.getX() + .5f - (float) obj.getCenterCoord().getX();
            float offsetY = pos.getY() + .5f - (float) obj.getCenterCoord().getY();
            float offsetZ = pos.getZ() + .5f - (float) obj.getCenterCoord().getZ();

            Vector3 position = new Vector3(offsetX, offsetY, offsetZ);
            Quaternion orientation = new Quaternion();

            Matrix4 transform = new Matrix4().set(position, orientation);

            collisionShape.addChildShape(transform, new btBoxShape(new Vector3(.5f, .5f, .5f)));
        }
        // Generate the construction info for a rigid body from the collision shape and mass
        // float mass = (float) obj.getInertiaData().getGameTickMass();
        float mass = 50;
        btRigidBodyConstructionInfo constructionInfo = new btRigidBodyConstructionInfo(
            mass, null, collisionShape, getLocalInertia(collisionShape, mass));

        // Create a rigid body from the construction info and collision shape
        btRigidBody rigidBody = new btRigidBody(constructionInfo);
        rigidBody.setCollisionShape(collisionShape);
        Matrix4 positionTransform = JOML.toGDX(obj.getTransform().getPositionTransform());
        rigidBody.setWorldTransform(positionTransform);


        queuedToAdd.add(rigidBody);

        // bulletWorld.addRigidBody(rigidBody);

        // Create BulletData and add to dataMap
        BulletData data = new BulletData(null, null, null, rigidBody);
        dataMap.put(obj.hashCode(), data);
    }

    @Override
    @PhysicsThreadOnly
    public void tick(float delta) {
        if (mcWorld == null) {
            return;
        }
        while (!queuedToAdd.isEmpty()) {
            bulletWorld.addRigidBody(queuedToAdd.remove());
        }
        bulletWorld.stepSimulation(1f/100f);



        // dataMap.values().forEach(data ->
        //     System.out.println(data.getRigidBody().getWorldTransform().getTranslation(new Vector3())));



        QueryableShipData.get(mcWorld).getShips().stream()
            .filter(shipData -> shipData.getPhyso() != null)
            .map(ShipData::getPhyso)
            .forEach(obj -> {
                BulletData data = getData(obj);
                if (data != null && data.rigidBody != null) {
                    btRigidBody body = data.rigidBody;
                    Matrix4 worldTransform = body.getWorldTransform();
                    Matrix4d transform = JOML.convertDouble(worldTransform);
                    Vector3d centerCoord = JOML.convert(obj.getCenterCoord());
                    obj.getShipTransformationManager().setCurrentPhysicsTransform(new ShipTransform(transform, centerCoord));
                }
            });
    }

    @Override
    public void unload() {
        bulletWorld.dispose();
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

    /**
     * Data used by the Bullet Physics Engine, uniquely assigned to each PhysicsObject
     */
    @Value
    public static class BulletData {

        List<Triangle> triangleList;
        btTriangleMesh trimesh;
        btGImpactMeshShape collisionShape;
        btRigidBody rigidBody;

    }

}
