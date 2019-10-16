package org.valkyrienskies.mod.common.physmanagement.shipdata;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.valkyrienskies.mod.common.coordinates.ShipTransform;
import org.valkyrienskies.mod.common.entity.PhysicsWrapperEntity;
import org.valkyrienskies.mod.common.math.Vector;

@Accessors(fluent = false)
@NoArgsConstructor(force = true)
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
        shipPosition.x = wrapper.posX;
        shipPosition.y = wrapper.posY;
        shipPosition.z = wrapper.posZ;
        transform =
            wrapper.getPhysicsObject().shipTransformationManager().getCurrentTickTransform();
    }

    public double getPosX() {
        return shipPosition.x;
    }

    public double getPosY() {
        return shipPosition.y;
    }

    public double getPosZ() {
        return shipPosition.z;
    }

}
