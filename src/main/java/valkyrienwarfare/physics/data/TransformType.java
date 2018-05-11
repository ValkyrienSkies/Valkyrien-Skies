package valkyrienwarfare.physics.data;

/**
 * A simple enum used to tell the ship transform which coordinates system we are
 * want to change to.
 * 
 * Ex. Moving from local to global at (0, 0, 0) will give us to the position of
 * the center of mass of the ship relative to the game world.
 * 
 * @author thebest108
 *
 */
public enum TransformType {

    LOCAL_TO_GLOBAL, GLOBAL_TO_LOCAL
}
