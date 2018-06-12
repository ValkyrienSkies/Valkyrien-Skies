package valkyrienwarfare.mod.coordinates;

import valkyrienwarfare.api.Vector;

/**
 * An immutable version of the Vector class that is wrapping another vector.
 * Used to guarantee nothing breaks the data used by ISubspace objects.
 * 
 * @author thebest108
 *
 */
public class VectorImmutable {

	private final Vector vectorData;
	
	public VectorImmutable(Vector vectorData) {
		this.vectorData = vectorData;
	}
	
	public VectorImmutable(double x, double y, double z) {
		this(new Vector(x, y, z));
	}
	
	public double getX() {
		return vectorData.X;
	}
	
	public double getY() {
		return vectorData.Y;
	}
	
	public double getZ() {
		return vectorData.Z;
	}
}
