package org.valkyrienskies.mod.common.util.names;

import org.valkyrienskies.mod.common.ship_handling.PhysicsObject;

/**
 * Generates names for use of naming things like
 * {@link PhysicsObject}. May or may not be
 * human-readable, that's implementation dependent.
 */
public interface NameGenerator {

    String generateName();

}
