package ValkyrienWarfareBase.API;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IBlockForceProvider {

    /**
     * The World space version of the force vector, calculated by default from the Ship space vector, do not override unless you have a good reason to
     *
     * @param world
     * @param pos
     * @param state
     * @param shipEntity
     * @param secondsToApply
     * @return
     */
    default Vector getBlockForceInWorldSpace(World world, BlockPos pos, IBlockState state, Entity shipEntity, double secondsToApply) {
        Vector toReturn = getBlockForceInShipSpace(world, pos, state, shipEntity, secondsToApply);
        if (toReturn == null || shipEntity == null) {
            return null;
        }
        if (shouldLocalForceBeRotated(world, pos, state, secondsToApply)) {
            double[] tranformationMatrix = ValkyrienWarfareHooks.getShipTransformMatrix(shipEntity);
            RotationMatrices.doRotationOnly(tranformationMatrix, toReturn);
        }
        return toReturn;
    }

    // Multiply your power usage values or whatever by the secondsToApply, otherwise you'll have issues (Example: <0,400,0> -(.01)-> <0,40,0>

    /**
     * The force Vector this block gives within its local space (Not within World space)
     *
     * @param world
     * @param pos
     * @param state
     * @param shipEntity
     * @param secondsToApply
     * @return
     */
    Vector getBlockForceInShipSpace(World world, BlockPos pos, IBlockState state, Entity shipEntity, double secondsToApply);

    /**
     * Returns true by default, blocks that shouldn't have their force rotated (Like Ether Compressors) must return false
     *
     * @param world
     * @param pos
     * @param state
     * @param secondsToApply
     * @return
     */
    default boolean shouldLocalForceBeRotated(World world, BlockPos pos, IBlockState state, double secondsToApply) {
        return true;
    }

    /**
     * This method returns null if no changes are needed, however some blocks like the Balloon Burner need to apply their force in a different position
     *
     * @param world
     * @param pos
     * @param state
     * @param shipEntity
     * @param secondsToApply
     * @return
     */
    default Vector getCustomBlockForcePosition(World world, BlockPos pos, IBlockState state, Entity shipEntity, double secondsToApply) {
        return null;
    }

}
