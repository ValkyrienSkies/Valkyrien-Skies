package org.valkyrienskies.mod.common.collision;

import org.joml.Vector3dc;

public class PolygonCollisionPointFinder {

    public static Vector3dc[] getPointsOfCollisionForPolygons(PhysCollisionObject collisionInfo) {
        Polygon topPoly = null;
        Polygon bottomPoly = null;
        Vector3dc collisionNormal = collisionInfo.collision_normal;

        Vector3dc centerDifference = collisionInfo.movable.getCenter()
            .sub(collisionInfo.fixed.getCenter());
        if (centerDifference.dot(collisionNormal) > 0) {
            // Then the movable is the bottom
            topPoly = collisionInfo.fixed;
            bottomPoly = collisionInfo.movable;
        } else {
            // Then the fixed is the bottom
            topPoly = collisionInfo.movable;
            bottomPoly = collisionInfo.fixed;
        }

        // First find the top point and bottom point:
        double minDot = 99999999D;
        int topPointIndex = -1;
        for (int i = 0; i < topPoly.getVertices().length; i++) {
            double dotProduct = topPoly.getVertices()[i].dot(collisionNormal);
            if (dotProduct < minDot) {
                minDot = dotProduct;
                topPointIndex = i;
            }
        }

        double maxDot = -9999999999D; // Double.MIN_VALUE;
        int bottomPointIndex = -1;
        for (int i = 0; i < bottomPoly.getVertices().length; i++) {
            double dotProduct = bottomPoly.getVertices()[i].dot(collisionNormal);
            if (dotProduct > maxDot) {
                maxDot = dotProduct;
                bottomPointIndex = i;
            }
        }

        Vector3dc currentTopVertex = topPoly.getVertices()[topPointIndex];
        Vector3dc currentBottomVertex = bottomPoly.getVertices()[bottomPointIndex];

        // TODO: We're oversolving for the collision here, but it prevents things going through eachother.
        return new Vector3dc[]{currentTopVertex, currentBottomVertex, currentTopVertex,
                currentBottomVertex};
    }

}