package ValkyrienWarfareBase.Relocation;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A detector that will only pick up one block; used for most orbitals
 *
 * @author thebest108
 */
public class SingeBlockPosDetector extends SpatialDetector {

    public SingeBlockPosDetector(BlockPos start, World worldIn, int maximum, boolean checkCorners) {
        super(start, worldIn, maximum, false);
        startDetection();
    }

    @Override
    public boolean isValidExpansion(int x, int y, int z) {
        if (x == firstBlock.getX() && y == firstBlock.getY() && z == firstBlock.getZ()) {
            return true;
        }
        return false;
    }

}
