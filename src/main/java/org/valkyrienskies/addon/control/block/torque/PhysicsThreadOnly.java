package org.valkyrienskies.addon.control.block.torque;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * This method may only be accessed on the physics thread
 */
@Target(ElementType.METHOD)
public @interface PhysicsThreadOnly {}
