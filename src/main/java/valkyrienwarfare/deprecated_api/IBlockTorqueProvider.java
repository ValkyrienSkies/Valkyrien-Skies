package valkyrienwarfare.deprecated_api;

import net.minecraft.util.math.BlockPos;
import valkyrienwarfare.math.Vector;
import valkyrienwarfare.physics.PhysicsCalculations;

public interface IBlockTorqueProvider {

	Vector getTorqueInGlobal(PhysicsCalculations physicsCalculations, BlockPos pos);
}
