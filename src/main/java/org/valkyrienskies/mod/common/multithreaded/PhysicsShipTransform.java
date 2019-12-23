package org.valkyrienskies.mod.common.multithreaded;

import net.minecraft.util.math.AxisAlignedBB;
import org.valkyrienskies.mod.common.coordinates.ShipTransform;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.collision.polygons.Polygon;
import valkyrienwarfare.api.TransformType;

/**
 * An extension of ShipTransform with extra data not required by most other ShipTransform objects.
 *
 * @author thebest108
 */
@Deprecated
public class PhysicsShipTransform extends ShipTransform {

    private final double posX;
    private final double posY;
    private final double posZ;
    private final double pitch;
    private final double yaw;
    private final double roll;
    private final Vector centerOfMass;
    private final AxisAlignedBB shipBoundingBox;

    /**
     * Creates the PhysicsShipTransform.
     *
     * @param physX
     * @param physY
     * @param physZ
     * @param physPitch
     * @param physYaw
     * @param physRoll
     * @param physCenterOfMass
     * @param gameTickShipBoundingBox
     * @param gameTickTransform
     */
    public PhysicsShipTransform(double physX, double physY, double physZ, double physPitch,
        double physYaw,
        double physRoll, Vector physCenterOfMass, AxisAlignedBB gameTickShipBoundingBox,
        ShipTransform gameTickTransform) {
        super(physX, physY, physZ, physPitch, physYaw, physRoll, physCenterOfMass.toVector3d());
        this.posX = physX;
        this.posY = physY;
        this.posZ = physZ;
        this.pitch = physPitch;
        this.yaw = physYaw;
        this.roll = physRoll;
        this.centerOfMass = new Vector(physCenterOfMass);
        this.shipBoundingBox = createApproxBoundingBox(gameTickShipBoundingBox, gameTickTransform);
    }

    /**
     * Makes a reasonable approximation for what the parent AABB would be with the physics
     * transformation instead of the game transformation. Reasonably accurate for small differences
     * in time, but shouldn't be used for time deltas greater than a tick or two.
     *
     * @return An approximation of a physics collision bounding box.
     */
    private AxisAlignedBB createApproxBoundingBox(AxisAlignedBB gameTickBB,
        ShipTransform gameTickTransform) {
        Polygon gameTickBBPoly = new Polygon(gameTickBB);
        gameTickBBPoly.transform(gameTickTransform, TransformType.GLOBAL_TO_SUBSPACE);
        gameTickBBPoly.transform(this, TransformType.SUBSPACE_TO_GLOBAL);
        return gameTickBBPoly.getEnclosedAABB();
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

    /**
     * Used for approximation purposes such that the AABB only has to be properly recalculated every
     * game tick instead of every physics tick.
     *
     * @return An approximation for what the ship collision bounding box would be using the physics
     * transformations instead of game transformation.
     */
    public AxisAlignedBB getShipBoundingBox() {
        return shipBoundingBox;
    }

}
