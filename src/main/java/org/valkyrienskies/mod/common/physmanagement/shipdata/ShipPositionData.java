package org.valkyrienskies.mod.common.physmanagement.shipdata;

import lombok.Getter;
import org.valkyrienskies.mod.common.coordinates.ShipTransform;
import org.valkyrienskies.mod.common.entity.PhysicsWrapperEntity;
import org.valkyrienskies.mod.common.math.Vector;

public class ShipPositionData {

    private final Vector shipPosition;
    @Getter
    private ShipTransform transform;

    // For Kryo
    @SuppressWarnings("unused")
    private ShipPositionData() {
        shipPosition = null;
        transform = null;
    }

    ShipPositionData(PhysicsWrapperEntity wrapper) {
        shipPosition = new Vector(wrapper.posX, wrapper.posY, wrapper.posZ);
        transform =
            wrapper.getPhysicsObject()
                .shipTransformationManager()
                .getCurrentTickTransform();
    }

    void updateData(PhysicsWrapperEntity wrapper) {
        shipPosition.X = wrapper.posX;
        shipPosition.Y = wrapper.posY;
        shipPosition.Z = wrapper.posZ;
        transform =
            wrapper.getPhysicsObject().shipTransformationManager().getCurrentTickTransform();
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

}
