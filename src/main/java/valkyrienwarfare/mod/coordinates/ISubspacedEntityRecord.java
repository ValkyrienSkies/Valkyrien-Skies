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

	VectorImmutable getLookDirection();

	VectorImmutable getVelocity();
}
