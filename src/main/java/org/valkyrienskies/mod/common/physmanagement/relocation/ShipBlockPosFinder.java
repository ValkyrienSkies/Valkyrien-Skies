package org.valkyrienskies.mod.common.physmanagement.relocation;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.physics.BlockPhysicsDetails;

public class ShipBlockPosFinder extends SpatialDetector {

    private final MutableBlockPos mutablePos = new MutableBlockPos();

    public ShipBlockPosFinder(BlockPos start, World worldIn, int maximum, boolean checkCorners) {
        super(start, worldIn, maximum, checkCorners);
        startDetection();
    }

    @Override
    public boolean isValidExpansion(int x, int y, int z) {
        mutablePos.setPos(x, y, z);
        return !BlockPhysicsDetails.blocksToNotPhysicsInfuse
            .contains(cache.getBlockState(mutablePos).getBlock());
    }

}
