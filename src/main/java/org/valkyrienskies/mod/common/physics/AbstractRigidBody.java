package org.valkyrienskies.mod.common.physics;

import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.Value;
import org.joml.Matrix3dc;
import org.joml.Matrix4dc;
import org.joml.Vector3dc;

public abstract class AbstractRigidBody {

    Set<RigidBodyObserver> observers = ConcurrentHashMap.newKeySet();
    @Getter
    Vector3dc centerOfMass;
    @Getter
    Matrix3dc inertia;
    @Getter
    float mass;
    @Getter
    Matrix4dc transform;
    final Set<Box> boxes = new HashSet<>();
    final Set<Box> boxesUnmodifiable = Collections.unmodifiableSet(boxes);

    @Getter
    private final ITransformController controller;

    public AbstractRigidBody(ITransformController controller, ImmutableSet<Box> initial,
        Vector3dc centerOfMass, Matrix3dc inertia, Matrix4dc transform) {
        this.centerOfMass = centerOfMass;
        this.inertia = inertia;
        this.transform = transform;
        this.boxes.addAll(initial);

        this.controller = controller;
        controller.addRigidBody(this);
    }

    public void updateTransform(Matrix4dc transform) {
        this.transform = transform;
        this.observers.forEach(o -> o.onTransformChange(transform));
        onTransformUpdate(transform);
    }

    protected abstract void onTransformUpdate(Matrix4dc transform);

    public void updateCenterOfMass(Vector3dc newCenterOfMass) {
        this.centerOfMass = newCenterOfMass;
        this.observers.forEach(o -> o.onCenterOfMassUpdate(newCenterOfMass));
    }

    public void updateInertia(Matrix3dc newInertia) {
        this.inertia = newInertia;
        this.observers.forEach(o -> o.onInertiaUpdate(newInertia));
    }

    public void updateMass(float newMass) {
        this.mass = newMass;
        this.observers.forEach(o -> o.onUpdateMass(newMass));
    }

    public Set<Box> getInternalShapeSet() {
        return boxesUnmodifiable;
    }

    public void addBox(Box box) {
        this.updateShape(ImmutableSet.of(box), ImmutableSet.of());
    }

    public void removeBox(Box box) {
        this.updateShape(ImmutableSet.of(), ImmutableSet.of(box));
    }

    public void updateShape(ImmutableSet<Box> added, ImmutableSet<Box> removed) {
        this.boxes.addAll(added);
        this.boxes.removeAll(removed);
        this.observers.forEach(o -> o.onShapeUpdate(added, removed));
    }

    public boolean registerObserver(RigidBodyObserver observer) {
        return observers.add(observer);
    }

    public boolean deregisterObserver(RigidBodyObserver observer) {
        return observers.remove(observer);
    }

    public interface RigidBodyObserver {

        void onShapeUpdate(ImmutableSet<Box> added, ImmutableSet<Box> removed);

        void onInertiaUpdate(Matrix3dc newInertia);

        void onCenterOfMassUpdate(Vector3dc newCenterOfMass);

        /**
         * Called when something causes the rigid body's transform to be changed through
         * {@link #updateTransform(Matrix4dc)}, e.g., NOT BY THE PHYSICS ENGINE
         *
         * @param newTransform The new transform of this rigid body updated by something
         *                     other than the physics engine
         */
        void onTransformChange(Matrix4dc newTransform);

        void onUpdateMass(float newMass);

    }

    @Value
    public static class Box {
        Vector3dc center;
        Vector3dc halfExtents;
    }

}
