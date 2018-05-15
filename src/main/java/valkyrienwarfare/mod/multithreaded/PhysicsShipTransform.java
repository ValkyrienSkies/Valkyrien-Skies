package valkyrienwarfare.mod.multithreaded;

import valkyrienwarfare.physics.data.ShipTransform;

/**
 * An extension of ShipTransform with extra data that is required to run the
 * physics engine.
 * 
 * @author thebest108
 *
 */
public class PhysicsShipTransform extends ShipTransform {

    public PhysicsShipTransform(ShipTransform transform) {
        super(transform);
    }

}
