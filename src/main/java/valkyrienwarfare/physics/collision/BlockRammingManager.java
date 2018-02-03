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

package valkyrienwarfare.physics.collision;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import valkyrienwarfare.physics.data.BlockMass;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

/**
 * Given the sets of inputs, this class decides which blocks should be rammed, and which blocks shouldn't
 *
 * @author BigBastard
 */
public class BlockRammingManager {

    //If either block broke, only apply 20% of the collision
    public static double collisionImpulseAfterRamming = .20D;
    public static double minimumVelocityToApply = 3.0D;


    /**
     * Returns percentage of power to apply collision
     *
     * @param collisionSpeed
     * @param inLocalState
     * @param inWorldState
     * @param didBlockBreakInShip
     * @param didBlockBreakInWorld
     * @return
     */
    public static double processBlockRamming(PhysicsWrapperEntity wrapper, double collisionSpeed, IBlockState inLocalState, IBlockState inWorldState, BlockPos inLocal, BlockPos inWorld, NestedBoolean didBlockBreakInShip, NestedBoolean didBlockBreakInWorld) {
        if (Math.abs(collisionSpeed) > 2D) {
            double shipBlockHardness = inLocalState.getBlock().blockResistance;//inLocalState.getBlockHardness(worldObj, inLocalPos);
            double worldBlockHardness = inWorldState.getBlock().blockResistance;//inWorldState.getBlockHardness(worldObj, inWorldPos);

            double hardnessRatio = Math.pow(worldBlockHardness / shipBlockHardness, Math.abs(collisionSpeed) / 5D);

            if (worldBlockHardness == -1) {
                worldBlockHardness = 100D;
            }

            if (shipBlockHardness == -1) {
                shipBlockHardness = 100D;
            }

            double arbitraryScale = 5.4D;

            if (hardnessRatio < .01D) {
                didBlockBreakInWorld.setValue(true);
                double shipBlockMass = BlockMass.basicMass.getMassFromState(inLocalState, inLocal, wrapper.world);
                double worldBlockMass = BlockMass.basicMass.getMassFromState(inWorldState, inWorld, wrapper.world);
//				return worldBlockMass / shipBlockMass;
                return Math.pow(worldBlockMass / worldBlockMass, arbitraryScale);//wrapper.wrapping.physicsProcessor.mass;
            }
            if (hardnessRatio > 100D) {
                didBlockBreakInShip.setValue(true);
                double shipBlockMass = BlockMass.basicMass.getMassFromState(inLocalState, inLocal, wrapper.world);
                double worldBlockMass = BlockMass.basicMass.getMassFromState(inWorldState, inWorld, wrapper.world);
//				return shipBlockMass / worldBlockMass;
                return Math.pow(shipBlockMass / worldBlockMass, arbitraryScale);//wrapper.wrapping.physicsProcessor.mass;
            }

        }

        return 1;
    }

    public static final class NestedBoolean {

        private boolean value;

        public NestedBoolean(boolean value) {
            this.value = value;
        }

        public boolean getValue() {
            return value;
        }

        public void setValue(boolean value) {
            this.value = value;
        }
    }

}