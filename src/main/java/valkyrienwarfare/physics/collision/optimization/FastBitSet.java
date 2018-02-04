package valkyrienwarfare.physics.collision.optimization;

// Not as space efficient as BitSet (About 8x the size), but also has a much
// lower cpu cost. Consumes around 7x the memory of a bitset.
public class FastBitSet {

	private final boolean[] data;

	public FastBitSet(int size) {
		data = new boolean[size];
	}

	public void set(int index, boolean value) {
		if (value) {
			set(index);
		} else {
			clear(index);
		}
	}

	public void set(int index) {
		data[index] = true;
	}

	public void clear(int index) {
		data[index] = false;
	}

	public boolean get(int index) {
		return data[index];
	}

}
