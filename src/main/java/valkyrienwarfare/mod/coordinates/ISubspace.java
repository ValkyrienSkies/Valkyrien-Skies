package valkyrienwarfare.mod.coordinates;

import valkyrienwarfare.api.Vector;

public interface ISubspace {

	/**
	 * True if this subspace has a coordinate for the given ISubspacedEntity.
	 * 
	 * @param subspaced
	 * @return
	 */
	boolean hasCoordinatesForSubspacedEntity(ISubspacedEntity subspaced);

	/**
	 * Returns the coordinates Vector for the given ISubspacedEntity relative to
	 * this ISubSpace.
	 * 
	 * @param subspaced
	 * @return
	 */
	Vector getCoordinatesOfSubspacedEntity(ISubspacedEntity subspaced);

	/**
	 * Records the position of this 
	 * @param subspaced
	 */
	void snapshotSubspacedEntity(ISubspacedEntity subspaced);
}
