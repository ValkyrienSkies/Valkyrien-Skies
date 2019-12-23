package org.valkyrienskies.mod.common.physmanagement.relocation;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DetectorManager {

    public static SpatialDetector getDetectorFor(int id, BlockPos start, World worldIn, int maximum,
        boolean checkCorners) {
        return getDetectorFor(DetectorIDs.values()[id], start, worldIn, maximum, checkCorners);
    }


    public static SpatialDetector getDetectorFor(DetectorIDs id, BlockPos start, World worldIn, int maximum,
        boolean checkCorners) {
        switch (id) {
            case ShipSpawnerGeneral:
                return new ShipSpawnDetector(start, worldIn, maximum, checkCorners);
            case BlockPosFinder:
                return new ShipBlockPosFinder(start, worldIn, maximum, checkCorners);
            case SingleBlockPosFinder:
                return new SingleBlockPosDetector(start, worldIn, maximum, checkCorners);
            default:
                throw new IllegalArgumentException("Unrecognized detector");
        }
    }

    public enum DetectorIDs {
        ShipSpawnerGeneral, BlockPosFinder, SingleBlockPosFinder
    }

}
