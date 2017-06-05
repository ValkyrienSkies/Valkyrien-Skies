package ValkyrienWarfareControl.TileEntity;

import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareControl.ThrustNetwork.IForceTile;

public class BasicForceNodeTileEntity extends BasicNodeTileEntity implements IForceTile{

	private Vector forceOutputVector = new Vector();

	private double maxThrust = 5000D;
	private double currentThrust = 0D;

	public BasicForceNodeTileEntity(){

	}

	@Override
	public Vector getForceOutputNormal() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector getForceOutput() {
		return forceOutputVector;
	}

	@Override
	public double getMaxThrust() {
		return maxThrust;
	}

	@Override
	public void setForceMagnitude(double newMagnitude) {
		currentThrust = newMagnitude;
	}

	@Override
	public Vector getPositionInLocalSpaceWithOrientation() {
		Vector engineCenter = new Vector(getPos().getX() + .5D, getPos().getY() + .5D, getPos().getZ() + .5D);
		return null;
	}

	@Override
	public Vector getVelocityAtEngineCenter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector getLinearVelocityAtEngineCenter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector getAngularVelocityAtEngineCenter() {
		// TODO Auto-generated method stub
		return null;
	}

}
