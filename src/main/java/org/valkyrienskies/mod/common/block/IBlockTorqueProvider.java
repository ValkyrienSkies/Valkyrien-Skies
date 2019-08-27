package org.valkyrienskies.mod.common.block;

import net.minecraft.util.math.BlockPos;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.PhysicsCalculations;

import javax.annotation.Nullable;

public interface IBlockTorqueProvider extends Comparable<IBlockTorqueProvider> {

    @Nullable
	Vector getTorqueInGlobal(PhysicsCalculations physicsCalculations, BlockPos pos);

    default int compareTo(IBlockTorqueProvider other) {
        return getBlockSortingIndex() - other.getBlockSortingIndex();
    }

    /**
     * Lower numbers will put this Block at a higher priority for adding torque to
     * the rigid body; a higher number is used to put this Block at a lower
     * priority.
     */
    default int getBlockSortingIndex() {
        return 0;
    }
}
