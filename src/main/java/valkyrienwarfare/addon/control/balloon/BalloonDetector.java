/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2017 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package valkyrienwarfare.addon.control.balloon;

import valkyrienwarfare.relocation.SpatialDetector;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class BalloonDetector extends SpatialDetector {

	public final TIntHashSet balloonWalls = new TIntHashSet(250);
	private final MutableBlockPos mutable = new MutableBlockPos();

	public BalloonDetector(BlockPos start, World worldIn, int maximum) {
		super(start, worldIn, maximum, false);
		startDetection();
	}

	@Override
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

				tryExpanding(inRealWorld.getX() + 1, inRealWorld.getY(), inRealWorld.getZ(), hash + maxRange);
				tryExpanding(inRealWorld.getX() - 1, inRealWorld.getY(), inRealWorld.getZ(), hash - maxRange);
				tryExpanding(inRealWorld.getX(), inRealWorld.getY() + 1, inRealWorld.getZ(), hash + 1);
				// tryExpanding(inRealWorld.getX(),inRealWorld.getY()-1,inRealWorld.getZ(),hash-1);
				tryExpanding(inRealWorld.getX(), inRealWorld.getY(), inRealWorld.getZ() + 1, hash + maxRangeSquared);
				tryExpanding(inRealWorld.getX(), inRealWorld.getY(), inRealWorld.getZ() - 1, hash - maxRangeSquared);
			}
		}
	}

	@Override
	public void tryExpanding(int x, int y, int z, int hash) {
		if (isValidExpansion(x, y, z)) {
			if (!foundSet.contains(hash) && (foundSet.size() + nextQueue.size() < maxSize)) {
				nextQueue.add(hash);
			}
		} else {
			if (!balloonWalls.contains(hash)) {
				balloonWalls.add(hash);
			}
		}
	}

	@Override
	public boolean isValidExpansion(int x, int y, int z) {
		IBlockState state = cache.getBlockState(x, y, z);
		if (!state.getBlock().blockMaterial.blocksMovement()) {
			Chunk chunk = cache.getChunkAt(x >> 4, z >> 4);
			mutable.setPos(x, y, z);
			if (!chunk.canSeeSky(mutable)) {
				return true;
			} else {
				cleanHouse = true;
				// System.out.println("Found a Hole in the Air-balloon");
			}
		}
		return false;
	}

}
