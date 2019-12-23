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

    final Set<RigidBodyObserver> observers = ConcurrentHashMap.newKeySet();
    @Getter
    InertiaData inertiaData;
    @Getter
    Matrix4dc transform;
    final Set<Box> boxes = new HashSet<>();
    final Set<Box> boxesUnmodifiable = Collections.unmodifiableSet(boxes);

    @Getter
    private final ITransformController controller;

    public AbstractRigidBody(ITransformController controller, ImmutableSet<Box> initial,
        InertiaData data, Matrix4dc transform) {
        this.inertiaData = data;
        this.transform = transform;
        this.boxes.addAll(initial);

        this.controller = controller;
        controller.addRigidBody(this);
    }

    public void updateInertiaData(InertiaData newInertiaData) {
        this.inertiaData = newInertiaData;
        this.observers.forEach(o -> o.onInertiaUpdate(newInertiaData));
    }

    /**
     * This updates the transform of the rigid body and notifies the physics engine.
     *
     * THIS SHOULD NOT BE CALLED BY A PHYSICS ENGINE, use {@link #silentUpdateTransform(Matrix4dc)}
     * instead.
     */
    public void updateTransform(Matrix4dc transform) {
        this.transform = transform;
        this.observers.forEach(o -> o.onTransformUpdate(transform));
        onTransformUpdate(transform);
    }

    /**
     * THIS IS ONLY TO BE CALLED BY THE PHYSICS ENGINE, IT DOES NOT TRIGGER THE
     * {@link RigidBodyObserver#onTransformUpdate(Matrix4dc)} EVENT
     */
    public void silentUpdateTransform(Matrix4dc transform) {
        this.transform = transform;
        onTransformUpdate(transform);
    }

    /**
     * Called when this Rigid Body's transform is changed, typically
     */
    protected abstract void onTransformUpdate(Matrix4dc transform);

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

        void onInertiaUpdate(InertiaData newInertia);

        /**
         * Called when something causes the rigid body's transform to be changed through
         * {@link #updateTransform(Matrix4dc)}, e.g., NOT BY THE PHYSICS ENGINE
         *
         * @param newTransform The new transform of this rigid body updated by something
         *                     other than the physics engine
         */
        void onTransformUpdate(Matrix4dc newTransform);

    }

    @Value
    public static class InertiaData {
        Vector3dc centerOfMass;
        Matrix3dc inertia;
        float mass;
    }

    @Value
    public static class Box {
        Vector3dc center;
        Vector3dc halfExtents;
    }

}
