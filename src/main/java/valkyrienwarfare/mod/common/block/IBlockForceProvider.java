/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2018 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package valkyrienwarfare.mod.common.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import valkyrienwarfare.api.TransformType;
import valkyrienwarfare.mod.common.coordinates.ShipTransform;
import valkyrienwarfare.mod.common.math.Vector;
import valkyrienwarfare.mod.common.physics.management.PhysicsObject;

import javax.annotation.Nullable;

public interface IBlockForceProvider {

    /**
     * The World space version of the force vector, calculated by default from the
     * Ship space vector, do not override unless you have a good reason to.
     */
    @Nullable
    default Vector getBlockForceInWorldSpace(World world, BlockPos pos, IBlockState state, PhysicsObject physicsObject,
                                             double secondsToApply) {
        Vector toReturn = getBlockForceInShipSpace(world, pos, state, physicsObject, secondsToApply);
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
     * The force Vector this block gives within its local space (Not within World
     * space).
     */
    @Nullable
    Vector getBlockForceInShipSpace(World world, BlockPos pos, IBlockState state, PhysicsObject physicsObject,
                                    double secondsToApply);

    /**
     * Blocks that shouldn't have their force rotated (Like Ether Compressors) must
     * return false.
     */
    boolean shouldLocalForceBeRotated(World world, BlockPos pos, IBlockState state, double secondsToApply);

    /**
     * This method returns null if no changes are needed, however some blocks like
     * the balloon Burner need to apply their force in a different position.
     */
    @Nullable
    default Vector getCustomBlockForcePosition(World world, BlockPos pos, IBlockState state, PhysicsObject physicsObject,
                                               double secondsToApply) {
        return null;
    }

    /**
     * Returns true if this force provider uses 'simulated airflow' particles.
     * Useful to make certain engines not function when placed indoors.
     */
    default boolean doesForceSpawnParticles() {
        return false;
    }

}
