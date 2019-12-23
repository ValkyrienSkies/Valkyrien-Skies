package org.valkyrienskies.mod.common.math;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.valkyrienskies.mod.common.coordinates.VectorImmutable;

/**
 * Custom Vector class used by Valkyrien Skies
 *
 * @author thebest108
 */
public class Vector {

    public double X;
    public double Y;
    public double Z;

    public Vector(double x, double y, double z) {
        X = x;
        Y = y;
        Z = z;
    }

    public Vector() {}

    /**
     * Construct a copy of a {@link VectorImmutable}
     */
    public Vector(VectorImmutable vec) {
        this(vec.getX(), vec.getY(), vec.getZ());
    }

    public Vector(double x, double y, double z, double[] rotationMatrix) {
        this(x, y, z);
        transform(rotationMatrix);
    }

    public Vector(Vector v) {
        this(v.X, v.Y, v.Z);
    }

    public Vector(Vector v, double scale) {
        this(v);
        multiply(scale);
    }

    public Vector(Vec3d vec3) {
        this(vec3.x, vec3.y, vec3.z);
    }

    public Vector(Entity entity) {
        this(entity.posX, entity.posY, entity.posZ);
    }

    public Vector(ByteBuf toRead) {
        this(toRead.readDouble(), toRead.readDouble(), toRead.readDouble());
    }

    public Vector(Vector theNormal, double[] matrixTransform) {
        this(theNormal.X, theNormal.Y, theNormal.Z, matrixTransform);
    }

    public Vector(EnumFacing facing) {
        switch (facing) {
            case DOWN:
                Y = 1;
                break;
            case UP:
                Y = -1;
                break;
            case EAST:
                X = -1;
                break;
            case NORTH:
                Z = 1;
                break;
            case WEST:
                X = 1;
                break;
            case SOUTH:
                Z = -1;
        }
    }

    public Vector(Vec3i directionVec) {
        this(directionVec.getX(), directionVec.getY(), directionVec.getZ());
    }

    public static Vector[] generateAxisAlignedNorms() {
        Vector[] norms = new Vector[]{new Vector(1.0D, 0.0D, 0.0D), new Vector(0.0D, 1.0D, 0.0D),
            new Vector(0.0D, 0.0D, 1.0D)};
        return norms;
    }

    public Vector getSubtraction(Vector v) {
        return new Vector(v.X - X, v.Y - Y, v.Z - Z);
    }

    public Vector getAddition(Vector v) {
        return new Vector(v.X + X, v.Y + Y, v.Z + Z);
    }

    public void subtract(Vector v) {
        subtract(v.X, v.Y, v.Z);
    }

    public void subtract(double x, double y, double z) {
        X -= x;
        Y -= y;
        Z -= z;
    }

    public void add(Vector v) {
        add(v.X, v.Y, v.Z);
    }

    public void add(double x, double y, double z) {
        X += x;
        Y += y;
        Z += z;
    }

    public double dot(Vector v) {
        return X * v.X + Y * v.Y + Z * v.Z;
    }

    public Vector cross(Vector v) {
        return new Vector(Y * v.Z - v.Y * Z, Z * v.X - X * v.Z, X * v.Y - v.X * Y);
    }

    public void setCross(Vector v1, Vector v2) {
        X = v1.Y * v2.Z - v2.Y * v1.Z;
        Y = v1.Z * v2.X - v1.X * v2.Z;
        Z = v1.X * v2.Y - v2.X * v1.Y;
    }

    public void multiply(double scale) {
        X *= scale;
        Y *= scale;
        Z *= scale;
    }

    public void divide(double scale) {
        X /= scale;
        Y /= scale;
        Z /= scale;
    }

    public Vector getProduct(double scale) {
        return new Vector(X * scale, Y * scale, Z * scale);
    }

    public Vec3d toVec3d() {
        return new Vec3d(X, Y, Z);
    }

    public void normalize() {
        double length = length();
        if (length > 1.0E-6D) {
            divide(length);
        } else {
            zero();
        }
    }

    public double length() {
        return Math.sqrt(lengthSq());
    }

    public double lengthSq() {
        return X * X + Y * Y + Z * Z;
    }

    public boolean isZero() {
        return lengthSq() < 1.0E-12D;
    }

    public void zero() {
        X = Y = Z = 0D;
    }

    public void roundToWhole() {
        X = Math.round(X);
        Y = Math.round(Y);
        Z = Math.round(Z);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        } else if (other instanceof Vector) {
            Vector vec = (Vector) other;
            return vec.X == X && vec.Y == Y && vec.Z == Z;
        }
        return false;
    }

    @Override
    public String toString() {
        String coords = "<" + X + ", " + Y + ", " + Z + ">";
        return coords;
    }

    public Vector crossAndUnit(Vector v) {
        Vector crossProduct = cross(v);
        crossProduct.normalize();
        return crossProduct;
    }

    public void writeToByteBuf(ByteBuf toWrite) {
        toWrite.writeDouble(X);
        toWrite.writeDouble(Y);
        toWrite.writeDouble(Z);
    }

    public void setSubtraction(Vector inLocal, Vector centerCoord) {
        X = inLocal.X - centerCoord.X;
        Y = inLocal.Y - centerCoord.Y;
        Z = inLocal.Z - centerCoord.Z;
    }

    public void transform(double[] rotationMatrix) {
        RotationMatrices.applyTransform(rotationMatrix, this);
    }

    public void setValue(double x, double y, double z) {
        X = x;
        Y = y;
        Z = z;
    }

    public void setValue(Vector toCopy) {
        setValue(toCopy.X, toCopy.Y, toCopy.Z);
    }

    public void setValue(Vec3d toCopy) {
        setValue(toCopy.x, toCopy.y, toCopy.z);
    }

    /**
     * @param other
     * @return The angle between these two vectors in radians.
     */
    public double angleBetween(Vector other) {
        double dotProduct = this.dot(other);
        double normalizedDotProduct = dotProduct / (this.length() * other.length());
        return Math.acos(normalizedDotProduct);
    }

    public VectorImmutable toImmutable() {
        return new VectorImmutable(this);
    }

    /**
     * Returns true if at least one of the components of this vector is NaN.
     *
     * @return
     */
    public boolean isNaN() {
        return Double.isNaN(X) || Double.isNaN(Y) || Double.isNaN(Z);
    }
}