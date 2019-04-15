package valkyrienwarfare.mod.coordinates;

import io.netty.buffer.ByteBuf;
import valkyrienwarfare.math.Vector;

import javax.annotation.concurrent.Immutable;

/**
 * An immutable version of the Vector class that is wrapping another vector.
 * Used to ensure that the the data used by ISubspace objects is never tampered,
 * and we can therefore consider it completely safe.
 *
 * @author thebest108
 */
@Immutable
public class VectorImmutable {

    public static final VectorImmutable ZERO_VECTOR = new VectorImmutable(0, 0, 0);
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

    public static VectorImmutable readFromByteBuf(ByteBuf bufToRead) {
        return new VectorImmutable(bufToRead.readDouble(), bufToRead.readDouble(), bufToRead.readDouble());
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

    @Override
    public String toString() {
        String coords = new String("<" + x + ", " + y + ", " + z + ">");
        return coords;
    }
}
