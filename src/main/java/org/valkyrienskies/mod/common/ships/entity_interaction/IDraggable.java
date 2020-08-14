package org.valkyrienskies.mod.common.ships.entity_interaction;

import net.minecraft.entity.MoverType;
import org.joml.Vector3dc;
import org.valkyrienskies.mod.common.ships.ShipData;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;

import java.util.UUID;

/**
 * This interface is really quite bad, and will be removed once subspaces are complete.
 *
 * @author thebest108
 */
@Deprecated
public interface IDraggable {

    ShipData getWorldBelowFeet();

    void setWorldBelowFeet(ShipData toSet);

    Vector3dc getVelocityAddedToPlayer();

    void setVelocityAddedToPlayer(Vector3dc toSet);

    double getYawDifVelocity();

    void setYawDifVelocity(double toSet);

}
