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
import valkyrienwarfare.collision.PhysCollisionObject;
import valkyrienwarfare.collision.PhysPolygonCollider;

public class PolygonCollisionPointFinder {

    //TODO: This algorithm isn't correct, fix it later on!
    public static Vector[] getPointsOfCollisionForPolygons(PhysPolygonCollider collider, PhysCollisionObject somethingElse, Vector velocity) {

//		Vector axis = new Vector(toCollideWith.axis);

        double minSecondsAgo = 69D;
        int minCollisionIndex = 0;
        int minCollisionIndexIndex = 0;

        for (int cont = 0; cont < collider.collisions.length; cont++) {
            PhysCollisionObject toCollideWith = collider.collisions[cont];

            Vector axis = somethingElse.axis;
            double reverseVelocityAlongAxis = -velocity.dot(axis);
            double minDot = 9999999999999D;
            int minIndex = 0;

            for (int i = 0; i < 8; i++) {
                Vector vertice = toCollideWith.movable.vertices[i];
                double dot = vertice.dot(axis) * reverseVelocityAlongAxis;
                if (dot < minDot) {
                    minDot = dot;
                    minIndex = i;
                }
            }

            Vector contactPoint = toCollideWith.movable.vertices[minIndex];

            double secondsAgo = 69;

            if (Math.signum(reverseVelocityAlongAxis) == 1.0D) {
                secondsAgo = toCollideWith.movMinFixMax / reverseVelocityAlongAxis;
            } else {
                secondsAgo = toCollideWith.movMaxFixMin / reverseVelocityAlongAxis;
            }

            if (secondsAgo < minSecondsAgo) {
                minSecondsAgo = secondsAgo;
                minCollisionIndex = cont;
                minCollisionIndexIndex = minIndex;
            }
        }


//		System.out.println(contactPoint);

        Vector contactPoint = collider.collisions[minCollisionIndex].movable.vertices[minCollisionIndexIndex];//contactPoint.getAddition(axis.getProduct(reverseVelocityAlongAxis * secondsAgo));

        return new Vector[]{
                contactPoint
//				toCollideWith.firstContactPoint,
//				toCollideWith.getSecondContactPoint()
        };
    }


}
