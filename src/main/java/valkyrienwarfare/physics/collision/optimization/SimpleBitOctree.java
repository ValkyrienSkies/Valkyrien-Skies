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

package valkyrienwarfare.physics.collision.optimization;

import java.util.BitSet;

public class SimpleBitOctree implements IBitOctree {

	private final BitSet bitbuffer;

	public SimpleBitOctree() {
		bitbuffer = new BitSet(BITS_TOTAL);
	}

	@Override
	public void set(int x, int y, int z, boolean bit) {
		bitbuffer.set(getBlockIndex(x, y, z), bit);
		updateOctrees(x, y, z, bit);
	}

	@Override
	public boolean get(int x, int y, int z) {
		return bitbuffer.get(getBlockIndex(x, y, z));
	}

	@Override
	public boolean getAtIndex(int index) {
		if (index > BITS_TOTAL) {
			throw new IllegalArgumentException();
		}
		return bitbuffer.get(index);
	}

	private void updateOctrees(int x, int y, int z, boolean bit) {
		int levelThreeIndex = getOctreeLevelThreeIndex(x, y, z);
		int levelTwoIndex = getOctreeLevelTwoIndex(x, y, z, levelThreeIndex);
		int levelOneIndex = getOctreeLevelOneIndex(x, y, z, levelTwoIndex);

		updateOctreeLevelOne(levelOneIndex, bit);
		updateOctreeLevelTwo(levelTwoIndex);
		updateOctreeLevelThree(levelThreeIndex);
	}

	private void updateOctreeLevelThree(int levelThreeIndex) {
		if (bitbuffer.get(levelThreeIndex + 1) || bitbuffer.get(levelThreeIndex + 10)
				|| bitbuffer.get(levelThreeIndex + 19) || bitbuffer.get(levelThreeIndex + 28)
				|| bitbuffer.get(levelThreeIndex + 37) || bitbuffer.get(levelThreeIndex + 46)
				|| bitbuffer.get(levelThreeIndex + 55) || bitbuffer.get(levelThreeIndex + 64)) {
			bitbuffer.set(levelThreeIndex);
		} else {
			bitbuffer.clear(levelThreeIndex);
		}
	}

	private void updateOctreeLevelTwo(int levelTwoIndex) {
		if (bitbuffer.get(levelTwoIndex + 1) || bitbuffer.get(levelTwoIndex + 2) || bitbuffer.get(levelTwoIndex + 3)
				|| bitbuffer.get(levelTwoIndex + 4) || bitbuffer.get(levelTwoIndex + 5)
				|| bitbuffer.get(levelTwoIndex + 6) || bitbuffer.get(levelTwoIndex + 7)
				|| bitbuffer.get(levelTwoIndex + 8)) {
			bitbuffer.set(levelTwoIndex);
		} else {
			bitbuffer.clear(levelTwoIndex);
		}
	}

	private void updateOctreeLevelOne(int levelOneIndex, boolean bit) {
		bitbuffer.set(levelOneIndex, bit);
	}

	private int getOctreeLevelOneIndex(int x, int y, int z, int levelTwoIndex) {
		x = (x & 0x02) >> 1;
		y = (y & 0x02);
		z = (z & 0x02) << 1;
		int offset = x | y | z;
		;
		return getOctreeLevelOneIndex(levelTwoIndex, offset);
	}

	private int getOctreeLevelTwoIndex(int x, int y, int z, int levelThreeIndex) {
		x = (x & 0x04) >> 2;
		y = (y & 0x04) >> 1;
		z = (z & 0x04);
		int offset = x | y | z;
		return getOctreeLevelTwoIndex(levelThreeIndex, offset);
	}

	private int getOctreeLevelThreeIndex(int x, int y, int z) {
		x = (x & 0x08) >> 3;
		y = (y & 0x08) >> 2;
		z = (z & 0x08) >> 1;
		int offset = (x | y | z);
		return getOctreeLevelThreeIndex(offset);
	}

	@Override
	public int getOctreeLevelOneIndex(int levelTwoIndex, int offset) {
		return levelTwoIndex + offset + 1;
	}

	@Override
	public int getOctreeLevelTwoIndex(int levelThreeIndex, int offset) {
		return levelThreeIndex + (9 * offset) + 1;
	}

	@Override
	public int getOctreeLevelThreeIndex(int offset) {
		return BLOCKS_TOTAL + (73 * offset);
	}

	private int getBlockIndex(int x, int y, int z) {
		return x | (y << 4) | (z << 8);
	}

}
