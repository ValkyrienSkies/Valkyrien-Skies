package org.valkyrienskies.mod.common.physics.collision.polygons;

import org.valkyrienskies.mod.common.math.Vector;

public class PolygonCollisionPointFinder {

    public static Vector[] getPointsOfCollisionForPolygons(PhysCollisionObject collisionInfo) {
        Polygon topPoly = null;
        Polygon bottomPoly = null;
        Vector collisionNormal = collisionInfo.collision_normal;

        Vector centerDifference = collisionInfo.movable.getCenter()
            .getSubtraction(collisionInfo.fixed.getCenter());
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

        Vector currentTopVertex = topPoly.getVertices()[topPointIndex];
        Vector currentBottomVertex = bottomPoly.getVertices()[bottomPointIndex];

        boolean useFastCollision = true;

        if (useFastCollision) {
            // TODO: We're oversolving for the collision here, but it prevents things going through eachother.
            return new Vector[]{currentTopVertex, currentBottomVertex, currentTopVertex,
                currentBottomVertex};
            // return new Vector[] { currentTopVertex, currentBottomVertex };
        } else {
            // Now use the surface normals that are most perpendicular to the collision
            // normals for
            // culling.
            Vector topCullingNormal = null;
            double maxTopAbsDot = -1;
            for (Vector topNormal : topPoly.getNormals()) {
                double absDotProduct = Math.abs(topNormal.dot(collisionNormal));
                if (absDotProduct > maxTopAbsDot) {
                    topCullingNormal = topNormal;
                    maxTopAbsDot = absDotProduct;
                }
            }

            Vector bottomCullingNormal = null;
            double maxBottomAbsDot = -1;
            for (Vector bottomNormal : bottomPoly.getNormals()) {
                double absDotProduct = Math.abs(bottomNormal.dot(collisionNormal));
                if (absDotProduct > maxBottomAbsDot) {
                    bottomCullingNormal = bottomNormal;
                    maxBottomAbsDot = absDotProduct;
                }
            }
            // Make sure the normals are facing in the right direction
            if (topCullingNormal.dot(collisionNormal) > 0) {
                topCullingNormal = new Vector(topCullingNormal, -1);
            }

            if (bottomCullingNormal.dot(collisionNormal) < 0) {
                bottomCullingNormal = new Vector(bottomCullingNormal, -1);
            }

            ClippedPolygon clippedTop = new ClippedPolygon(topPoly, bottomCullingNormal,
                currentBottomVertex);
            ClippedPolygon clippedBottom = new ClippedPolygon(bottomPoly, topCullingNormal,
                currentTopVertex);

            // Now with our normals found and the plane vertex, we now search for the
            // points of collision.
            // TODO: Find a real algorithm for this please!
            return new Vector[]{currentTopVertex, currentBottomVertex, currentTopVertex,
                currentBottomVertex};
        }
    }

}