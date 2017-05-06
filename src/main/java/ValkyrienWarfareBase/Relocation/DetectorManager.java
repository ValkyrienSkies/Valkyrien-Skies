package ValkyrienWarfareBase.Relocation;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DetectorManager {

	public static enum DetectorIDs {
		ShipSpawnerGeneral, BlockPosFinder, SingleBlockPosFinder
	}

	public static SpatialDetector getDetectorFor(int id, BlockPos start, World worldIn, int maximum, boolean checkCorners) {
		if (id == DetectorIDs.ShipSpawnerGeneral.ordinal()) {
			return new ShipSpawnDetector(start, worldIn, maximum, checkCorners);
		}

		if (id == DetectorIDs.BlockPosFinder.ordinal()) {
			return new ShipBlockPosFinder(start, worldIn, maximum, checkCorners);
		}

		if(id == DetectorIDs.SingleBlockPosFinder.ordinal()) {
			return new SingeBlockPosDetector(start, worldIn, maximum, checkCorners);
		}

		return null;
	}

}
