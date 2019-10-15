package org.valkyrienskies.mod.common.coordinates;

import io.netty.buffer.ByteBuf;
import javax.annotation.concurrent.Immutable;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.util.math.Vec3d;
import org.valkyrienskies.mod.common.math.Vector;

/**
 * An immutable version of the Vector class that is wrapping another vector. Used to ensure that the
 * the data used by ISubspace objects is never tampered, and we can therefore consider it completely
 * safe.
 *
 * @author thebest108
 */
@Immutable
@MethodsReturnNonnullByDefault
public class VectorImmutable extends Vec3d {

    public static final VectorImmutable ZERO_VECTOR = new VectorImmutable(0, 0, 0);

    public VectorImmutable(double x, double y, double z) {
        super(x, y, z);
    }

    public VectorImmutable(Vector vectorData) {
        this(vectorData.X, vectorData.Y, vectorData.Z);
    }

    public static VectorImmutable readFromByteBuf(ByteBuf bufToRead) {
        return new VectorImmutable(bufToRead.readDouble(), bufToRead.readDouble(),
            bufToRead.readDouble());
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

    /**
     * Do not use this method. Use <code>new Vector(VectorImmutable vec)</code> instead.
     */
    @Deprecated
    public Vector createMutableVectorCopy() {
        return new Vector(this);
    }

    public void writeToByteBuf(ByteBuf bufToWrite) {
        bufToWrite.writeDouble(this.getX());
        bufToWrite.writeDouble(this.getY());
        bufToWrite.writeDouble(this.getZ());
    }

    @Override
    public String toString() {
        return "<" + x + ", " + y + ", " + z + ">";
    }
}
