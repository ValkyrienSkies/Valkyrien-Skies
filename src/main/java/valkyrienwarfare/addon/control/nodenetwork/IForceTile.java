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

package valkyrienwarfare.addon.control.nodenetwork;

import valkyrienwarfare.api.Vector;

public interface IForceTile {

    /**
     * Used to tell what direction of force an engine will output, this is
     * calculated with respect to the orientation of the engine, DO NOT ALTER
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
     * Returns the thrust value of this ForceTile
     *
     * @return
     */
    public double getThrust();

    /**
     * Sets the force output vector to be this outputNormal() * newMagnitude
     *
     * @param toUse
     */
    public void setThrust(double newMagnitude);

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
     * Returns the velocity vector of this engine moving relative to the world,
     * except only the linear component from the total velocity
     *
     * @return
     */
    public Vector getLinearVelocityAtEngineCenter();

    /**
     * Returns the velocity vector of this engine moving relative to the world,
     * except only the angular component from the total velocity
     *
     * @return
     */
    public Vector getAngularVelocityAtEngineCenter();
}
