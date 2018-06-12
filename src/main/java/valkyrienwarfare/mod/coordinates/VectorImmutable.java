package valkyrienwarfare.mod.coordinates;

import javax.annotation.concurrent.Immutable;

import valkyrienwarfare.api.Vector;

/**
 * An immutable version of the Vector class that is wrapping another vector.
 * Used to ensure that the the data used by ISubspace objects is never tampered,
 * and we can therefore consider it completely safe.
 * 
 * @author thebest108
 *
 */
@Immutable
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
	
	public Vector createMutibleVectorCopy() {
		return new Vector(vectorData);
	}
}
