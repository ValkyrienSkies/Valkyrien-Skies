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

public class SimpleBitOctree implements IBitOctree {

    private final FastBitSet bitbuffer;
    // private final BitSet bifbuffer;

    public SimpleBitOctree() {
        bitbuffer = new FastBitSet(BITS_TOTAL);
        // bitbuffer = new BitSet(BITS_TOTAL);
    }

    @Override
    public void set(int x, int y, int z, boolean bit) {
        int index = getBlockIndex(x, y, z);
        ensureCapacity(index);
        if (bitbuffer.get(index) != bit) {
            bitbuffer.set(index, bit);
            updateOctrees(x, y, z, bit);
        }
    }

    @Override
    public boolean get(int x, int y, int z) {
        return getAtIndex(getBlockIndex(x, y, z));
    }

    @Override
    public boolean getAtIndex(int index) {
        ensureCapacity(index);
        return bitbuffer.get(index);
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
    
    // If something tried calling code outside of the buffer size, throw an
    // IllegalArgumentException its way.
    private void ensureCapacity(int index) {
        if (index > BITS_TOTAL) {
            throw new IllegalArgumentException();
        }
    }

    private void updateOctrees(int x, int y, int z, boolean bit) {
        int levelThreeIndex = getOctreeLevelThreeIndex(x, y, z);
        int levelTwoIndex = getOctreeLevelTwoIndex(x, y, z, levelThreeIndex);
        int levelOneIndex = getOctreeLevelOneIndex(x, y, z, levelTwoIndex);

        if (getAtIndex(levelOneIndex) != bit) {
            if (updateOctreeLevelOne(levelOneIndex, x, y, z)) {
                if (updateOctreeLevelTwo(levelTwoIndex)) {
                    updateOctreeLevelThree(levelThreeIndex);
                }
            }
        }
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

    // Returns true if the next level of octree should be updated
    private boolean updateOctreeLevelTwo(int levelTwoIndex) {
        if (bitbuffer.get(levelTwoIndex + 1) || bitbuffer.get(levelTwoIndex + 2) || bitbuffer.get(levelTwoIndex + 3)
                || bitbuffer.get(levelTwoIndex + 4) || bitbuffer.get(levelTwoIndex + 5)
                || bitbuffer.get(levelTwoIndex + 6) || bitbuffer.get(levelTwoIndex + 7)
                || bitbuffer.get(levelTwoIndex + 8)) {
            if (!bitbuffer.get(levelTwoIndex)) {
                bitbuffer.set(levelTwoIndex);
                return true;
            }
        } else {
            if (bitbuffer.get(levelTwoIndex)) {
                bitbuffer.clear(levelTwoIndex);
                return true;
            }
        }
        return false;
    }

    // Returns true if the next level of octree should be updated
    private boolean updateOctreeLevelOne(int levelOneIndex, int x, int y, int z) {
        // Only keep the last 4 bits; 0x0E = 1110, also removes the last bit
        x &= 0x0E;
        y &= 0x0E;
        z &= 0x0E;
        if (get(x, y, z) || get(x, y, z + 1) || get(x, y + 1, z) || get(x, y + 1, z + 1) || get(x + 1, y, z)
                || get(x + 1, y, z + 1) || get(x + 1, y + 1, z) || get(x + 1, y + 1, z + 1)) {
            if (!bitbuffer.get(levelOneIndex)) {
                bitbuffer.set(levelOneIndex);
                return true;
            }
        } else {
            if (bitbuffer.get(levelOneIndex)) {
                bitbuffer.clear(levelOneIndex);
                return true;
            }
        }
        return false;
    }

    private int getOctreeLevelOneIndex(int x, int y, int z, int levelTwoIndex) {
        x = (x & 0x02) >> 1;
        y = (y & 0x02);
        z = (z & 0x02) << 1;
        return getOctreeLevelOneIndex(levelTwoIndex, x | y | z);
    }

    private int getOctreeLevelTwoIndex(int x, int y, int z, int levelThreeIndex) {
        x = (x & 0x04) >> 2;
        y = (y & 0x04) >> 1;
        z = (z & 0x04);
        return getOctreeLevelTwoIndex(levelThreeIndex, x | y | z);
    }

    private int getOctreeLevelThreeIndex(int x, int y, int z) {
        x = (x & 0x08) >> 3;
        y = (y & 0x08) >> 2;
        z = (z & 0x08) >> 1;
        return getOctreeLevelThreeIndex(x | y | z);
    }

    private int getBlockIndex(int x, int y, int z) {
        return x | (y << 4) | (z << 8);
    }

}
