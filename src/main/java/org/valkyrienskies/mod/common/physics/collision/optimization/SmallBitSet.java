package org.valkyrienskies.mod.common.physics.collision.optimization;

/**
 * Slightly slower implementation of {@link BooleanArrayBitSet}, but has much lower memory
 * requirements This is better than a byte[], as java promotes the bytes to ints while working on
 * them anyway. Fits much better into the cpu cache, yielding better performance during iteration.
 */
public class SmallBitSet implements IBitSet {

    private final int[] data;

    public SmallBitSet(int size) {
        // We are using 32 bit integers, 2^5.
        data = new int[(size >> 5) + 1];
    }

    @Override
    public void set(int index) {
        data[index >> 5] |= 1 << (index & 0x1F);
    }

    @Override
    public void clear(int index) {
        data[index >> 5] &= ~(1 << (index & 0x1F));
    }

    @Override
    public boolean get(int index) {
        return (((data[index >> 5]) >> (index & 0x1F)) & 1) == 1L;
    }
}
