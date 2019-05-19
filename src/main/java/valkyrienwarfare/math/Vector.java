/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2018 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package valkyrienwarfare.math;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import valkyrienwarfare.mod.coordinates.VectorImmutable;
import valkyrienwarfare.physics.PhysicsCalculations;

/**
 * Custom Vector class used by Valkyrien Warfare
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

    public Vector() {
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
                Y = 1d;
                break;
            case UP:
                Y = -1d;
                break;
            case EAST:
                X = -1d;
                break;
            case NORTH:
                Z = 1d;
                break;
            case WEST:
                X = 1d;
                break;
            case SOUTH:
                Z = -1d;
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

    public static void writeToBuffer(Vector vector, ByteBuf buffer) {
        buffer.writeFloat((float) vector.X);
        buffer.writeFloat((float) vector.Y);
        buffer.writeFloat((float) vector.Z);
    }

    public static Vector readFromBuffer(ByteBuf buffer) {
        return new Vector(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
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
        String coords = new String("<" + X + ", " + Y + ", " + Z + ">");
        return coords;
    }

    public String toRoundedString() {
        String coords = new String("<" + Math.round(X * 100.0) / 100.0 + ", " + Math.round(Y * 100.0) / 100.0 + ", "
                + Math.round(Z * 100.0) / 100.0 + ">");
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

    /**
     * @param other
     * @return The angle between these two vectors in radians.
     */
    public double angleBetween(Vector other) {
        double dotProduct = this.dot(other);
        double normalizedDotProduect = dotProduct / (this.length() * other.length());
        return Math.acos(dotProduct);
    }

    /**
     * Returns true if both vectors are parallel.
     *
     * @param other
     * @return
     */
    public boolean isParrallelTo(Vector other) {
        return (this.dot(other) * this.dot(other)) / (this.lengthSq() * other.lengthSq()) > .99;
    }

    /**
     * Returns true if both vectors are perpendicular.
     *
     * @param other
     * @return
     */
    public boolean isPerpendicularTo(Vector other) {
        return Math.abs(this.dot(other)) < PhysicsCalculations.EPSILON;
    }

    public VectorImmutable toImmutable() {
        return new VectorImmutable(this);
    }
}