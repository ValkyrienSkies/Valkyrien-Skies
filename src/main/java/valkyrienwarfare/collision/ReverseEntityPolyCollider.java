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

package valkyrienwarfare.collision;

import valkyrienwarfare.api.Vector;

/**
 * Heavily modified version of the original Polygon Collider, with checks on polygons passing through eachother, normals not to be considered, and a consideration for Net Velocity between the polygons
 *
 * @author thebest108
 */
public class ReverseEntityPolyCollider {

    public Vector[] potentialSeperatingAxes;
    public boolean seperated = false;
    public ReverseEntityCollisionObject[] collisions;
    public int minDistanceIndex;
    public double minDistance;
    public Polygon entity;
    public Polygon block;
    public Vector entityVelocity;
    public boolean originallySeperated;
    public int minIndexInReverse;

    public ReverseEntityPolyCollider(Polygon movable, Polygon stationary, Vector[] axes, Vector entityVel) {
        potentialSeperatingAxes = axes;
        entity = movable;
        block = stationary;
        entityVelocity = entityVel;
        processData();
    }

    public void processData() {
        seperated = false;
        collisions = new ReverseEntityCollisionObject[potentialSeperatingAxes.length];
        for (int i = 0; i < collisions.length; i++) {
            if (!seperated) {
                collisions[i] = new ReverseEntityCollisionObject(entity, block, potentialSeperatingAxes[i], entityVelocity);
                if (collisions[i].seperated) {
                    seperated = true;
                    break;
                }
            }
        }
        if (!seperated) {
            minDistance = 420;
            double minNegativeImpactTime = 0D;
            for (int i = 0; i < collisions.length; i++) {
                // Take the collision response closest to 0
                if (Math.abs(collisions[i].penetrationDistance) < minDistance) {
                    minDistanceIndex = i;
                    minDistance = Math.abs(collisions[i].penetrationDistance);
                }
                double velDot = collisions[i].axis.dot(entityVelocity);
                double negativeTime = -velDot / collisions[i].penetrationDistance;
                if (negativeTime < minNegativeImpactTime) {
                    minNegativeImpactTime = negativeTime;
                    minIndexInReverse = i;
                }
            }
            if (entityVelocity.isZero()) {
                minIndexInReverse = minDistanceIndex;
            }
        }
    }

}