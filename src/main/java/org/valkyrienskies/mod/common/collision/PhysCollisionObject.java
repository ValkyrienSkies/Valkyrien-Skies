package org.valkyrienskies.mod.common.collision;

import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.mod.common.util.VSMath;

public class PhysCollisionObject {

    public final Vector3dc collision_normal;
    public final Polygon movable, fixed;
    public double penetrationDistance;
    public boolean seperated;
    private double[] playerMinMax;
    private double[] blockMinMax;
    private double movMaxFixMin;
    private double movMinFixMax;
    private Vector3dc firstContactPoint;

    public PhysCollisionObject(Polygon movable_, Polygon stationary, Vector3dc axes) {
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
            for (Vector3dc v : movable.getVertices()) {
                if (v.dot(collision_normal) == playerMinMax[1]) {
                    firstContactPoint = v;
                }
            }
        } else {
            penetrationDistance = movMaxFixMin;
            for (Vector3dc v : movable.getVertices()) {
                if (v.dot(collision_normal) == playerMinMax[0]) {
                    firstContactPoint = v;
                }
            }
        }
        seperated = false;
    }

    /*
    public Vector3dc getSecondContactPoint() {
        if (Math.abs(movMaxFixMin) > Math.abs(movMinFixMax)) {
            for (Vector3dc v : fixed.getVertices()) {
                if (v.dot(collision_normal) == blockMinMax[0]) {
                    return v;
                }
            }
        } else {
            for (Vector3dc v : fixed.getVertices()) {
                if (v.dot(collision_normal) == blockMinMax[1]) {
                    return v;
                }
            }
        }
        return null;
    }
     */

    public Vector3d getResponse() {
        return collision_normal.mul(penetrationDistance, new Vector3d());
    }

}