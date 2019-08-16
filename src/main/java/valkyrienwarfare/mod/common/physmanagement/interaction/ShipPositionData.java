package valkyrienwarfare.mod.common.physmanagement.interaction;

import valkyrienwarfare.api.TransformType;
import valkyrienwarfare.mod.common.entity.PhysicsWrapperEntity;
import valkyrienwarfare.mod.common.math.RotationMatrices;
import valkyrienwarfare.mod.common.math.Vector;

import java.nio.ByteBuffer;

public class ShipPositionData {

    private final Vector shipPosition;
    private final float[] lToWTransform;

    // For Kryo
    private ShipPositionData() {
        shipPosition = null;
        lToWTransform = null;
    }

    ShipPositionData(PhysicsWrapperEntity wrapper) {
        shipPosition = new Vector(wrapper.posX, wrapper.posY, wrapper.posZ);
        lToWTransform = RotationMatrices.convertToFloat(
                wrapper.getPhysicsObject()
                        .getShipTransformationManager()
                        .getCurrentTickTransform()
                        .getInternalMatrix(TransformType.SUBSPACE_TO_GLOBAL));
    }

    ShipPositionData(ByteBuffer buffer) {
        shipPosition = new Vector(buffer.getFloat(), buffer.getFloat(), buffer.getFloat());
        lToWTransform = new float[16];
        for (int i = 0; i < 16; i++) {
            lToWTransform[i] = buffer.getFloat();
        }
    }

    void writeToByteBuffer(ByteBuffer buffer) {
        buffer.putFloat((float) shipPosition.X);
        buffer.putFloat((float) shipPosition.Y);
        buffer.putFloat((float) shipPosition.Z);
        for (int i = 0; i < 16; i++) {
            buffer.putFloat(lToWTransform[i]);
        }
    }

    void updateData(PhysicsWrapperEntity wrapper) {
        shipPosition.X = wrapper.posX;
        shipPosition.Y = wrapper.posY;
        shipPosition.Z = wrapper.posZ;
        RotationMatrices.convertToFloat(wrapper.getPhysicsObject().getShipTransformationManager().getCurrentTickTransform().getInternalMatrix(TransformType.SUBSPACE_TO_GLOBAL), lToWTransform);
    }

    public double getPosX() {
        return shipPosition.X;
    }

    public double getPosY() {
        return shipPosition.Y;
    }

    public double getPosZ() {
        return shipPosition.Z;
    }

    // Returns a copy of of the lToWTransform as a double array.
    public double[] getLToWTransform() {
        return RotationMatrices.convertToDouble(lToWTransform);
    }
}
