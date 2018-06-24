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

package valkyrienwarfare.physics.collision.polygons;

import valkyrienwarfare.math.Vector;

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

        Vector currentTopVertice = topPoly.getVertices()[topPointIndex];
        Vector currentBottomVertice = bottomPoly.getVertices()[bottomPointIndex];

        boolean useFastCollision = true;

        if (useFastCollision) {
            // TODO: We're oversolving for the collision here, but it prevents things going through eachother.
            return new Vector[]{currentTopVertice, currentBottomVertice, currentTopVertice, currentBottomVertice};
            // return new Vector[] { currentTopVertice, currentBottomVertice };
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

            ClippedPolygon clippedTop = new ClippedPolygon(topPoly, bottomCullingNormal, currentBottomVertice);
            ClippedPolygon clippedBottom = new ClippedPolygon(bottomPoly, topCullingNormal, currentTopVertice);

            // Now with our normals found and the plane vertice, we now search for the
            // points of collision.
            // TODO: Find a real algorithm for this please!
            return new Vector[]{currentTopVertice, currentBottomVertice, currentTopVertice, currentBottomVertice};
        }
    }

}