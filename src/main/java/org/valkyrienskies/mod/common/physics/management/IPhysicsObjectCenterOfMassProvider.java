package org.valkyrienskies.mod.common.physics.management;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import org.valkyrienskies.mod.common.ship_handling.PhysicsObject;

/**
 * Manages the center of mass, as well as the inertia matrix of a provided ShipInertiaData.
 */
public interface IPhysicsObjectCenterOfMassProvider {

    void onSetBlockState(PhysicsObject physicsObject, BlockPos pos, IBlockState oldState, IBlockState newState);

    /**
     * @return True if and only if this provider provides an inertia matrix.
     */
    default boolean providesInertiaMatrix() {
        return false;
    }
}
