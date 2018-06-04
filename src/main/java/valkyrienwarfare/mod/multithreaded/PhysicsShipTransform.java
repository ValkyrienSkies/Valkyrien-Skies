/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2018 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package valkyrienwarfare.mod.multithreaded;

import net.minecraft.util.math.AxisAlignedBB;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.physics.ShipTransform;
import valkyrienwarfare.physics.TransformType;
import valkyrienwarfare.physics.collision.polygons.Polygon;

/**
 * An extension of ShipTransform with extra data not required by most other
 * ShipTransform objects.
 *
 * @author thebest108
 */
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
     * @param shipBoundingBox
     */
    public PhysicsShipTransform(double physX, double physY, double physZ, double physPitch, double physYaw,
                                double physRoll, Vector physCenterOfMass, AxisAlignedBB gameTickShipBoundingBox, ShipTransform gameTickTransform) {
        super(physX, physY, physZ, physPitch, physYaw, physRoll, physCenterOfMass);
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
     * Makes a reasonable approximation for what the parent AABB would be with the
     * physics transformation instead of the game transformation. Reasonably
     * accurate for small differences in time, but shouldn't be used for time deltas
     * greater than a tick or two.
     *
     * @return An approximation of a physics collision bounding box.
     */
    private AxisAlignedBB createApproxBoundingBox(AxisAlignedBB gameTickBB, ShipTransform gameTickTransform) {
        Polygon gameTickBBPoly = new Polygon(gameTickBB);
        gameTickBBPoly.transform(gameTickTransform, TransformType.GLOBAL_TO_LOCAL);
        gameTickBBPoly.transform(this, TransformType.LOCAL_TO_GLOBAL);
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
     * Used for approximation purposes such that the AABB only has to be properly
     * recalculated every game tick instead of every physics tick.
     *
     * @return An approximation for what the ship collision bounding box would be
     * using the physics transformations instead of game transformation.
     */
    public AxisAlignedBB getShipBoundingBox() {
        return shipBoundingBox;
    }

}
