package valkyrienwarfare.mod.multithreaded;

import valkyrienwarfare.api.Vector;
import valkyrienwarfare.physics.data.ShipTransform;

/**
 * An extension of ShipTransform with extra data not required by most other
 * ShipTransform objeccts.
 * 
 * @author thebest108
 *
 */
public class PhysicsShipTransform extends ShipTransform {

    private final double posX;
    private final double posY;
    private final double posZ;
    private final double pitch;
    private final double yaw;
    private final double roll;
    private final Vector centerOfMass;

    /**
     * Creates the PhysicsShipTransform 
     * @param physX
     * @param physY
     * @param physZ
     * @param physPitch
     * @param physYaw
     * @param physRoll
     * @param physCenterOfMass
     */
    public PhysicsShipTransform(double physX, double physY, double physZ, double physPitch, double physYaw,
            double physRoll, Vector physCenterOfMass) {
        super(physX, physY, physZ, physPitch, physYaw, physRoll, physCenterOfMass);
        this.posX = physX;
        this.posY = physY;
        this.posZ = physZ;
        this.pitch = physPitch;
        this.yaw = physYaw;
        this.roll = physRoll;
        this.centerOfMass = new Vector(physCenterOfMass);
    }

    public double getPosX() {
        return posX;
    }

    public double getPosY() {
        return posY;
    }

    public double getPosZ() {
        return posZ;
    }

    public double getPitch() {
        return pitch;
    }

    public double getYaw() {
        return yaw;
    }

    public double getRoll() {
        return roll;
    }

    public Vector getCenterOfMass() {
        return centerOfMass;
    }

}
