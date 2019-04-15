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

package valkyrienwarfare.physics.management;

import valkyrienwarfare.mod.coordinates.ShipTransformationPacketHolder;
import valkyrienwarfare.mod.network.PhysWrapperPositionMessage;

import java.util.LinkedList;

/**
 * Ideally this would smooth out data coming from the sever, but for now it
 * mostly does nothing aside from storing the previous transforms. May possibly
 * add something here in the future.
 *
 * @author thebest108
 */
public class ShipTransformationBuffer {

    public static final int PACKET_BUFFER_SIZE = 50;
    public static final int TRANSFORMS_SMOOTHED = 5;
    public static final double TRANFORMATION_DELAY = .5D;
    private final LinkedList<ShipTransformationPacketHolder> transformations;
    private final BezierWeightGenerator weightGenerator;

    public ShipTransformationBuffer() {
        this.transformations = new LinkedList<ShipTransformationPacketHolder>();
        this.weightGenerator = new BezierWeightGenerator(TRANSFORMS_SMOOTHED);
    }

    public void pushMessage(PhysWrapperPositionMessage toPush) {
        transformations.push(new ShipTransformationPacketHolder(toPush));
        if (transformations.size() > PACKET_BUFFER_SIZE) {
            transformations.removeLast();
        }
    }

    public ShipTransformationPacketHolder pollForClientTransform() {
        if (isSmoothTransformReady()) {
            return generateSmoothTransform();
        } else {
            return null;
        }
    }

    private boolean isSmoothTransformReady() {
        return true; /*transformations.size() > TRANSFORMS_SMOOTHED;*/
    }

    // Doesn't really do anything yet.
    private ShipTransformationPacketHolder generateSmoothTransform() {
        return transformations.pollFirst();
    	/*
    	double[] weights = new double[TRANSFORMS_SMOOTHED];
    	ShipTransformationPacketHolder[] transforms = new ShipTransformationPacketHolder[TRANSFORMS_SMOOTHED];
    	for (int i = 0; i < TRANSFORMS_SMOOTHED; i++) {
    		weights[i] = weightGenerator.getTermWeight(.5, i);
    		transforms[i] = transformations.get(i);
    	}
    	return transforms[0]; // new ShipTransformationPacketHolder(transforms, weights);
    	*/
    }

    private class BezierWeightGenerator {

        private final int order;

        public BezierWeightGenerator(int order) {
            this.order = order;
        }

        public double getTermWeight(double deltaTime, int term) {
            return 2 * Math.pow(deltaTime, term) * Math.pow(1 - deltaTime, order - term)
                    * binomial(TRANSFORMS_SMOOTHED - 1, term);
        }

        private int binomial(int n, int k) {
            if (k > n - k)
                k = n - k;
            int b = 1;
            for (int i = 1, m = n; i <= k; i++, m--)
                b = b * m / i;
            return b;
        }

    }
}
