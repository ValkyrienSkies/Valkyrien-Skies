package org.valkyrienskies.mod.common.physics.management;

import java.util.LinkedList;
import org.valkyrienskies.mod.common.network.WrapperPositionMessage;

/**
 * Ideally this would smooth out data coming from the sever, but for now it mostly does nothing
 * aside from storing the previous transforms. May possibly add something here in the future.
 *
 * @author thebest108
 */
public class ShipTransformationBuffer {

    public static final int PACKET_BUFFER_SIZE = 50;
    public static final int TRANSFORMS_SMOOTHED = 5;
    public static final double TRANFORMATION_DELAY = .5D;
    private final LinkedList<WrapperPositionMessage> transformations;
    private final BezierWeightGenerator weightGenerator;

    public ShipTransformationBuffer() {
        this.transformations = new LinkedList<WrapperPositionMessage>();
        this.weightGenerator = new BezierWeightGenerator(TRANSFORMS_SMOOTHED);
    }

    public void pushMessage(WrapperPositionMessage toPush) {
        transformations.push(new WrapperPositionMessage(toPush));
        if (transformations.size() > PACKET_BUFFER_SIZE) {
            transformations.removeLast();
        }
    }

    public WrapperPositionMessage pollForClientTransform() {
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
    private WrapperPositionMessage generateSmoothTransform() {
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

    private static class BezierWeightGenerator {

        private final int order;

        BezierWeightGenerator(int order) {
            this.order = order;
        }

        public double getTermWeight(double deltaTime, int term) {
            return 2 * Math.pow(deltaTime, term) * Math.pow(1 - deltaTime, order - term)
                * binomial(TRANSFORMS_SMOOTHED - 1, term);
        }

        private int binomial(int n, int k) {
            if (k > n - k) {
                k = n - k;
            }
            int b = 1;
            for (int i = 1, m = n; i <= k; i++, m--) {
                b = b * m / i;
            }
            return b;
        }

    }
}
