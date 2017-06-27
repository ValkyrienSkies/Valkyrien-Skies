package ValkyrienWarfareControl.NodeNetwork;

import ValkyrienWarfareBase.API.Vector;

public interface IForceTile {

    /**
     * Used to tell what direction of force an engine will output, this is calculated with respect to the orientation of the engine, DO NOT ALTER
     *
     * @return
     */
    public Vector getForceOutputNormal();

    /**
     * Returns the current unoriented force output vector of this engine
     *
     * @return
     */
    public Vector getForceOutputUnoriented(double secondsToApply);

    /**
     * Returns the current oriented force output vector of this engine
     *
     * @return
     */
    public Vector getForceOutputOriented(double secondsToApply);

    /**
     * Returns the maximum magnitude of force this engine can provide
     *
     * @return
     */
    public double getMaxThrust();

    /**
     * Sets the force output vector to be this outputNormal() * newMagnitude
     *
     * @param toUse
     */
    public void setThrust(double newMagnitude);

    /**
     * Returns the thrust value of this ForceTile
     * @return
     */
    public double getThrust();

    /**
     * Matrix transformation stuff
     *
     * @return
     */
    public Vector getPositionInLocalSpaceWithOrientation();

    /**
     * Returns the velocity vector this engine is moving to relative to the world
     *
     * @return
     */
    public Vector getVelocityAtEngineCenter();

    /**
     * Returns the velocity vector of this engine moving relative to the world, except only the linear component from the total velocity
     *
     * @return
     */
    public Vector getLinearVelocityAtEngineCenter();

    /**
     * Returns the velocity vector of this engine moving relative to the world, except only the angular component from the total velocity
     *
     * @return
     */
    public Vector getAngularVelocityAtEngineCenter();
}
