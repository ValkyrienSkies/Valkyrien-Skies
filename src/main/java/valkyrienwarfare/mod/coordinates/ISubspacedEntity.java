package valkyrienwarfare.mod.coordinates;

/**
 * An interface that allows entities to interact safely within the context of
 * multiple subspaces.
 * 
 * @author thebest108
 *
 */
public interface ISubspacedEntity {

	CoordinateSpaceType currentSubspaceType();
}
