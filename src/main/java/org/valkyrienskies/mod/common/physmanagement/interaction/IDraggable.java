package org.valkyrienskies.mod.common.physmanagement.interaction;

import net.minecraft.entity.MoverType;
import org.valkyrienskies.mod.common.entity.PhysicsWrapperEntity;
import org.valkyrienskies.mod.common.math.Vector;

/**
 * This interface is really quite bad, and will be removed once subspaces are complete.
 *
 * @author thebest108
 */
@Deprecated
public interface IDraggable {

    PhysicsWrapperEntity getWorldBelowFeet();

    void setWorldBelowFeet(PhysicsWrapperEntity toSet);

    Vector getVelocityAddedToPlayer();

    void setVelocityAddedToPlayer(Vector toSet);

    double getYawDifVelocity();

    void setYawDifVelocity(double toSet);

    void setCancelNextMove(boolean toSet);

    void move(MoverType type, double dx, double dy, double dz);

    void setForcedRelativeSubspace(PhysicsWrapperEntity toSet);

    PhysicsWrapperEntity getForcedSubspaceBelowFeet();

}
