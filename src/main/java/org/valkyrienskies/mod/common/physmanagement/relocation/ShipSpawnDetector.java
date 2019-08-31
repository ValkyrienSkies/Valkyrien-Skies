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

package org.valkyrienskies.mod.common.physmanagement.relocation;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.config.VSConfig;

public class ShipSpawnDetector extends SpatialDetector {

    private static final Set<Block> blacklist = new CopyOnWriteArraySet<>();

    static {
        VSConfig.registerSyncEvent(ShipSpawnDetector::syncWithConfig);
        // This static block doesn't get loaded until we try spawning a ship, so initially blacklist is empty.
        // We run the function here to fix that.
        syncWithConfig();
    }

    /**
     * This is called by {@link VSConfig#sync}
     */
    private static void syncWithConfig() {
        blacklist.clear();

        Arrays.stream(VSConfig.shipSpawnDetectorBlacklist)
            .map(Block::getBlockFromName)
            .forEach(blacklist::add);
    }

    private final MutableBlockPos mutablePos = new MutableBlockPos();

    ShipSpawnDetector(BlockPos start, World worldIn, int maximum, boolean checkCorners) {
        super(start, worldIn, maximum, checkCorners);
        // syncWithConfig();
        startDetection();
    }

    @Override
    public boolean isValidExpansion(int x, int y, int z) {
        mutablePos.setPos(x, y, z);
        IBlockState state = cache.getBlockState(mutablePos);
        if (state.getBlock() == Blocks.BEDROCK) {
            cleanHouse = true;
            return false;
        }
        return !blacklist.contains(state.getBlock());
    }

}
