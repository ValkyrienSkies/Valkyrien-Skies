package org.valkyrienskies.mod.common.ships.interpolation;

import net.minecraft.util.math.AxisAlignedBB;
import org.valkyrienskies.mod.common.ships.ship_transform.ShipTransform;

import javax.annotation.Nonnull;

/**
 * An interface that allows for different ship interpolation algorithms to be implemented easily.
 */
public interface ITransformInterpolator {

    /**
     * Sends the latest transform and AABB to the interpolator.
     */
    void onNewTransformPacket(@Nonnull ShipTransform newTransform, @Nonnull AxisAlignedBB newAABB);

    /**
     * Moves the interpolator up 1 tick, moving the current transform closer to the latest transform.
     */
    void tickTransformInterpolator();

    /**
     * Returns the current smoothed transform.
     */
    @Nonnull
    ShipTransform getCurrentTickTransform();

    /**
     * Returns the current smoothed AxisAlignedBB.
     */
    @Nonnull
    AxisAlignedBB getCurrentAABB();

}
