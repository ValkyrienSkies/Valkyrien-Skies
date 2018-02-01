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

package valkyrienwarfare.physics;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import valkyrienwarfare.api.IBlockForceProvider;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.physicsmanagement.PhysicsObject;

import java.util.HashMap;

public class BlockForce {

    public static BlockForce basicForces = new BlockForce();

    public HashMap<Block, Force> blocksToForces = new HashMap<Block, Force>();

    public static void registerBlockForce(Block block, Vector forceVec, boolean isLocal) {
        Force force = new Force(forceVec.X, forceVec.Y, forceVec.Z, isLocal);
        basicForces.blocksToForces.put(block, force);
    }

    public void getForceFromState(IBlockState state, BlockPos pos, World world, double secondsToApply, PhysicsObject obj, Vector toSet) {
        Block block = state.getBlock();
        if (block instanceof IBlockForceProvider) {
            Vector forceVector = ((IBlockForceProvider) block).getBlockForceInWorldSpace(world, pos, state, obj.wrapper, secondsToApply);
            if (forceVector == null) {
                toSet.zero();
                return;
            }
            toSet.X = forceVector.X;
            toSet.Y = forceVector.Y;
            toSet.Z = forceVector.Z;
            return;
        }
        Force force = basicForces.blocksToForces.get(block);
        if (force != null) {
            toSet.X = force.X * secondsToApply;
            toSet.Y = force.Y * secondsToApply;
            toSet.Z = force.Z * secondsToApply;
        } else {
            toSet.zero();
        }
    }

    public boolean isBlockProvidingForce(IBlockState state, BlockPos pos, World world) {
        Block block = state.getBlock();
        return basicForces.blocksToForces.containsKey(block) || block instanceof IBlockForceProvider;
    }

}
