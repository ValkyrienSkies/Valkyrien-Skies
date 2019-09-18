/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2019 the Valkyrien Skies team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Skies team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Skies team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package org.valkyrienskies.mod.common.physics.collision.polygons;

import org.valkyrienskies.mod.common.math.Vector;

public class PhysPolygonCollider {

    public Vector[] potentialSeperatingAxes = null;
    public boolean seperated = false;
    public PhysCollisionObject[] collisions = null;
    public int minDistanceIndex;
    public double minDistance;
    public Polygon entity;
    public Polygon block;

    public PhysPolygonCollider(Polygon movable, Polygon stationary, Vector[] axes) {
        potentialSeperatingAxes = axes;
        entity = movable;
        block = stationary;
        processData();
    }

    // TODO: Fix this, processes the penetration distances backwards from their reality
    public void processData() {
        collisions = new PhysCollisionObject[potentialSeperatingAxes.length];
        for (int i = 0; i < potentialSeperatingAxes.length && !seperated; i++) {
            collisions[i] = new PhysCollisionObject(entity, block, potentialSeperatingAxes[i]);
            seperated = collisions[i].seperated;
        }
        if (!seperated) {
            minDistance = 420;
            for (int i = 0; i < potentialSeperatingAxes.length; i++) {
                // Take the collision response closest to 0
                if (Math.abs(collisions[i].penetrationDistance) < minDistance) {
                    minDistanceIndex = i;
                    minDistance = Math.abs(collisions[i].penetrationDistance);
                }
            }
        }
    }

}