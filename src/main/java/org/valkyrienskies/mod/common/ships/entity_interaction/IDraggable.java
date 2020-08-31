package org.valkyrienskies.mod.common.ships.entity_interaction;

import org.joml.Vector3dc;
import org.valkyrienskies.mod.common.ships.ShipData;

@Deprecated
public interface IDraggable {

    ShipData getWorldBelowFeet();

    void setWorldBelowFeet(ShipData toSet);

    Vector3dc getVelocityAddedToPlayer();

    void setVelocityAddedToPlayer(Vector3dc toSet);

    double getYawDifVelocity();

    void setYawDifVelocity(double toSet);

    int getTicksSinceTouchedShip();

    void setTicksSinceTouchedShip(int ticksSinceTouchedShip);
}
