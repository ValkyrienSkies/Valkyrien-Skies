/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2017 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package valkyrienwarfare.collision;

import valkyrienwarfare.api.Vector;
import valkyrienwarfare.math.BigBastardMath;

/**
 * An enhanced version of the collision Object, designed to prevent entities from moving through a polygon
 *
 * @author thebest108
 */
public class ReverseEntityCollisionObject {

	public final Vector axis;
	public final Polygon movable, fixed;
	public double penetrationDistance;
	public boolean seperated;
	public double[] playerMinMax;
	public double[] blockMinMax;
	public Vector entityVelocity;

	public ReverseEntityCollisionObject(Polygon movable_, Polygon stationary, Vector axes, Vector entityVel) {
		axis = axes;
		movable = movable_;
		fixed = stationary;
		entityVelocity = entityVel;
		generateCollision();
	}

	public void generateCollision() {
		// velDot = -entityVelocity.dot(axis);
		playerMinMax = BigBastardMath.getMinMaxOfArray(movable.getProjectionOnVector(axis));
		blockMinMax = BigBastardMath.getMinMaxOfArray(fixed.getProjectionOnVector(axis));
		double movMaxFixMin = playerMinMax[0] - blockMinMax[1];
		double movMinFixMax = playerMinMax[1] - blockMinMax[0];
	    /*
		 * if(velDot>0){ movMaxFixMin-=velDot; }else{ movMinFixMax-=velDot; // }
		 */
		if (movMaxFixMin > 0 || movMinFixMax < 0) {
			seperated = true;
			penetrationDistance = 0.0D;
			return;
		}
		// Set the penetration to be the smaller distance
		if (Math.abs(movMaxFixMin) < Math.abs(movMinFixMax)) {
			penetrationDistance = movMaxFixMin;
		} else {
			penetrationDistance = movMinFixMax;
		}
		seperated = false;
	}

	public Vector getResponse() {
		return axis.getProduct(-penetrationDistance);
	}
}