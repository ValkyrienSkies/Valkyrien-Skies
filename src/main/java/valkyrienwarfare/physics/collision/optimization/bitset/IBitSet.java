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

package valkyrienwarfare.physics.collision.optimization.bitset;

/**
 * A basic representation of a BitSet implementation
 *
 * @author DaPorkchop_
 */
public interface IBitSet {
    /**
     * Sets the flag at the index to true
     *
     * @param index the index to set
     */
    void set(int index);

    /**
     * Sets the flag at the index to false
     *
     * @param index the index to set
     */
    void clear(int index);

    /**
     * Sets the flag at the index to a given value
     *
     * @param index the index to set
     * @param val   the value to set
     */
    default void set(int index, boolean val) {
        if (val) {
            this.set(index);
        } else {
            this.clear(index);
        }
    }

    /**
     * Get a value at an index
     *
     * @param index the index to get
     * @return the value at the given index
     */
    boolean get(int index);
}
