package org.valkyrienskies.mod.common.entity;

import lombok.NonNull;
import lombok.Value;
import lombok.With;
import org.joml.Vector3dc;
import org.valkyrienskies.mod.common.ships.ShipData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class stores data about the last ship an entity touched, as well as the velocity that ship added to the entity.
 */
@Value
@With
public class EntityShipMovementData {
    // If null, then the last touched "Ship" was the world. Otherwise, the last touched ship was a real ship.
    @Nullable
    ShipData lastTouchedShip;
    int ticksSinceTouchedShip;
    // The number of consecutive ticks that lastTouchedShip has equaled null.
    int ticksPartOfGround;
    @NonNull
    @Nonnull
    Vector3dc addedLinearVelocity;
    double addedYawVelocity;
}
