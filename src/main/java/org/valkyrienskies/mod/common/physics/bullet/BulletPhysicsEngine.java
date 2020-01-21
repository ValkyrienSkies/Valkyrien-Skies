package org.valkyrienskies.mod.common.physics.bullet;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btCompoundShape;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btGImpactCollisionAlgorithm;
import com.badlogic.gdx.physics.bullet.collision.btStaticPlaneShape;
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody.btRigidBodyConstructionInfo;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.joml.Matrix4d;
import org.joml.Matrix4dc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.addon.control.block.torque.PhysicsThreadOnly;
import org.valkyrienskies.mod.common.coordinates.ShipTransform;
import org.valkyrienskies.mod.common.physics.AbstractRigidBody;
import org.valkyrienskies.mod.common.physics.AbstractRigidBody.Box;
import org.valkyrienskies.mod.common.physics.AbstractRigidBody.InertiaData;
import org.valkyrienskies.mod.common.physics.AbstractRigidBody.RigidBodyObserver;
import org.valkyrienskies.mod.common.physics.IPhysicsEngine;
import org.valkyrienskies.mod.common.physics.ShipRigidBody;
import org.valkyrienskies.mod.common.physmanagement.shipdata.QueryableShipData;
import org.valkyrienskies.mod.common.util.JOML;
import org.valkyrienskies.mod.common.util.VSPreconditions;

public class BulletPhysicsEngine implements IPhysicsEngine {

    private final Map<AbstractRigidBody, BulletData> dataMap = new ConcurrentHashMap<>();

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

        // btRigidBody rigidBody = new btRigidBody(constructionInfo);

        // bulletWorld.addRigidBody(rigidBody);
        // groundObject.setWorldTransform();
    }

    private btCompoundShape generateCollisionShape(Collection<BlockPos> positions,
                                                   Vector3 centerOfMass) {
        btCompoundShape collisionShape = new btCompoundShape();

        for (BlockPos pos : positions) {
            Vector3 position = JOML.toGDX(pos).add(0.5f).sub(centerOfMass);
            Quaternion orientation = new Quaternion();

            Matrix4 transform = new Matrix4().set(position, orientation);

            collisionShape.addChildShape(transform, new btBoxShape(new Vector3(.5f, .5f, .5f)));
        }

        return collisionShape;
    }

    /*public void addPhysicsObject(@Nonnull PhysicsObject obj) {
        // Create the collision shape
        btCompoundShape collisionShape = generateCollisionShape(
            obj.getBlockPositions(),
            JOML.toGDX(obj.getTransform().getCenterCoord()));

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


        // bulletWorld.addRigidBody(rigidBody);

        // Create BulletData and add to dataMap
        dataMap.put(obj, observer);
    }*/

    @Override
    public void addRigidBody(AbstractRigidBody body) {
        BulletData observer = new BulletData(body);
        queuedToAdd.add(observer.bulletBody);
        dataMap.put(body, observer);
    }

    @Override
    public void removeRigidBody(AbstractRigidBody body) {
        try {
            BulletData removed = dataMap.remove(body);
        } catch (Exception e) {
            String text = e.toString();
        }
    }

    @PhysicsThreadOnly
    public void tick(float delta) {
        VSPreconditions.assertPhysicsThread();

        if (mcWorld == null) {
            return;
        }
        while (!queuedToAdd.isEmpty()) {
            bulletWorld.addRigidBody(queuedToAdd.remove());
        }
        bulletWorld.stepSimulation(1f/100f);

        // dataMap.values().forEach(data ->
        //     System.out.println(data.getRigidBody().getWorldTransform().getTranslation(new Vector3())));

        QueryableShipData.get(mcWorld).getLoadedPhysos()
                .forEach(obj -> {
                    BulletData data = getData(obj.getShipRigidBody());
                    if (data.bulletBody != null) {
                        btRigidBody body = data.bulletBody;
                        Matrix4 worldTransform = body.getWorldTransform();
                        Matrix4d transform = JOML.convertDouble(worldTransform);
                        obj.getShipTransformationManager().setCurrentPhysicsTransform(new ShipTransform(transform, data.centerOfMass));
                    }
                });
    }

    public void unload() {
        bulletWorld.dispose();
    }

    private static Vector3 getLocalInertia(btCollisionShape shape, float mass) {
        Vector3 localInertia = new Vector3();
        shape.calculateLocalInertia(mass, localInertia);
        return localInertia;
    }

    @Nonnull
    public BulletData getData(AbstractRigidBody obj) {
        return Objects.requireNonNull(dataMap.get(obj));
    }

    @Override
    public void applyForce(AbstractRigidBody body, Vector3dc force, Vector3dc position) {
        getData(body).bulletBody.applyForce(JOML.toGDX(force), JOML.toGDX(position));
    }

    @Override
    public void addCentralForce(AbstractRigidBody body, Vector3dc force) {
        getData(body).bulletBody.applyCentralForce(JOML.toGDX(force));
    }

    /**
     * This class is only created once when an AbstractRigidBody is registered with the
     * BulletPhysicsEngine.
     */
    private static class BulletData implements RigidBodyObserver {

        final btRigidBody bulletBody;
        final btCompoundShape boxesShape;
        final btCompoundShape bulletBodyShape;
        final Map<Box, btBoxShape> shapeMap = new HashMap<>();

        final Vector3dc shipRefPos;
        Vector3dc centerOfMass;

        // This constructor is only called when a new AbstractRigidBody is registered
        BulletData(AbstractRigidBody observing) {
            observing.registerObserver(this);

            boxesShape = new btCompoundShape();
            observing.getInternalShapeSet().forEach(this::addBox);

            // TODO: This is REALLY BAD code, but hey, it works for now.
            if (observing instanceof ShipRigidBody) {
                shipRefPos = new Vector3d(((ShipRigidBody) observing).getReferencePos());
            } else {
                shipRefPos = new Vector3d();
            }
            // TODO: bad code end


            // Remember the old center of mass
            centerOfMass = new Vector3d(observing.getInertiaData().getCenterOfMass());

            bulletBodyShape = new btCompoundShape();

            Matrix4dc boxShapeTransform = new Matrix4d().translate((float) shipRefPos.x() - centerOfMass.x(), (float) shipRefPos.y() - centerOfMass.y(), (float) shipRefPos.z() - centerOfMass.z());

            bulletBodyShape.addChildShape(JOML.toGDX(boxShapeTransform), boxesShape);

            // bulletBodyShape.createAabbTreeFromChildren();
            // bulletBodyShape.recalculateLocalAabb();

            float mass = observing.getInertiaData().getMass();

            // TODO: implement this properly
            btRigidBodyConstructionInfo constructionInfo = new btRigidBodyConstructionInfo(
                    mass, null, bulletBodyShape, getLocalInertia(bulletBodyShape, mass));

            bulletBody = new btRigidBody(constructionInfo);

            if (mass == 0) {
                bulletBody.setCollisionFlags(bulletBody.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_STATIC_OBJECT);
            }

            bulletBody.setCollisionShape(bulletBodyShape);

            Matrix4 initialBodyTransform = JOML.toGDX(observing.getTransform());
            bulletBody.setWorldTransform(initialBodyTransform);
        }

        @Override
        public void onShapeUpdate(@Nullable ImmutableSet<Box> added, @Nullable ImmutableSet<Box> removed) {
            // TODO: This is NOT thread-safe. We should have something like PhysicsEngine.queueShapeUpdate()
            //       but for now we just pray that nothing breaks.
            if (removed != null)
                removed.forEach(this::removeBox);
            if (added != null)
                added.forEach(this::addBox);

            // bulletBodyShape.createAabbTreeFromChildren();
            // bulletBodyShape.recalculateLocalAabb();

            bulletBody.activate();
        }

        @Override
        public void onInertiaUpdate(InertiaData newInertia) {
            // TODO: implement this properly
            float mass = newInertia.getMass();
            Vector3 inertia = getLocalInertia(boxesShape, mass);

            Vector3dc newCenterOfMass = newInertia.getCenterOfMass();

            // Offset the rigid body collision shape.
            Matrix4 newTransform = JOML.toGDX(new Matrix4d().translate((float) shipRefPos.x() - newCenterOfMass.x(), (float) shipRefPos.y() - newCenterOfMass.y(), (float) shipRefPos.z() - newCenterOfMass.z()));
            bulletBodyShape.updateChildTransform(0, newTransform);


            // Basically we need to offset the rigid body position by the change of the center of mass.
            // Otherwise the ship will jiggle when we update the center of mass
            Vector3d offset = newCenterOfMass.sub(centerOfMass, new Vector3d());
            Matrix4dc bulletBodyTransform = JOML.convertDouble(bulletBody.getWorldTransform());
            bulletBodyTransform.transformDirection(offset);
            bulletBody.translate(JOML.toGDX(offset));

            // Update the mass and inertia matrix
            bulletBody.setMassProps(mass, inertia);

            // Update the stored center of mass
            this.centerOfMass = newCenterOfMass;
        }

        /**
         * Removes a box from the shapeMap and then removes the associated btBox from the
         * compound shape
         */
        private void removeBox(Box box) {
            boxesShape.removeChildShape(shapeMap.remove(box));
        }

        /**
         * Creates a btBox and adds it to the compound shape and adds it to the shapeMap
         */
        private void addBox(Box box) {
            // Create the bullet version of the data stored in the Box
            Vector3 halfExtents = JOML.toGDX(box.getHalfExtents());
            Matrix4 position = JOML.toGDX(new Matrix4d().setTranslation(box.getCenter()));
            btBoxShape bulletBox = new btBoxShape(halfExtents);

            // Add to the bullet compound shape
            boxesShape.addChildShape(position, bulletBox);
            // Maintain a map
            shapeMap.put(box, bulletBox);
        }

        @Override
        public void onTransformUpdate(Matrix4dc newTransform) {
            bulletBody.setWorldTransform(JOML.toGDX(newTransform));
        }

    }

}
