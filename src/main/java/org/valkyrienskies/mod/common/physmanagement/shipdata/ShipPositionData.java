package org.valkyrienskies.mod.common.physmanagement.shipdata;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.valkyrienskies.mod.common.coordinates.ShipTransform;
import org.valkyrienskies.mod.common.entity.PhysicsWrapperEntity;
import org.valkyrienskies.mod.common.math.Vector;

@NoArgsConstructor(force = true) // For Kryo
public class ShipPositionData {

    private final Vector shipPosition;
    @Getter
    private ShipTransform transform;

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
