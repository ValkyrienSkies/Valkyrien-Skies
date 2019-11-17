package org.valkyrienskies.mod.common.coordinates;

/**
 * A workaround interface used with Mixins to give objects the capability of having a subspace.
 *
 * @author thebest108
 */
public interface ISubspaceProvider {

    ISubspace getSubspace();
}
