package valkyrienwarfare.physics.collision;

import valkyrienwarfare.api.Vector;

public class PolygonCollisionPointFinder {

    public static Vector[] getPointsOfCollisionForPolygons(PhysCollisionObject collisionInfo) {
        Polygon topPoly = null;
        Polygon bottomPoly = null;
        Vector collisionNormal = collisionInfo.collision_normal;
        
        Vector centerDifference = collisionInfo.movable.getCenter().getSubtraction(collisionInfo.fixed.getCenter());
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
        double minDot = Double.MAX_VALUE;
        int topPointIndex = -1;
        for (int i = 0; i < topPoly.getVertices().length; i++) {
            double dotProduct = topPoly.getVertices()[i].dot(collisionNormal);
            if (dotProduct < minDot) {
                minDot = dotProduct;
                topPointIndex = i;
            }
        }
        
        double maxDot = -9999999999D; //Double.MIN_VALUE;
        int bottomPointIndex = -1;
        for (int i = 0; i < bottomPoly.getVertices().length; i++) {
            double dotProduct = bottomPoly.getVertices()[i].dot(collisionNormal);
            if (dotProduct > maxDot) {
                maxDot = dotProduct;
                bottomPointIndex = i;
            }
        }
        
        Vector currentTopVertice = topPoly.getVertices()[topPointIndex];
        Vector currentBottomVertice = bottomPoly.getVertices()[bottomPointIndex];
        
        boolean useFastCollision = true;
        
        if (useFastCollision) {
            return new Vector[] {currentTopVertice, currentBottomVertice};
        } else {
            // Now use the surface normals that are most perpendicular to the collision normals for
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
            
            ClippedPolygon clippedTop = new ClippedPolygon(topPoly, bottomCullingNormal, currentBottomVertice);
            ClippedPolygon clippedBottom = new ClippedPolygon(bottomPoly, topCullingNormal, currentTopVertice);
            
            // Now with our normals found and the plane vertice, we now search for the points of collision.
            
            return new Vector[] {currentTopVertice, currentBottomVertice};
        }
    }

}