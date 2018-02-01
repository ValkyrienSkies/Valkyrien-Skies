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

package valkyrienwarfare.relocation;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;

/**
 * Used to efficiently detect a connected set of blocks TODO: Incorporate a scanline technique to improve detection speed
 *
 * @author thebest108
 */
public abstract class SpatialDetector {

    public static final int maxRange = 512;
    public static final int maxRangeHalved = maxRange / 2;
    public static final int maxRangeSquared = maxRange * maxRange;
    public final TIntHashSet foundSet = new TIntHashSet(250);
    public final BlockPos firstBlock;
    public final MutableBlockPos tempPos = new MutableBlockPos();
    public final VWChunkCache cache;
    public final World worldObj;
    public final int maxSize;
    public final boolean corners;
    public TIntHashSet nextQueue = new TIntHashSet();
    // public int totalCalls = 0;
    public boolean cleanHouse = false;

    public SpatialDetector(BlockPos start, World worldIn, int maximum, boolean checkCorners) {
        firstBlock = start;
        worldObj = worldIn;
        maxSize = maximum;
        corners = checkCorners;
        cache = new VWChunkCache(worldIn, start.getX() - 128, start.getZ() - 128, start.getX() + 128, start.getZ() + 128);
    }

    public static int getHashWithRespectTo(int realX, int realY, int realZ, BlockPos start) {
        int x = realX - start.getX() + maxRangeHalved;
        int z = realZ - start.getZ() + maxRangeHalved;
        return realY + maxRange * x + maxRangeSquared * z;
    }

    public static BlockPos getPosWithRespectTo(int hash, BlockPos start) {
        int y = hash % maxRange;
        int x = ((hash - y) / maxRange) % maxRange;
        int z = (hash - (x * maxRange) - y) / (maxRangeSquared);
        x -= maxRangeHalved;
        z -= maxRangeHalved;
        return new BlockPos(x + start.getX(), y, z + start.getZ());
    }

    public static void setPosWithRespectTo(int hash, BlockPos start, MutableBlockPos toSet) {
        int y = hash % maxRange;
        int x = ((hash - y) / maxRange) % maxRange;
        int z = (hash - (x * maxRange) - y) / (maxRangeSquared);
        x -= maxRangeHalved;
        z -= maxRangeHalved;
        toSet.setPos(x + start.getX(), y, z + start.getZ());
    }

    public final void startDetection() {
        calculateSpatialOccupation();
        if (cleanHouse) {
            foundSet.clear();
        }
    }

    public void calculateSpatialOccupation() {
        nextQueue.add(firstBlock.getY() + maxRange * maxRangeHalved + maxRangeSquared * maxRangeHalved);
        MutableBlockPos inRealWorld = new MutableBlockPos();
        int hash;
        while (!nextQueue.isEmpty() && !cleanHouse) {
            TIntIterator queueIter = nextQueue.iterator();
            foundSet.addAll(nextQueue);
            nextQueue = new TIntHashSet();
            while (queueIter.hasNext()) {
                hash = queueIter.next();
                setPosWithRespectTo(hash, firstBlock, inRealWorld);
                if (corners) {
                    tryExpanding(inRealWorld.getX() - 1, inRealWorld.getY() - 1, inRealWorld.getZ() - 1, hash - maxRange - 1 - maxRangeSquared);
                    tryExpanding(inRealWorld.getX() - 1, inRealWorld.getY() - 1, inRealWorld.getZ(), hash - maxRange - 1);
                    tryExpanding(inRealWorld.getX() - 1, inRealWorld.getY() - 1, inRealWorld.getZ() + 1, hash - maxRange - 1 + maxRangeSquared);
                    tryExpanding(inRealWorld.getX() - 1, inRealWorld.getY(), inRealWorld.getZ() - 1, hash - maxRange - maxRangeSquared);
                    tryExpanding(inRealWorld.getX() - 1, inRealWorld.getY(), inRealWorld.getZ(), hash - maxRange);
                    tryExpanding(inRealWorld.getX() - 1, inRealWorld.getY(), inRealWorld.getZ() + 1, hash - maxRange + maxRangeSquared);
                    tryExpanding(inRealWorld.getX() - 1, inRealWorld.getY() + 1, inRealWorld.getZ() - 1, hash - maxRange + 1 - maxRangeSquared);
                    tryExpanding(inRealWorld.getX() - 1, inRealWorld.getY() + 1, inRealWorld.getZ(), hash - maxRange + 1);
                    tryExpanding(inRealWorld.getX() - 1, inRealWorld.getY() + 1, inRealWorld.getZ() + 1, hash - maxRange + 1 + maxRangeSquared);

                    tryExpanding(inRealWorld.getX(), inRealWorld.getY() - 1, inRealWorld.getZ() - 1, hash - 1 - maxRangeSquared);
                    tryExpanding(inRealWorld.getX(), inRealWorld.getY() - 1, inRealWorld.getZ(), hash - 1);
                    tryExpanding(inRealWorld.getX(), inRealWorld.getY() - 1, inRealWorld.getZ() + 1, hash - 1 + maxRangeSquared);
                    tryExpanding(inRealWorld.getX(), inRealWorld.getY(), inRealWorld.getZ() - 1, hash - maxRangeSquared);
                    tryExpanding(inRealWorld.getX(), inRealWorld.getY(), inRealWorld.getZ() + 1, hash + maxRangeSquared);
                    tryExpanding(inRealWorld.getX(), inRealWorld.getY() + 1, inRealWorld.getZ() - 1, hash + 1 - maxRangeSquared);
                    tryExpanding(inRealWorld.getX(), inRealWorld.getY() + 1, inRealWorld.getZ(), hash + 1);
                    tryExpanding(inRealWorld.getX(), inRealWorld.getY() + 1, inRealWorld.getZ() + 1, hash + 1 + maxRangeSquared);

                    tryExpanding(inRealWorld.getX() + 1, inRealWorld.getY() - 1, inRealWorld.getZ() - 1, hash + maxRange - 1 - maxRangeSquared);
                    tryExpanding(inRealWorld.getX() + 1, inRealWorld.getY() - 1, inRealWorld.getZ(), hash + maxRange - 1);
                    tryExpanding(inRealWorld.getX() + 1, inRealWorld.getY() - 1, inRealWorld.getZ() + 1, hash + maxRange - 1 + maxRangeSquared);
                    tryExpanding(inRealWorld.getX() + 1, inRealWorld.getY(), inRealWorld.getZ() - 1, hash + maxRange - maxRangeSquared);
                    tryExpanding(inRealWorld.getX() + 1, inRealWorld.getY(), inRealWorld.getZ(), hash + maxRange);
                    tryExpanding(inRealWorld.getX() + 1, inRealWorld.getY(), inRealWorld.getZ() + 1, hash + maxRange + maxRangeSquared);
                    tryExpanding(inRealWorld.getX() + 1, inRealWorld.getY() + 1, inRealWorld.getZ() - 1, hash + maxRange + 1 - maxRangeSquared);
                    tryExpanding(inRealWorld.getX() + 1, inRealWorld.getY() + 1, inRealWorld.getZ(), hash + maxRange + 1);
                    tryExpanding(inRealWorld.getX() + 1, inRealWorld.getY() + 1, inRealWorld.getZ() + 1, hash + maxRange + 1 + maxRangeSquared);
                } else {
                    tryExpanding(inRealWorld.getX() + 1, inRealWorld.getY(), inRealWorld.getZ(), hash + maxRange);
                    tryExpanding(inRealWorld.getX() - 1, inRealWorld.getY(), inRealWorld.getZ(), hash - maxRange);
                    tryExpanding(inRealWorld.getX(), inRealWorld.getY() + 1, inRealWorld.getZ(), hash + 1);
                    tryExpanding(inRealWorld.getX(), inRealWorld.getY() - 1, inRealWorld.getZ(), hash - 1);
                    tryExpanding(inRealWorld.getX(), inRealWorld.getY(), inRealWorld.getZ() + 1, hash + maxRangeSquared);
                    tryExpanding(inRealWorld.getX(), inRealWorld.getY(), inRealWorld.getZ() - 1, hash - maxRangeSquared);
                }
            }
        }
    }

    public void tryExpanding(int x, int y, int z, int hash) {
        if (isValidExpansion(x, y, z)) {
            // totalCalls++;
            if (!foundSet.contains(hash) && (foundSet.size() + nextQueue.size() < maxSize)) {
                nextQueue.add(hash);
            }
        }
    }

    public ArrayList<BlockPos> getBlockPosArrayList() {
        ArrayList<BlockPos> detectedBlockPos = new ArrayList<BlockPos>();
        TIntIterator intIter = foundSet.iterator();
        while (intIter.hasNext()) {
            int hash = intIter.next();
            BlockPos fromHash = getPosWithRespectTo(hash, firstBlock);
            if (fromHash.getY() + 128 - firstBlock.getY() < 0) {
                System.err.println("I really hope this doesnt happen");
                return new ArrayList<BlockPos>();
            }
            detectedBlockPos.add(fromHash);
        }
        return detectedBlockPos;
    }

    public abstract boolean isValidExpansion(int x, int y, int z);
}