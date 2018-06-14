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

package valkyrienwarfare.mod.physmanagement.relocation;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class ShipSpawnDetector extends SpatialDetector {

    private static final List<Block> blackList = new ArrayList<>();

    static {
        blackList.add(Blocks.AIR);
        blackList.add(Blocks.DIRT);
        blackList.add(Blocks.GRASS);
        blackList.add(Blocks.STONE);
        blackList.add(Blocks.TALLGRASS);
        // blackList.add(Blocks.LEAVES);
        // blackList.add(Blocks.LEAVES2);
        blackList.add(Blocks.WATER);
        blackList.add(Blocks.FLOWING_WATER);
        blackList.add(Blocks.SAND);
        blackList.add(Blocks.SANDSTONE);
        blackList.add(Blocks.GRAVEL);
        blackList.add(Blocks.ICE);
        blackList.add(Blocks.SNOW);
        blackList.add(Blocks.SNOW_LAYER);
        blackList.add(Blocks.LAVA);
        blackList.add(Blocks.FLOWING_LAVA);
        blackList.add(Blocks.GRASS_PATH);
        blackList.add(Blocks.BEDROCK);
        blackList.add(Blocks.END_PORTAL_FRAME);
        blackList.add(Blocks.END_PORTAL);
        blackList.add(Blocks.END_GATEWAY);
        blackList.add(Blocks.PORTAL);
    }

    public static void registerBlacklistEntry(Block block)  {
        synchronized (blackList) {
            if (block == null) {
                throw new NullPointerException("block");
            } else {
                if (!blackList.contains(block)) {
                    blackList.add(block);
                }
            }
        }
    }

    public ShipSpawnDetector(BlockPos start, World worldIn, int maximum, boolean checkCorners) {
        super(start, worldIn, maximum, checkCorners);
        startDetection();
    }

    @Override
    public boolean isValidExpansion(int x, int y, int z) {
        IBlockState state = cache.getBlockState(x, y, z);
        if (state.getBlock() == Blocks.BEDROCK) {
            cleanHouse = true;
            return false;
        }
        return !blackList.contains(state.getBlock());
        // if(state.getBlock()==Blocks.BEDROCK){
        // this.cleanHouse = true;
        // return false;
        // }
        // return !cache.getBlockState(x,y,z).getBlock().isAir(state, this.worldObj, this.tempPos.setPos(x, y, z));
    }

}
