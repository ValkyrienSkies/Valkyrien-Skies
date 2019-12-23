package org.valkyrienskies.mod.common.physmanagement.interaction;

import net.minecraft.entity.MoverType;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;

/**
 * This interface is really quite bad, and will be removed once subspaces are complete.
 *
 * @author thebest108
 */
@Deprecated
public interface IDraggable {

    PhysicsObject getWorldBelowFeet();

    void setWorldBelowFeet(PhysicsObject toSet);

    Vector getVelocityAddedToPlayer();

    void setVelocityAddedToPlayer(Vector toSet);

    double getYawDifVelocity();

    void setYawDifVelocity(double toSet);

    void setCancelNextMove(boolean toSet);

    void move(MoverType type, double dx, double dy, double dz);

    void setForcedRelativeSubspace(PhysicsObject toSet);

    PhysicsObject getForcedSubspaceBelowFeet();

}
