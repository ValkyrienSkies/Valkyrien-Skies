package ValkyrienWarfareControl.ControlSystems;

import java.util.HashSet;

import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.API.Block.EtherCompressor.TileEntityEtherCompressor;
import ValkyrienWarfareBase.Physics.PhysicsCalculations;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsObject;
import ValkyrienWarfareControl.NodeNetwork.Node;
import ValkyrienWarfareControl.TileEntity.ThrustModulatorTileEntity;
import ValkyrienWarfareControl.TileEntity.TileEntityNormalEtherCompressor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class StabilityHeightPIDControl {

	public final ThrustModulatorTileEntity parentTile;
	private double idealY;

	public StabilityHeightPIDControl(ThrustModulatorTileEntity parentTile) {
		this.parentTile = parentTile;
	}

	public void solveThrustValues(PhysicsCalculations calculations) {
		Vector currentLinearMomentum = new Vector(calculations.linearMomentum);
		Vector currentAngularMomentum = new Vector(calculations.angularVelocity);

		Vector currentShipNormal = new Vector(0, 1, 0, calculations.parent.coordTransform.lToWRotation);

//		calculations.parent.wrapper.isDead = true;

		idealY = 25D;
		Vector idealLinear = getIdealLinearMomentum(calculations);

		Vector thrustAllowance = idealLinear.getSubtraction(currentLinearMomentum);

		if(thrustAllowance.Y < 0) {
//			thrustAllowance.multiply(0);
		}



//		double maxThrustGiven = getMaxThrustForAllThrusters();

		double currentUsedThrust = 0D;

		for(Node otherNode : getNetworkedNodesList()) {
			TileEntity nodeTile = otherNode.parentTile;
			if(nodeTile instanceof TileEntityEtherCompressor) {
				TileEntityNormalEtherCompressor ether = (TileEntityNormalEtherCompressor) nodeTile;

				//Normalized, multiply by thrust output to change
				Vector angularThrustChange = new Vector(0, 1D, 0);
				Vector posInShip = new Vector(ether.getPos().getX() + .5D, ether.getPos().getY() + .5D, ether.getPos().getZ() + .5D, calculations.parent.coordTransform.lToWTransform);
				posInShip.subtract(calculations.parent.wrapper.posX, calculations.parent.wrapper.posY, calculations.parent.wrapper.posZ);
				angularThrustChange = posInShip.cross(angularThrustChange);

				RotationMatrices.applyTransform3by3(calculations.invFramedMOI, angularThrustChange);

				double offset = getEngineDistFromIdealAngular(ether.getPos(), calculations.parent, calculations.physRawSpeed);

				if(offset > 0) {
					ether.setThrust(0D);
				}else {
					ether.setThrust(offset);
					currentUsedThrust += offset;
				}
			}
		}



		double multiple = -idealLinear.Y/200;// / currentUsedThrust;

		for(Node otherNode : getNetworkedNodesList()) {
			TileEntity nodeTile = otherNode.parentTile;
			if(nodeTile instanceof TileEntityEtherCompressor) {
//				System.out.println(multiple);
				((TileEntityNormalEtherCompressor) nodeTile).setThrust(multiple);

			}
		}

	}

	private Vector getIdealLinearMomentum(PhysicsCalculations calculations) {
		double maxMagnitude = 5D;

		double currentY = calculations.parent.wrapper.posY;

		Vector idealMomentum = new Vector(0, idealY - currentY, 0);

		double magnitude = idealMomentum.length();

		if(magnitude > maxMagnitude) {
			idealMomentum.multiply(maxMagnitude / magnitude);
		}

		idealMomentum.multiply(-calculations.mass);

		return idealMomentum;
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



	//BAD
    public double angularVelocityBias = 50D;


    public double getEngineDistFromIdealAngular(BlockPos enginePos, PhysicsObject physObj, double secondsToApply) {
    	BlockPos pos = parentTile.getPos();
    	Vector normalVector = new Vector(0, 1D, 0);

        Vector controllerPos = new Vector(pos.getX() + .5D, pos.getY() + .5D, pos.getZ() + .5D);
        Vector enginePosVec = new Vector(enginePos.getX() + .5D, enginePos.getY() + .5D, enginePos.getZ() + .5D);

        controllerPos.subtract(physObj.physicsProcessor.centerOfMass);
        enginePosVec.subtract(physObj.physicsProcessor.centerOfMass);

        Vector unOrientedPosDif = new Vector(enginePosVec.X - controllerPos.X, enginePosVec.Y - controllerPos.Y, enginePosVec.Z - controllerPos.Z);

        double idealYDif = unOrientedPosDif.dot(normalVector);

        RotationMatrices.doRotationOnly(physObj.coordTransform.lToWRotation, controllerPos);
        RotationMatrices.doRotationOnly(physObj.coordTransform.lToWRotation, enginePosVec);

        double inWorldYDif = enginePosVec.Y - controllerPos.Y;

        Vector angularVelocityAtPoint = physObj.physicsProcessor.angularVelocity.cross(enginePosVec);
        angularVelocityAtPoint.multiply(secondsToApply);

        return idealYDif - (inWorldYDif + angularVelocityAtPoint.Y * 500D);
    }
}
