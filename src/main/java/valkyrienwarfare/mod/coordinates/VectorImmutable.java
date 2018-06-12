package valkyrienwarfare.mod.coordinates;

import javax.annotation.concurrent.Immutable;

import io.netty.buffer.ByteBuf;
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

	private final double x;
	private final double y;
	private final double z;
	
	public VectorImmutable(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public VectorImmutable(Vector vectorData) {
		this(vectorData.X, vectorData.Y, vectorData.Z);
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public double getZ() {
		return z;
	}
	
	public Vector createMutibleVectorCopy() {
		return new Vector(x, y, z);
	}
	
	public void writeToByteBuf(ByteBuf bufToWrite) {
		bufToWrite.writeDouble(this.getX());
		bufToWrite.writeDouble(this.getY());
		bufToWrite.writeDouble(this.getZ());
	}
	
	public static VectorImmutable readFromByteBuf(ByteBuf bufToRead) {
		return new VectorImmutable(bufToRead.readDouble(), bufToRead.readDouble(), bufToRead.readDouble());
	}
}
