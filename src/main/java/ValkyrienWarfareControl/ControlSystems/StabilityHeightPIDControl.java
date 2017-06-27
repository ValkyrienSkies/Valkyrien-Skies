package ValkyrienWarfareControl.ControlSystems;

import java.util.HashSet;

import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.API.Block.EtherCompressor.TileEntityEtherCompressor;
import ValkyrienWarfareBase.Math.BigBastardMath;
import ValkyrienWarfareBase.Physics.PhysicsCalculations;
import ValkyrienWarfareControl.NodeNetwork.Node;
import ValkyrienWarfareControl.TileEntity.ThrustModulatorTileEntity;
import ValkyrienWarfareControl.TileEntity.TileEntityNormalEtherCompressor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class StabilityHeightPIDControl {

	public final ThrustModulatorTileEntity parentTile;
	private double idealHeight = 25D;


	public double linearVelocityBias = 1D;
	public double angularVelocityBias = 50D;

    public double angularConstant = 500000000D;
    public double linearConstant = 1000000D;

    public double stabilityBias = .45D;
	private Vector normalVector = new Vector(0, 1, 0);

	public StabilityHeightPIDControl(ThrustModulatorTileEntity parentTile) {
		this.parentTile = parentTile;
	}

	public void solveThrustValues(PhysicsCalculations calculations) {
		//TODO: Implement magic algorithm here :(
//		double idealYPos = 25D;
		double physTickSpeed = calculations.physTickSpeed;
		double totalThrust = 0;
		double idealTotalThrust = calculations.mass * 9.8D;
		double totalPotentialThrust = getMaxThrustForAllThrusters();

		double maxYDelta = 5D;

		double[] rotationMatrix = calculations.parent.coordTransform.lToWRotation;
		double[] rotationAndTranslationMatrix = calculations.parent.coordTransform.lToWTransform;
		double[] invRotationAndTranslationMatrix = calculations.parent.coordTransform.wToLTransform;
		double[] invMOIMatrix = calculations.invFramedMOI;

		Vector posInWorld = new Vector(calculations.parent.wrapper.posX, calculations.parent.wrapper.posY, calculations.parent.wrapper.posZ);
		Vector angularVelocity = new Vector(calculations.angularVelocity);
		Vector linearMomentum = new Vector(calculations.linearMomentum);
		Vector linearVelocity = new Vector(linearMomentum, calculations.invMass);

		BlockPos shipRefrencePos = calculations.parent.refrenceBlockPos;

//		ArrayList<Node> nodeList = new ArrayList(getNetworkedNodesList());
//		Collections.shuffle(nodeList);

//		stabilityBias = idealTotalThrust / totalPotentialThrust;


		idealHeight = 35;

		Vector totalAngularForce = new Vector();

		for(Node node : getNetworkedNodesList()) {
			if(node.parentTile instanceof TileEntityEtherCompressor) {
				TileEntityEtherCompressor forceTile = (TileEntityEtherCompressor) node.parentTile;

				Vector tileForce = getForceForEngine(forceTile, forceTile.getPos(), calculations.invMass, linearMomentum, angularVelocity, rotationAndTranslationMatrix, posInWorld, calculations.centerOfMass, calculations.physTickSpeed);

				tileForce.multiply(1D / calculations.physTickSpeed);

				Vector forcePos = forceTile.getPositionInLocalSpaceWithOrientation();


				stabilityBias = 0.5D;//.6D;


				double tileForceMagnitude = tileForce.length();

				forceTile.setThrust(BigBastardMath.limitToRange(tileForceMagnitude, 0D, forceTile.getMaxThrust()));

				totalAngularForce.add(forceTile.angularThrust);

//				System.out.println(forceTile.getThrust());

				Vector forceOutputWithRespectToTime = forceTile.getForceOutputOriented(calculations.physTickSpeed);

				linearMomentum.add(forceOutputWithRespectToTime);

				Vector torque = forceTile.getPositionInLocalSpaceWithOrientation().cross(forceOutputWithRespectToTime);
				RotationMatrices.applyTransform3by3(invMOIMatrix, torque);
				angularVelocity.add(torque);
			}
		}

		double totalAngularForceRatioToIdealThrust = totalAngularForce.length() / idealTotalThrust;

		if(totalAngularForceRatioToIdealThrust > .9D) {
			for(Node node : getNetworkedNodesList()) {
				if(node.parentTile instanceof TileEntityEtherCompressor) {
					TileEntityEtherCompressor forceTile = (TileEntityEtherCompressor) node.parentTile;

					Vector angularToRemove = forceTile.linearThrust.getAddition(forceTile.angularThrust).getProduct(1D - (1D / totalAngularForceRatioToIdealThrust));

//					forceTile.linearThrust

					forceTile.angularThrust.subtract(angularToRemove);
					forceTile.linearThrust.subtract(angularToRemove);

					Vector forceOut = forceTile.linearThrust.getAddition(forceTile.angularThrust);

					double tileForceMagnitude = forceOut.length();

					forceTile.setThrust(BigBastardMath.limitToRange(tileForceMagnitude, 0D, forceTile.getMaxThrust()));
				}
			}
		}

		Vector linearVelocityFinal = new Vector(linearMomentum, calculations.invMass);

		if(linearVelocityFinal.Y > maxYDelta)	{
			double ratio = maxYDelta / Math.abs(linearVelocityFinal.Y);
			for(Node node : getNetworkedNodesList()) {
				if(node.parentTile instanceof TileEntityEtherCompressor) {
					TileEntityEtherCompressor forceTile = (TileEntityEtherCompressor) node.parentTile;

					forceTile.linearThrust.subtract(forceTile.angularThrust);

					if(forceTile.linearThrust.Y < 0) {
						forceTile.linearThrust.zero();
					}

//					forceTile.angularThrust.multiply(ratio);

					double tileForceMagnitude = forceTile.getThrust() * ratio;
					forceTile.setThrust(BigBastardMath.limitToRange(tileForceMagnitude, 0D, forceTile.getMaxThrust()));
				}
			}
		}
	}


	public Vector getForceForEngine(TileEntityEtherCompressor engine, BlockPos enginePos, double invMass, Vector linearMomentum, Vector angularVelocity, double[] rotationAndTranslationMatrix, Vector shipPos, Vector centerOfMass, double secondsToApply) {
        Vector shipVel = new Vector(linearMomentum);

        shipVel.multiply(invMass);

        double linearDist = -getControllerDistFromIdealY(rotationAndTranslationMatrix, invMass, shipPos.Y, linearMomentum);
        double angularDist = -getEngineDistFromIdealAngular(enginePos, rotationAndTranslationMatrix, angularVelocity, centerOfMass, secondsToApply);

        engine.angularThrust.Y -= (angularConstant * secondsToApply) * angularDist;
        engine.linearThrust.Y -= (linearConstant * secondsToApply) * linearDist;

        engine.angularThrust.Y = Math.max(engine.angularThrust.Y, 0D);
        engine.linearThrust.Y = Math.max(engine.linearThrust.Y, 0D);

        engine.angularThrust.Y = Math.min(engine.angularThrust.Y, engine.getMaxThrust() * stabilityBias);
        engine.linearThrust.Y = Math.min(engine.linearThrust.Y, engine.getMaxThrust() * (1D - stabilityBias));

        engine.linearThrust.Y -= engine.angularThrust.Y;
        engine.linearThrust.Y = Math.max(engine.linearThrust.Y, 0D);

        if (shipVel.Y > 25D) {
            engine.linearThrust.Y = 0; //*= 10D/shipVel.Y;
        }

        if (shipVel.Y < -10D) {
            engine.linearThrust.Y *= -20D / shipVel.Y;
        }

        Vector aggregateForce = engine.linearThrust.getAddition(engine.angularThrust);
        aggregateForce.multiply(secondsToApply);

        // System.out.println(aggregateForce);

        return aggregateForce;
//		return new Vector();
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

		return idealYDif - (inWorldYDif + angularVelocityAtPoint.Y * 50D);
	}

	public double getControllerDistFromIdealY(double[] lToWTransform, double invMass, double posY, Vector linearMomentum) {
		BlockPos pos = parentTile.getPos();
		Vector controllerPos = new Vector(pos.getX() + .5D, pos.getY() + .5D, pos.getZ() + .5D);
		controllerPos.transform(lToWTransform);
		return idealHeight - (posY + (linearMomentum.Y * invMass * linearVelocityBias));
	}

	public double getMaxThrustForAllThrusters() {
		double totalThrustAvaliable = 0D;
		for(Node otherNode : getNetworkedNodesList()) {
			TileEntity nodeTile = otherNode.parentTile;
			if(nodeTile instanceof TileEntityNormalEtherCompressor) {
				TileEntityNormalEtherCompressor ether = (TileEntityNormalEtherCompressor) nodeTile;
				totalThrustAvaliable += ether.getMaxThrust();
			}
		}
		return totalThrustAvaliable;
	}

	private HashSet<Node> getNetworkedNodesList() {
		return parentTile.tileNode.getNodeNetwork().networkedNodes;
	}

}
