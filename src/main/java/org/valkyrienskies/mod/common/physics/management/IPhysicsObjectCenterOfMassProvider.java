package org.valkyrienskies.mod.common.physics.management;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import org.valkyrienskies.mod.common.ship_handling.ShipInertiaData;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Manages the center of mass, as well as the inertia matrix of a provided ShipInertiaData.
 */
public interface IPhysicsObjectCenterOfMassProvider {

    @ParametersAreNonnullByDefault
    void onSetBlockState(ShipInertiaData data, BlockPos pos, IBlockState oldState, IBlockState newState);

}
