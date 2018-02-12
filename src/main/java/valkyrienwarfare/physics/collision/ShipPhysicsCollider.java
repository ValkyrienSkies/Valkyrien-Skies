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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import valkyrienwarfare.api.RotationMatrices;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.math.BigBastardMath;
import valkyrienwarfare.physics.calculations.PhysicsCalculations;
import valkyrienwarfare.physics.calculations.PhysicsCalculationsOrbital;
import valkyrienwarfare.physics.management.PhysicsObject;

public class ShipPhysicsCollider {

	public static double axisTolerance = .3D;
	public PhysicsCalculations calculator;
	public World worldObj;
	public PhysicsObject parent;
	public double e = .35D;
	private ArrayList<BlockPos> cachedPotentialHits;
	// Ensures this always updates the first tick after creation
	private double ticksSinceCacheUpdate = 420;

	public ShipPhysicsCollider(PhysicsCalculations calculations) {
		calculator = calculations;
		parent = calculations.parent;
		worldObj = parent.worldObj;
	}

	public void doShipCollision(PhysicsObject toCollideWith) {
		// Don't process collision if either of them are phased
		if (toCollideWith.physicsProcessor instanceof PhysicsCalculationsOrbital) {
			if (((PhysicsCalculationsOrbital) toCollideWith.physicsProcessor).isOrbitalPhased) {
				return;
			}
		}
		if (parent.physicsProcessor instanceof PhysicsCalculationsOrbital) {
			if (((PhysicsCalculationsOrbital) parent.physicsProcessor).isOrbitalPhased) {
				return;
			}
		}

		AxisAlignedBB firstBB = parent.getCollisionBoundingBox();
		AxisAlignedBB secondBB = toCollideWith.getCollisionBoundingBox();
		AxisAlignedBB betweenBB = BigBastardMath.getBetweenAABB(firstBB, secondBB);

		Polygon betweenBBPoly = new Polygon(betweenBB, toCollideWith.coordTransform.wToLTransform);

		List<AxisAlignedBB> bbsInFirst = parent.worldObj.getCollisionBoxes(parent.wrapper,
				betweenBBPoly.getEnclosedAABB());
		if (bbsInFirst.isEmpty()) {
			return;
		}

		Vector[] axes = parent.coordTransform.getSeperatingAxisWithShip(toCollideWith);
		Iterator<AxisAlignedBB> firstRandIter = bbsInFirst.iterator();// RandomIterator.getRandomIteratorForList(bbsInFirst);
		while (firstRandIter.hasNext()) {
			AxisAlignedBB fromIter = firstRandIter.next();

			Polygon firstInWorld = new Polygon(fromIter, toCollideWith.coordTransform.lToWTransform);

			AxisAlignedBB inWorldAABB = firstInWorld.getEnclosedAABB();

			Polygon inShip2Poly = new Polygon(inWorldAABB, parent.coordTransform.wToLTransform);

			// This is correct
			List<AxisAlignedBB> bbsInSecond = parent.worldObj.getCollisionBoxes(parent.wrapper,
					inShip2Poly.getEnclosedAABB());

			Iterator<AxisAlignedBB> secondRandIter = bbsInSecond.iterator();// RandomIterator.getRandomIteratorForList(bbsInSecond);

			while (secondRandIter.hasNext()) {
				// System.out.println("test");
				Polygon secondInWorld = new Polygon(secondRandIter.next(), parent.coordTransform.lToWTransform);

				// Both of these are in WORLD coordinates
				Vector firstCenter = firstInWorld.getCenter();
				Vector secondCenter = secondInWorld.getCenter();

				Vector inBodyFirst = new Vector(firstCenter.X - parent.wrapper.posX,
						firstCenter.Y - parent.wrapper.posY, firstCenter.Z - parent.wrapper.posZ);
				Vector inBodySecond = new Vector(secondCenter.X - toCollideWith.wrapper.posX,
						secondCenter.Y - toCollideWith.wrapper.posY, secondCenter.Z - toCollideWith.wrapper.posZ);

				Vector velAtFirst = parent.physicsProcessor.getVelocityAtPoint(inBodyFirst);
				Vector velAtSecond = toCollideWith.physicsProcessor.getVelocityAtPoint(inBodySecond);

				velAtFirst.subtract(velAtSecond);

				PhysPolygonCollider polyCol = new PhysPolygonCollider(firstInWorld, secondInWorld, axes);
				if (!polyCol.seperated) {

					Vector speedAtPoint = velAtFirst;

					double xDot = Math.abs(speedAtPoint.X);
					double yDot = Math.abs(speedAtPoint.Y);
					double zDot = Math.abs(speedAtPoint.Z);

					PhysCollisionObject polyColObj = null;

					// NOTE: This is all EXPERIMENTAL! Could possibly revert

					if (yDot > xDot && yDot > zDot) {
						// Y speed is greatest
						if (xDot > zDot) {
							polyColObj = polyCol.collisions[2];
						} else {
							polyColObj = polyCol.collisions[0];
						}
					} else {
						if (xDot > zDot) {
							// X speed is greatest
							polyColObj = polyCol.collisions[1];
						} else {
							// Z speed is greatest
							polyColObj = polyCol.collisions[1];
						}
					}

					// PhysCollisionObject polyColObj = polyCol.collisions[1];
					if (polyColObj.penetrationDistance > axisTolerance
							|| polyColObj.penetrationDistance < -axisTolerance) {
						polyColObj = polyCol.collisions[polyCol.minDistanceIndex];
					}

					// PhysCollisionObject physCol = new
					// PhysCollisionObject(firstInWorld,secondInWorld,polyColObj.axis);
					processCollisionAtPoint(toCollideWith, polyColObj);

					if (Math.abs(polyColObj.movMaxFixMin) > Math.abs(polyColObj.movMinFixMax)) {
						for (Vector v : polyColObj.movable.getVertices()) {
							if (v.dot(polyColObj.axis) == polyColObj.playerMinMax[1]) {
								polyColObj.firstContactPoint = v;
							}
						}
					} else {
						for (Vector v : polyColObj.movable.getVertices()) {
							if (v.dot(polyColObj.axis) == polyColObj.playerMinMax[0]) {
								polyColObj.firstContactPoint = v;
							}
						}
					}

					// physCol.firstContactPoint = physCol.getSecondContactPoint();
					processCollisionAtPoint(toCollideWith, polyColObj);
				}
			}
		}
	}

	private void processCollisionAtPoint(PhysicsObject toCollideWith, PhysCollisionObject object) {
		double e;
		Vector inFirstShip = new Vector(object.firstContactPoint);
		Vector inSecondShip = new Vector(object.firstContactPoint);
		// inFirstShip.subtract(firstController.centerOfMass);
		// inSecondShip.subtract(secondController.centerOfMass);
		// System.out.println(object.axis);
		// BigBastardMath.setInBodyWOFromInWorld(object.firstContactPoint,
		// parent.physicsProcessor.centerOfMass, parent.coordTransform.lToWRotation,
		// parent.coordTransform.wToLTransform,inFirstShip);

		// BigBastardMath.setInBodyWOFromInWorld(object.firstContactPoint,
		// toCollideWith.physicsProcessor.centerOfMass,
		// toCollideWith.coordTransform.lToWRotation,
		// toCollideWith.coordTransform.wToLTransform,inSecondShip);

		inFirstShip.X -= parent.wrapper.posX;
		inFirstShip.Y -= parent.wrapper.posY;
		inFirstShip.Z -= parent.wrapper.posZ;

		inSecondShip.X -= toCollideWith.wrapper.posX;
		inSecondShip.Y -= toCollideWith.wrapper.posY;
		inSecondShip.Z -= toCollideWith.wrapper.posZ;

		Vector momentumInFirst = parent.physicsProcessor.getVelocityAtPoint(inFirstShip);
		Vector momentumInSecond = toCollideWith.physicsProcessor.getVelocityAtPoint(inSecondShip);

		// COULD BE WRONG!!!
		Vector netVelocity = momentumInFirst.getSubtraction(momentumInSecond);

		e = .9;

		double topJ = -(e + 1D) * netVelocity.dot(object.axis);

		double bottomJ = parent.physicsProcessor.getInvMass() + toCollideWith.physicsProcessor.getInvMass();

		bottomJ += RotationMatrices
				.get3by3TransformedVec(toCollideWith.physicsProcessor.invFramedMOI, inFirstShip.cross(object.axis))
				.cross(inFirstShip).dot(object.axis);

		bottomJ += RotationMatrices
				.get3by3TransformedVec(toCollideWith.physicsProcessor.invFramedMOI, inSecondShip.cross(object.axis))
				.cross(inSecondShip).dot(object.axis);

		double j = topJ / bottomJ;

		Vector responseVec = new Vector(object.axis, j);
		// System.out.println(object.axis);
		if (responseVec.dot(object.getResponse()) < 0) {
			responseVec.multiply(-1D);
			parent.physicsProcessor.linearMomentum.add(responseVec);
			Vector cross = inFirstShip.cross(responseVec);
			RotationMatrices.applyTransform3by3(parent.physicsProcessor.invFramedMOI, cross);
			parent.physicsProcessor.angularVelocity.add(cross);

			responseVec.multiply(-1D);
			toCollideWith.physicsProcessor.linearMomentum.add(responseVec);
			cross = inSecondShip.cross(responseVec);
			RotationMatrices.applyTransform3by3(toCollideWith.physicsProcessor.invFramedMOI, cross);
			toCollideWith.physicsProcessor.angularVelocity.add(cross);
		}
	}

}
