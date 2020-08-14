package org.valkyrienskies.mod.common.ships.physics_data;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import org.valkyrienskies.mod.common.ships.physics_data.ShipInertiaData;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Manages the center of mass, as well as the inertia matrix of a provided ShipInertiaData.
 */
public interface IPhysicsObjectCenterOfMassProvider {

    @ParametersAreNonnullByDefault
    void onSetBlockState(ShipInertiaData data, BlockPos pos, IBlockState oldState, IBlockState newState);

}
