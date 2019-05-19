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

package valkyrienwarfare.mixin.world.chunk;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BitArray;
import net.minecraft.world.chunk.BlockStateContainer;
import net.minecraft.world.chunk.IBlockStatePalette;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import valkyrienwarfare.physics.collision.optimization.IBitOctree;
import valkyrienwarfare.physics.collision.optimization.IBitOctreeProvider;
import valkyrienwarfare.physics.collision.optimization.SimpleBitOctree;

@Mixin(BlockStateContainer.class)
public class MixinBlockStateContainer implements IBitOctreeProvider {

    @Shadow
    @Final
    public static IBlockState AIR_BLOCK_STATE;
    private final IBitOctree bitOctree = new SimpleBitOctree();
    @Shadow
    IBlockStatePalette palette;
    @Shadow
    BitArray storage;

    /**
     * @author thebest108
     */
    @Overwrite
    public void set(int index, IBlockState state) {
        if (state == null) {
            state = AIR_BLOCK_STATE;
        }
        int i = this.palette.idFor(state);
        this.storage.setAt(index, i);
        // VW code starts here:
        int x = index & 0xF;
        int z = (index & 0xF0) >> 4;
        int y = (index & 0xF00) >> 8;
        boolean isStateSolid = state.getMaterial().isSolid();
        bitOctree.set(x & 15, y & 15, z & 15, isStateSolid);
    }

    @Override
    public IBitOctree getBitOctree() {
        return bitOctree;
    }
}