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
import gnu.trove.set.hash.TIntHashSet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;

import java.util.HashSet;

public class BalloonAirDetector extends SpatialDetector {

	public final TIntHashSet foundBalloonWalls = new TIntHashSet(250);
	private final BalloonProcessor holeOwner;
	private final MutableBlockPos mutable = new MutableBlockPos();
	private final HashSet<BlockPos> possiblePositions;

	public BalloonAirDetector(BlockPos start, World worldIn, int maximum, BalloonProcessor processor, HashSet<BlockPos> avaliablePositions) {
		super(start, worldIn, maximum, false);
		holeOwner = processor;
		possiblePositions = avaliablePositions;
		startDetection();
	}

	@Override
	public boolean isValidExpansion(int x, int y, int z) {
		mutable.setPos(x, y, z);

		boolean isAir = possiblePositions.contains(mutable);
		if (!isAir) {
			if (holeOwner.balloonWalls.contains(mutable)) {
				foundBalloonWalls.add(this.getHashWithRespectTo(x, y, z, firstBlock));
			}
			return false;
		}
		return true;
	}

}
