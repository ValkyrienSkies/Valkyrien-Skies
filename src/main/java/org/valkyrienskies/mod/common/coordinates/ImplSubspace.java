package org.valkyrienskies.mod.common.coordinates;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.valkyrienskies.mod.common.entity.PhysicsWrapperEntity;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;
import valkyrienwarfare.api.TransformType;

/**
 * A basic implementation of the ISubspace interface.
 *
 * @author thebest108
 */
public class ImplSubspace implements ISubspace {

    // If null, then we are the World subspace.
    private final PhysicsObject parent;
    private final Map<ISubspacedEntity, ISubspacedEntityRecord> subspacedEntityRecords;

    public ImplSubspace(@Nullable PhysicsObject parent) {
        this.parent = parent;
        this.subspacedEntityRecords = new HashMap<ISubspacedEntity, ISubspacedEntityRecord>();
    }

    @Override
    public boolean hasRecordForSubspacedEntity(ISubspacedEntity subspaced) {
        return subspacedEntityRecords.containsKey(subspaced);
    }

    @Override
    public ISubspacedEntityRecord getRecordForSubspacedEntity(ISubspacedEntity subspaced) {
        return subspacedEntityRecords.get(subspaced);
    }

    @Override
    public void snapshotSubspacedEntity(ISubspacedEntity subspaced) {
        if (subspaced.currentSubspaceType() != CoordinateSpaceType.GLOBAL_COORDINATES) {
            throw new IllegalArgumentException(
                "Subspace snapshots can only be taken for entities that in the global coordinates system!");
        }
        if (subspaced instanceof PhysicsWrapperEntity) {
            throw new IllegalArgumentException(
                "Do not create subspace records for PhysicsWrapperEntities!!");
        }
        subspacedEntityRecords.put(subspaced, createRecordForSubspacedEntity(subspaced));
    }

    private ISubspacedEntityRecord createRecordForSubspacedEntity(
        ISubspacedEntity subspacedEntity) {
        Vector position = subspacedEntity.createCurrentPositionVector();
        Vector positionLastTick = subspacedEntity.createLastTickPositionVector();
        Vector look = subspacedEntity.createCurrentLookVector();
        Vector velocity = subspacedEntity.createCurrentVelocityVector();
        ShipTransform subspaceTransform = getSubspaceTransform();
        if (subspaceTransform != null) {
            subspaceTransform.transform(position, TransformType.GLOBAL_TO_SUBSPACE);
            subspaceTransform.rotate(look, TransformType.GLOBAL_TO_SUBSPACE);
            subspaceTransform.rotate(velocity, TransformType.GLOBAL_TO_SUBSPACE);
        }
        return new ImplSubspacedEntityRecord(subspacedEntity, this, position.toImmutable(),
            positionLastTick.toImmutable(), look.toImmutable(), velocity.toImmutable());
    }

    @Override
    public CoordinateSpaceType getSubspaceCoordinatesType() {
        if (parent == null) {
            return CoordinateSpaceType.GLOBAL_COORDINATES;
        } else {
            return CoordinateSpaceType.SUBSPACE_COORDINATES;
        }
    }

    @Override
    public ShipTransform getSubspaceTransform() {
        if (getSubspaceCoordinatesType() == CoordinateSpaceType.GLOBAL_COORDINATES) {
            return null;
        } else {
            ShipTransform transform = parent.getShipTransformationManager()
                .getCurrentTickTransform();
            if (transform == null) {
                throw new IllegalStateException(
                    "A PhysicsObject got a request to use its subspace, but it had no transforms "
                        + "loaded. This is crash worthy.");
            }
            return transform;
        }
    }

    @Override
    public int getSubspaceParentEntityID() {
        if (getSubspaceCoordinatesType() == CoordinateSpaceType.GLOBAL_COORDINATES) {
            throw new IllegalStateException(
                "The World coordinate subspace doesn't have an entity ID. Don't call this method "
                    + "unless you're sure that the subspace isn't the world.");
        }
        return parent.getWrapperEntity().getEntityId();
    }

    @Override
    public void forceSubspaceRecord(ISubspacedEntity entity, ISubspacedEntityRecord record) {
        subspacedEntityRecords.put(entity, record);
    }

}
