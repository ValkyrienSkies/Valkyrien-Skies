package valkyrienwarfare.mod.multithreaded;

import valkyrienwarfare.api.Vector;
import valkyrienwarfare.physics.data.ShipTransform;

/**
 * An extension of ShipTransform with extra data that is required to run the
 * physics engine.
 * 
 * @author thebest108
 *
 */
public class PhysicsShipTransform extends ShipTransform {

    private Vector centerOfMassLocal;
    private Vector angularVelocity;
    private Vector linearVelocity;
    private Vector torque;
    // 3 x 3 MOI matrix
    private double[] momentOfIntertiaMatrix;
    private double[] inverseMomentOfIntertiaMatrix;
    private double mass;
    
}
