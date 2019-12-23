package org.valkyrienskies.mod.common.block;

import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.coordinates.ShipTransform;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;
import valkyrienwarfare.api.TransformType;

public interface IBlockForceProvider {

    /**
     * The World space version of the force vector, calculated by default from the Ship space
     * vector, do not override unless you have a good reason to.
     */
    @Nullable
    default Vector getBlockForceInWorldSpace(World world, BlockPos pos, IBlockState state,
        PhysicsObject physicsObject,
        double secondsToApply) {
        Vector toReturn = getBlockForceInShipSpace(world, pos, state, physicsObject,
            secondsToApply);
        if (toReturn == null) {
            return null;
        }
        if (shouldLocalForceBeRotated(world, pos, state, secondsToApply)) {
            ShipTransform shipTransform = physicsObject.getShipTransformationManager()
                .getCurrentTickTransform();
            shipTransform.rotate(toReturn, TransformType.SUBSPACE_TO_GLOBAL);
        }
        return toReturn;
    }

    // Multiply your power usage values or whatever by the secondsToApply, otherwise
    // you'll have issues (Example: <0,400,0> -(.01)-> <0,40,0>

    /**
     * The force Vector this block gives within its local space (Not within World space).
     */
    @Nullable
    Vector getBlockForceInShipSpace(World world, BlockPos pos, IBlockState state,
        PhysicsObject physicsObject,
        double secondsToApply);

    /**
     * Blocks that shouldn't have their force rotated (Like Valkyrium Compressors) must return false.
     */
    boolean shouldLocalForceBeRotated(World world, BlockPos pos, IBlockState state,
        double secondsToApply);

    /**
     * This method returns null if no changes are needed, however some blocks like the balloon
     * Burner need to apply their force in a different position.
     */
    @Nullable
    default Vector getCustomBlockForcePosition(World world, BlockPos pos, IBlockState state,
        PhysicsObject physicsObject,
        double secondsToApply) {
        return null;
    }

    /**
     * Returns true if this force provider uses 'simulated airflow' particles. Useful to make
     * certain engines not function when placed indoors.
     */
    default boolean doesForceSpawnParticles() {
        return false;
    }

}
