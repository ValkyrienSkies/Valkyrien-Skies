package org.valkyrienskies.mod.common.physics.collision;

import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.collision.polygons.Polygon;

/**
 * An enhanced version of the collision Object, designed to prevent entities from moving through a
 * polygon
 *
 * @author thebest108
 */
public class EntityCollisionObject {

    private final Vector axis;
    private final Polygon movable, fixed;
    private double penetrationDistance;
    private boolean separated;
    private double[] playerMinMax;
    private double[] blockMinMax;
    private Vector entityVelocity;
    private boolean originallyCollided;
    private double velDot;

    public EntityCollisionObject(Polygon movable_, Polygon stationary, Vector axes,
        Vector entityVel) {
        axis = axes;
        movable = movable_;
        fixed = stationary;
        entityVelocity = entityVel;
        originallyCollided = false;
        generateCollision();
    }

    public void generateCollision() {
        velDot = -entityVelocity.dot(axis);
        // playerMinMax =
        // BigBastardMath.getMinMaxOfArray(movable.getProjectionOnVector(axis));
        // blockMinMax =
        // BigBastardMath.getMinMaxOfArray(fixed.getProjectionOnVector(axis));
        // double movMaxFixMin = playerMinMax[0]-blockMinMax[1];
        // double movMinFixMax = playerMinMax[1]-blockMinMax[0];

        // NOTE: This code isnt compatible or readable, but its faster
        double dot = axis.dot(movable.getVertices()[0]);
        double playerMin = dot, playerMax = dot;
        for (int i = 1; i < movable.getVertices().length; i++) {
            dot = axis.dot(movable.getVertices()[i]);
            if (dot < playerMin) {
                playerMin = dot;
            }
            if (dot > playerMax) {
                playerMax = dot;
            }
        }

        dot = axis.dot(fixed.getVertices()[0]);
        double blockMin = dot, blockMax = dot;
        for (int i = 1; i < fixed.getVertices().length; i++) {
            dot = axis.dot(fixed.getVertices()[i]);
            if (dot < blockMin) {
                blockMin = dot;
            }
            if (dot > blockMax) {
                blockMax = dot;
            }
        }

        double movMaxFixMin = playerMin - blockMax;
        double movMinFixMax = playerMax - blockMin;

        boolean useDefault = true;
        if (movMaxFixMin > 0 || movMinFixMax < 0) {
            // Original position not colliding, use velocity based bastards
            useDefault = false;
        } else {
            originallyCollided = true;
        }
        if (velDot > 0) {
            movMaxFixMin -= velDot;
        } else {
            movMinFixMax -= velDot;
        }
        if (movMaxFixMin > 0 || movMinFixMax < 0) {
            separated = true;
            penetrationDistance = 0.0D;
            return;
        }
        // Set the penetration to be the smaller distance
        if (useDefault || velDot == 0D) {
            if (Math.abs(movMaxFixMin) < Math.abs(movMinFixMax)) {
                penetrationDistance = movMaxFixMin;
            } else {
                penetrationDistance = movMinFixMax;
            }
        } else {
            if (Math.signum(velDot) != Math.signum(movMinFixMax)) {
                penetrationDistance = movMinFixMax;
            } else {
                penetrationDistance = movMaxFixMin;
            }
        }
        separated = false;
    }

    public Vector getResponse() {
        return axis.getProduct(-penetrationDistance);
    }

    public Vector getCollisionNormal() {
        return axis;
    }

    public double getCollisionPenetrationDistance() {
        return penetrationDistance;
    }

    public boolean arePolygonsSeperated() {
        return separated;
    }

    public boolean werePolygonsInitiallyColliding() {
        return originallyCollided;
    }

    public double getVelDot() {
        return velDot;
    }
}