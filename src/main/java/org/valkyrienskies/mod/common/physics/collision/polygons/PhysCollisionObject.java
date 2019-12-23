package org.valkyrienskies.mod.common.physics.collision.polygons;

import org.valkyrienskies.mod.common.math.VSMath;
import org.valkyrienskies.mod.common.math.Vector;

public class PhysCollisionObject {

    public final Vector collision_normal;
    public final Polygon movable, fixed;
    public double penetrationDistance;
    public boolean seperated;
    private double[] playerMinMax;
    private double[] blockMinMax;
    private double movMaxFixMin;
    private double movMinFixMax;
    private Vector firstContactPoint;

    public PhysCollisionObject(Polygon movable_, Polygon stationary, Vector axes) {
        collision_normal = axes;
        movable = movable_;
        fixed = stationary;
        generateCollision();
    }

    public void generateCollision() {
        playerMinMax = VSMath.getMinMaxOfArray(movable.getProjectionOnVector(collision_normal));
        blockMinMax = VSMath.getMinMaxOfArray(fixed.getProjectionOnVector(collision_normal));
        movMaxFixMin = playerMinMax[0] - blockMinMax[1];
        movMinFixMax = playerMinMax[1] - blockMinMax[0];
        if (movMaxFixMin > 0 || movMinFixMax < 0) {
            seperated = true;
            penetrationDistance = 0.0D;
            return;
        }
        // Set the penetration to be the smaller distance
        if (Math.abs(movMaxFixMin) > Math.abs(movMinFixMax)) {
            penetrationDistance = movMinFixMax;
            for (Vector v : movable.getVertices()) {
                if (v.dot(collision_normal) == playerMinMax[1]) {
                    firstContactPoint = v;
                }
            }
        } else {
            penetrationDistance = movMaxFixMin;
            for (Vector v : movable.getVertices()) {
                if (v.dot(collision_normal) == playerMinMax[0]) {
                    firstContactPoint = v;
                }
            }
        }
        seperated = false;
    }

    public Vector getSecondContactPoint() {
        if (Math.abs(movMaxFixMin) > Math.abs(movMinFixMax)) {
            for (Vector v : fixed.getVertices()) {
                if (v.dot(collision_normal) == blockMinMax[0]) {
                    return v;
                }
            }
        } else {
            for (Vector v : fixed.getVertices()) {
                if (v.dot(collision_normal) == blockMinMax[1]) {
                    return v;
                }
            }
        }
        return null;
    }

    public Vector getResponse() {
        return collision_normal.getProduct(penetrationDistance);
    }

    public void setResponse(Vector v) {
        v.x = collision_normal.x * penetrationDistance;
        v.y = collision_normal.y * penetrationDistance;
        v.z = collision_normal.z * penetrationDistance;
    }
}