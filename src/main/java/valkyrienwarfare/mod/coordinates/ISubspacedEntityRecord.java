package valkyrienwarfare.mod.coordinates;

import javax.annotation.concurrent.Immutable;

/**
 * An immutable record for an Entity within a subspace. Holds information which
 * can be used to restore the state of the given entity back to a safe value;
 * including position, rotation, and velocity data.
 * 
 * @author thebest108
 *
 */
@Immutable
public interface ISubspacedEntityRecord {

	ISubspacedEntity getParentEntity();

	ISubspace getParentSubspace();

	VectorImmutable getPosition();

	VectorImmutable getPositionLastTick();
	
	VectorImmutable getLookDirection();

	VectorImmutable getVelocity();

	default VectorImmutable getPositionInGlobalCoordinates() {
		if (getParentSubspace().getSubspaceCoordinatesType() == CoordinateSpaceType.GLOBAL_COORDINATES) {
			return getPosition();
		} else {
			return getParentSubspace().getSubspaceTransform().transform(getPosition(), TransformType.SUBSPACE_TO_GLOBAL);
		}
	}
	
	default VectorImmutable getPositionLastTickInGlobalCoordinates() {
		if (getParentSubspace().getSubspaceCoordinatesType() == CoordinateSpaceType.GLOBAL_COORDINATES) {
			return getPositionLastTick();
		} else {
			return getParentSubspace().getSubspaceTransform().transform(getPositionLastTick(), TransformType.SUBSPACE_TO_GLOBAL);
		}
	}
	
	default VectorImmutable getLookDirectionInGlobalCoordinates() {
		if (getParentSubspace().getSubspaceCoordinatesType() == CoordinateSpaceType.GLOBAL_COORDINATES) {
			return getLookDirection();
		} else {
			return getParentSubspace().getSubspaceTransform().rotate(getLookDirection(), TransformType.SUBSPACE_TO_GLOBAL);
		}
	}
	
	default VectorImmutable getVelocityInGlobalCoordinates() {
		if (getParentSubspace().getSubspaceCoordinatesType() == CoordinateSpaceType.GLOBAL_COORDINATES) {
			return getVelocity();
		} else {
			return getParentSubspace().getSubspaceTransform().rotate(getVelocity(), TransformType.SUBSPACE_TO_GLOBAL);
		}
	}

}
