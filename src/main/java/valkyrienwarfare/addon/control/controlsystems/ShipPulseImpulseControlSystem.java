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

package valkyrienwarfare.addon.control.controlsystems;

import java.util.Set;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import valkyrienwarfare.addon.control.nodenetwork.Node;
import valkyrienwarfare.addon.control.tileentity.ThrustModulatorTileEntity;
import valkyrienwarfare.addon.control.tileentity.TileEntityNormalEtherCompressor;
import valkyrienwarfare.api.RotationMatrices;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.api.block.ethercompressor.TileEntityEtherCompressor;
import valkyrienwarfare.math.BigBastardMath;
import valkyrienwarfare.physics.calculations.PhysicsCalculations;

public class ShipPulseImpulseControlSystem {

    private final ThrustModulatorTileEntity parentTile;
    private double linearVelocityBias = 1D;
    private double angularVelocityBias = 50D;
    private double angularConstant = 500000000D;
    private double linearConstant = 1000000D;
    private double bobspeed = 10D;
    private double bobmagnitude = 3D;
    private double totalSecondsRunning = 0D;
    private Vector normalVector = new Vector(0, 1, 0);

    public ShipPulseImpulseControlSystem(ThrustModulatorTileEntity parentTile) {
        this.parentTile = parentTile;
        totalSecondsRunning = Math.random() * bobspeed;
    }

    public void solveThrustValues(PhysicsCalculations calculations) {
        double totalThrust = 0;

        double totalPotentialThrust = getMaxThrustForAllThrusters();
        double currentThrust = getTotalThrustForAllThrusters();

        double[] rotationMatrix = calculations.parent.coordTransform.lToWRotation;
        double[] rotationAndTranslationMatrix = calculations.parent.coordTransform.lToWTransform;
        double[] invRotationAndTranslationMatrix = calculations.parent.coordTransform.wToLTransform;
        double[] invMOIMatrix = calculations.invFramedMOI;

        Vector posInWorld = new Vector(calculations.parent.wrapper.posX, calculations.parent.wrapper.posY, calculations.parent.wrapper.posZ);
        Vector angularVelocity = new Vector(calculations.angularVelocity);
        Vector linearMomentum = new Vector(calculations.linearMomentum);
        Vector linearVelocity = new Vector(linearMomentum, calculations.getInvMass());

        BlockPos shipRefrencePos = calculations.parent.refrenceBlockPos;

        double maxYDelta = parentTile.maximumYVelocity;
        double idealHeight = parentTile.idealYHeight + getBobForTime();

        Vector linearMomentumError = getIdealMomentumErrorForSystem(calculations, posInWorld, maxYDelta, idealHeight);

        double engineThrustToChange = linearMomentumError.Y;

        double newTotalThrust = currentThrust + engineThrustToChange;

        if (!(newTotalThrust > 0 && newTotalThrust < totalPotentialThrust)) {
//			System.out.println("Current result impossible");
        }

        double linearThama = 4.5D;
        double angularThama = 1343.5D;

        Vector theNormal = new Vector(0, 1, 0);

        Vector idealNormal = new Vector(theNormal);
        Vector currentNormal = new Vector(theNormal, calculations.parent.coordTransform.lToWRotation);

        Vector currentNormalError = currentNormal.getSubtraction(idealNormal);

        linearVelocityBias = calculations.getPhysTickSpeed();

        for (Node node : getNetworkedNodesList()) {
            if (node.getParentTile() instanceof TileEntityEtherCompressor && !((TileEntityEtherCompressor) node.getParentTile()).updateParentShip()) {
                TileEntityEtherCompressor forceTile = (TileEntityEtherCompressor) node.getParentTile();

                Vector angularVelocityAtNormalPosition = angularVelocity.cross(currentNormalError);

                forceTile.updateTicksSinceLastRecievedSignal();

                //Assume zero change
                double currentErrorY = (posInWorld.Y - idealHeight) + linearThama * (linearMomentum.Y * calculations.getInvMass());

                double currentEngineErrorAngularY = getEngineDistFromIdealAngular(forceTile.getPos(), rotationAndTranslationMatrix, angularVelocity, calculations.centerOfMass, calculations.getPhysTickSpeed());


                Vector potentialMaxForce = new Vector(0, forceTile.getMaxThrust(), 0);
                potentialMaxForce.multiply(calculations.getInvMass());
                potentialMaxForce.multiply(calculations.getPhysTickSpeed());
                Vector potentialMaxThrust = forceTile.getPositionInLocalSpaceWithOrientation().cross(potentialMaxForce);
                RotationMatrices.applyTransform3by3(invMOIMatrix, potentialMaxThrust);
                potentialMaxThrust.multiply(calculations.getPhysTickSpeed());

                double futureCurrentErrorY = currentErrorY + linearThama * potentialMaxForce.Y;
                double futureEngineErrorAngularY = getEngineDistFromIdealAngular(forceTile.getPos(), rotationAndTranslationMatrix, angularVelocity.getAddition(potentialMaxThrust), calculations.centerOfMass, calculations.getPhysTickSpeed());


                boolean doesForceMinimizeError = false;

                if (Math.abs(futureCurrentErrorY) < Math.abs(currentErrorY) && Math.abs(futureEngineErrorAngularY) < Math.abs(currentEngineErrorAngularY)) {
                    doesForceMinimizeError = true;
                    if (Math.abs(linearMomentum.Y * calculations.getInvMass()) > maxYDelta) {
                        if (Math.abs((potentialMaxForce.Y + linearMomentum.Y) * calculations.getInvMass()) > Math.abs(linearMomentum.Y * calculations.getInvMass())) {
                            doesForceMinimizeError = false;
                        }
                    } else {
                        if (Math.abs((potentialMaxForce.Y + linearMomentum.Y) * calculations.getInvMass()) > maxYDelta) {
                            doesForceMinimizeError = false;
                        }
                    }

                }

                if (doesForceMinimizeError) {
                    forceTile.setThrust(forceTile.getMaxThrust());
                    if (Math.abs(currentErrorY) < 1D) {
                        forceTile.setThrust(forceTile.getMaxThrust() * Math.pow(Math.abs(currentErrorY), 3D));
                    }
                } else {
                    forceTile.setThrust(0);
                }

                Vector forceOutputWithRespectToTime = forceTile.getForceOutputOriented(calculations.getPhysTickSpeed());
                linearMomentum.add(forceOutputWithRespectToTime);
                Vector torque = forceTile.getPositionInLocalSpaceWithOrientation().cross(forceOutputWithRespectToTime);
                RotationMatrices.applyTransform3by3(invMOIMatrix, torque);
                angularVelocity.add(torque);
            }
        }




		/*for(Node node : getNetworkedNodesList()) {
            if(node.parentTile instanceof TileEntityEtherCompressor && !((TileEntityEtherCompressor) node.parentTile).updateParentShip()) {
				TileEntityEtherCompressor forceTile = (TileEntityEtherCompressor) node.parentTile;

				Vector tileForce = getForceForEngine(forceTile, forceTile.getPos(), calculations.invMass, linearMomentum, angularVelocity, rotationAndTranslationMatrix, posInWorld, calculations.centerOfMass, calculations.physTickSpeed);

				tileForce.multiply(1D / calculations.physTickSpeed);

				Vector forcePos = forceTile.getPositionInLocalSpaceWithOrientation();

				double tileForceMagnitude = tileForce.length();

				forceTile.setThrust(BigBastardMath.limitToRange(tileForceMagnitude, 0D, forceTile.getMaxThrust()));

				Vector forceOutputWithRespectToTime = forceTile.getForceOutputOriented(calculations.physTickSpeed);

				linearMomentum.add(forceOutputWithRespectToTime);

				Vector torque = forceTile.getPositionInLocalSpaceWithOrientation().cross(forceOutputWithRespectToTime);
				RotationMatrices.applyTransform3by3(invMOIMatrix, torque);
				angularVelocity.add(torque);
			}
		}*/

        totalSecondsRunning += calculations.getPhysTickSpeed();
    }

    private double getBobForTime() {
        double fraction = totalSecondsRunning / bobspeed;

        double degrees = (fraction * 360D) % 360D;

        double sinVal = Math.sin(Math.toRadians(degrees));

//		sinVal = math.signum(sinVal) * math.pow(math.abs(sinVal), 1.5D);

        return sinVal * bobmagnitude;
    }

    public Vector getIdealMomentumErrorForSystem(PhysicsCalculations calculations, Vector posInWorld, double maxYDelta, double idealHeight) {
        double yErrorDistance = idealHeight - posInWorld.Y;
        double idealYLinearMomentumMagnitude = BigBastardMath.limitToRange(yErrorDistance, -maxYDelta, maxYDelta);
        Vector idealLinearMomentum = new Vector(0, 1, 0);
        idealLinearMomentum.multiply(idealYLinearMomentumMagnitude * calculations.getMass());
        Vector linearMomentumError = calculations.linearMomentum.getSubtraction(idealLinearMomentum);
        return linearMomentumError;
    }

    public Vector getForceForEngine(TileEntityEtherCompressor engine, BlockPos enginePos, double invMass, Vector linearMomentum, Vector angularVelocity, double[] rotationAndTranslationMatrix, Vector shipPos, Vector centerOfMass, double secondsToApply, double idealHeight) {
        double stabilityVal = .145D;

        Vector shipVel = new Vector(linearMomentum);

        shipVel.multiply(invMass);

        double linearDist = -getControllerDistFromIdealY(rotationAndTranslationMatrix, invMass, shipPos.Y, linearMomentum, idealHeight);
        double angularDist = -getEngineDistFromIdealAngular(enginePos, rotationAndTranslationMatrix, angularVelocity, centerOfMass, secondsToApply);

        engine.angularThrust.Y -= (angularConstant * secondsToApply) * angularDist;
        engine.linearThrust.Y -= (linearConstant * secondsToApply) * linearDist;

        engine.angularThrust.Y = Math.max(engine.angularThrust.Y, 0D);
        engine.linearThrust.Y = Math.max(engine.linearThrust.Y, 0D);

        engine.angularThrust.Y = Math.min(engine.angularThrust.Y, engine.getMaxThrust() * stabilityVal);
        engine.linearThrust.Y = Math.min(engine.linearThrust.Y, engine.getMaxThrust() * (1D - stabilityVal));

        Vector aggregateForce = engine.linearThrust.getAddition(engine.angularThrust);
        aggregateForce.multiply(secondsToApply);

        return aggregateForce;
    }

    public double getEngineDistFromIdealAngular(BlockPos enginePos, double[] lToWRotation, Vector angularVelocity, Vector centerOfMass, double secondsToApply) {
        BlockPos pos = parentTile.getPos();

        Vector controllerPos = new Vector(pos.getX() + .5D, pos.getY() + .5D, pos.getZ() + .5D);
        Vector enginePosVec = new Vector(enginePos.getX() + .5D, enginePos.getY() + .5D, enginePos.getZ() + .5D);

        controllerPos.subtract(centerOfMass);
        enginePosVec.subtract(centerOfMass);

        Vector unOrientedPosDif = new Vector(enginePosVec.X - controllerPos.X, enginePosVec.Y - controllerPos.Y, enginePosVec.Z - controllerPos.Z);

        double idealYDif = unOrientedPosDif.dot(normalVector);

        RotationMatrices.doRotationOnly(lToWRotation, controllerPos);
        RotationMatrices.doRotationOnly(lToWRotation, enginePosVec);

        double inWorldYDif = enginePosVec.Y - controllerPos.Y;

        Vector angularVelocityAtPoint = angularVelocity.cross(enginePosVec);
        angularVelocityAtPoint.multiply(secondsToApply);

        return idealYDif - (inWorldYDif + angularVelocityAtPoint.Y * angularVelocityBias);
    }

    public double getControllerDistFromIdealY(double[] lToWTransform, double invMass, double posY, Vector linearMomentum, double idealHeight) {
        BlockPos pos = parentTile.getPos();
        Vector controllerPos = new Vector(pos.getX() + .5D, pos.getY() + .5D, pos.getZ() + .5D);
        controllerPos.transform(lToWTransform);
        return idealHeight - (posY + (linearMomentum.Y * invMass * linearVelocityBias));
    }

    public double getTotalThrustForAllThrusters() {
        double totalThrust = 0D;
        for (Node otherNode : getNetworkedNodesList()) {
            TileEntity nodeTile = otherNode.getParentTile();
            if (nodeTile instanceof TileEntityNormalEtherCompressor) {
                TileEntityNormalEtherCompressor ether = (TileEntityNormalEtherCompressor) nodeTile;
                totalThrust += ether.getThrust();
            }
        }
        return totalThrust;
    }

    public double getMaxThrustForAllThrusters() {
        double totalThrustAvaliable = 0D;
        for (Node otherNode : getNetworkedNodesList()) {
            TileEntity nodeTile = otherNode.getParentTile();
            if (nodeTile instanceof TileEntityNormalEtherCompressor) {
                TileEntityNormalEtherCompressor ether = (TileEntityNormalEtherCompressor) nodeTile;
                totalThrustAvaliable += ether.getMaxThrust();
            }
        }
        return totalThrustAvaliable;
    }

    private Set<Node> getNetworkedNodesList() {
        return parentTile.tileNode.getNodeNetwork().getNetworkedNodes();
    }

}
