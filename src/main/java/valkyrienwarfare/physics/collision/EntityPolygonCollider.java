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

package valkyrienwarfare.physics.collision;

import valkyrienwarfare.api.Vector;

/**
 * Heavily modified version of the original Polygon Collider, with checks on polygons passing through eachother, normals not to be considered, and a consideration for Net Velocity between the polygons
 *
 * @author thebest108
 */
public class EntityPolygonCollider {

    public Vector[] potentialSeperatingAxes;
    public boolean seperated = false;
    public EntityCollisionObject[] collisions;
    public int minDistanceIndex;
    public double minDistance;
    public EntityPolygon entity;
    public Polygon block;
    public Vector entityVelocity;
    public boolean originallySeperated;

    public EntityPolygonCollider(EntityPolygon movable, Polygon stationary, Vector[] axes, Vector entityVel) {
        potentialSeperatingAxes = axes;
        entity = movable;
        block = stationary;
        entityVelocity = entityVel;
        processData();
    }

    public void processData() {
        seperated = false;
        collisions = new EntityCollisionObject[potentialSeperatingAxes.length];
        for (int i = 0; i < collisions.length; i++) {
            if (!seperated) {
                collisions[i] = new EntityCollisionObject(entity, block, potentialSeperatingAxes[i], entityVelocity);
                if (collisions[i].seperated) {
                    seperated = true;
                    break;
                }
                if (!collisions[i].originallyCollided) {
                    originallySeperated = true;
                }
            }
        }
        if (!seperated) {
            minDistance = 420;
            for (int i = 0; i < collisions.length; i++) {
                if (originallySeperated) {
                    if (Math.abs((collisions[i].penetrationDistance - collisions[i].velDot) / collisions[i].velDot) < minDistance && !collisions[i].originallyCollided) {
                        minDistanceIndex = i;
                        minDistance = Math.abs((collisions[i].penetrationDistance - collisions[i].velDot) / collisions[i].velDot);
                    }
                } else {
                    // System.out.println("wtf happened here");
                    if (Math.abs(collisions[i].penetrationDistance) < minDistance) {
                        minDistanceIndex = i;
                        minDistance = Math.abs(collisions[i].penetrationDistance);
                    }
                }
            }
        }
    }

}