package org.valkyrienskies.mod.common.ships.entity_interaction;

import net.minecraft.entity.MoverType;
import org.joml.Vector3dc;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;

/**
 * This interface is really quite bad, and will be removed once subspaces are complete.
 *
 * @author thebest108
 */
@Deprecated
public interface IDraggable {

    PhysicsObject getWorldBelowFeet();

    void setWorldBelowFeet(PhysicsObject toSet);

    Vector3dc getVelocityAddedToPlayer();

    void setVelocityAddedToPlayer(Vector3dc toSet);

    double getYawDifVelocity();

    void setYawDifVelocity(double toSet);

    void setCancelNextMove(boolean toSet);

    void move(MoverType type, double dx, double dy, double dz);

    void setForcedRelativeSubspace(PhysicsObject toSet);

    PhysicsObject getForcedSubspaceBelowFeet();

}
