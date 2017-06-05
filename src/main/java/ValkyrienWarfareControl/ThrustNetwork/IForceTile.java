package ValkyrienWarfareControl.ThrustNetwork;

import ValkyrienWarfareBase.API.Vector;

public interface IForceTile {

	/**
	 * Used to tell what direction of force an engine will output, this is calculated with respect to the orientation of the engine
	 * @return
	 */
	public Vector getForceOutputNormal();

	/**
	 * Returns the current force output vector of this engine
	 * @return
	 */
	public Vector getForceOutput();

	/**
	 * Returns the maximum magnitude of force this engine can provide
	 * @return
	 */
	public double getMaxThrust();

	/**
	 * Sets the force output vector to be this outputNormal() * newMagnitude
	 * @param toUse
	 */
	public void setForceMagnitude(double newMagnitude);

	/**
	 * Matrix transformation stuff
	 * @return
	 */
	public Vector getPositionInLocalSpaceWithOrientation();

	/**
	 * Returns the velocity vector this engine is moving to relative to the world
	 * @return
	 */
	public Vector getVelocityAtEngineCenter();

	/**
	 * Returns the velocity vector of this engine moving relative to the world, except only the linear component from the total velocity
	 * @return
	 */
	public Vector getLinearVelocityAtEngineCenter();

	/**
	 * Returns the velocity vector of this engine moving relative to the world, except only the angular component from the total velocity
	 * @return
	 */
	public Vector getAngularVelocityAtEngineCenter();
}
