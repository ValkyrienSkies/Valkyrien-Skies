package valkyrienwarfare.mod.coordinates;

import valkyrienwarfare.api.Vector;

public interface ISubspace {

	/**
	 * True if this subspace has a coordinate for the given ISubspacedEntity.
	 * 
	 * @param subspaced
	 * @return
	 */
	boolean hasRecordForSubspacedEntity(ISubspacedEntity subspaced);

	/**
	 * Returns the coordinates Vector for the given ISubspacedEntity relative to
	 * this ISubSpace.
	 * 
	 * @param subspaced
	 * @return
	 */
	ISubspacedEntityRecord getRecordForSubspacedEntity(ISubspacedEntity subspaced);

	/**
	 * Creates a ISubspacedEntityRecord for the given ISubspacedEntity and stores
	 * the data with the ISubSpace.
	 * 
	 * @param subspaced
	 */
	void snapshotSubspacedEntity(ISubspacedEntity subspaced);

	/**
	 * Returns GLOBAL if this subspace is the world, and SUBSPACE for PhysicsObject
	 * subspaces.
	 * 
	 * @return
	 */
	CoordinateSpaceType getSubspaceCoordinatesType();
}
