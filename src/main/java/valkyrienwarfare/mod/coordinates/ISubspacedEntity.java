package valkyrienwarfare.mod.coordinates;

import valkyrienwarfare.api.Vector;

/**
 * An interface that allows entities to interact safely within the context of
 * multiple subspaces.
 * 
 * @author thebest108
 *
 */
public interface ISubspacedEntity {

	CoordinateSpaceType currentSubspaceType();

	Vector createCurrentPositionVector();

	Vector createLastTickPositionVector();
	
	Vector createCurrentLookVector();

	Vector createCurrentVelocityVector();
	
	void restoreSubspacedEntityStateToRecord(ISubspacedEntityRecord record);
}
