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

package valkyrienwarfare.api;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IBlockForceProvider {

    /**
     * The World space version of the force vector, calculated by default from the
     * Ship space vector, do not override unless you have a good reason to
     *
     * @param world
     * @param pos
     * @param state
     * @param shipEntity
     * @param secondsToApply
     * @return
     */
    default Vector getBlockForceInWorldSpace(World world, BlockPos pos, IBlockState state, Entity shipEntity,
                                             double secondsToApply) {
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

    // Multiply your power usage values or whatever by the secondsToApply, otherwise
    // you'll have issues (Example: <0,400,0> -(.01)-> <0,40,0>

    /**
     * The force Vector this block gives within its local space (Not within World
     * space), should only be used for static blocks with forces that do not need
     * updating.
     *
     * @param world
     * @param pos
     * @param state
     * @param shipEntity
     * @param secondsToApply
     * @return
     */
    Vector getBlockForceInShipSpace(World world, BlockPos pos, IBlockState state, Entity shipEntity,
                                    double secondsToApply);

    /**
     * Returns true by default, blocks that shouldn't have their force rotated (Like
     * Ether Compressors) must return false
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
     * This method returns null if no changes are needed, however some blocks like
     * the balloon Burner need to apply their force in a different position
     *
     * @param world
     * @param pos
     * @param state
     * @param shipEntity
     * @param secondsToApply
     * @return
     */
    default Vector getCustomBlockForcePosition(World world, BlockPos pos, IBlockState state, Entity shipEntity,
                                               double secondsToApply) {
        return null;
    }

}
