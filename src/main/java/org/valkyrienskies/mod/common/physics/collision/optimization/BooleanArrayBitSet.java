package org.valkyrienskies.mod.common.physics.collision.optimization;

/**
 * Not as space efficient as BitSet (about 8x the size), but also has a much lower cpu cost. Has no
 * bounds checking to reduce overhead.
 */
public class BooleanArrayBitSet implements IBitSet {

    private final boolean[] data;

    public BooleanArrayBitSet(int size) {
        data = new boolean[size];
    }

    @Override
    public void set(int index) {
        data[index] = true;
    }

    @Override
    public void clear(int index) {
        data[index] = false;
    }

    @Override
    public boolean get(int index) {
        return data[index];
    }

}
