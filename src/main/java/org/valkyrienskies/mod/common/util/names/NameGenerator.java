package org.valkyrienskies.mod.common.util.names;

/**
 * Generates names for use of naming things like
 * {@link org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject}. May or may not be
 * human-readable, that's implementation dependent.
 */
public interface NameGenerator {

    String generateName();

}
