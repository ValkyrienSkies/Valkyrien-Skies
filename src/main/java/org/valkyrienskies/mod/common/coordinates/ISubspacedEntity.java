package org.valkyrienskies.mod.common.coordinates;

import org.valkyrienskies.mod.common.math.Vector;

/**
 * An interface that allows entities to interact safely within the context of multiple subspaces.
 *
 * @author thebest108
 */
public interface ISubspacedEntity {

    CoordinateSpaceType currentSubspaceType();

    Vector createCurrentPositionVector();

    Vector createLastTickPositionVector();

    Vector createCurrentLookVector();

    Vector createCurrentVelocityVector();

    void restoreSubspacedEntityStateToRecord(ISubspacedEntityRecord record);

    int getSubspacedEntityID();
}
