package org.valkyrienskies.mod.common.ships.entity_interaction;

import org.valkyrienskies.mod.common.entity.EntityShipMovementData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IDraggable {
    @Nonnull
    EntityShipMovementData getEntityShipMovementData();

    void setEntityShipMovementData(@Nullable EntityShipMovementData entityShipMovementData);
}
