package org.valkyrienskies.mod.common.physics.collision.optimization;

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
