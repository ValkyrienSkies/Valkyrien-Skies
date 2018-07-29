package valkyrienwarfare.deprecated_api;

import jline.internal.Nullable;
import net.minecraft.util.math.BlockPos;
import valkyrienwarfare.math.Vector;
import valkyrienwarfare.physics.PhysicsCalculations;

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
	 * 
	 * @return
	 */
	default int getBlockSortingIndex() {
		return 0;
	}
}
