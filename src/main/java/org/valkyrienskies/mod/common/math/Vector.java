/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2019 the Valkyrien Skies team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Skies team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Skies team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package org.valkyrienskies.mod.common.math;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.mod.common.coordinates.VectorImmutable;

/**
 * Custom Vector class used by Valkyrien Skies
 *
 * @author thebest108
 * @deprecated Use JOML {@link Vector3d}
 */
@Data
@Deprecated
public class Vector {

    public double x;
    public double y;
    public double z;

    public Vector(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector() {
        this(0, 0, 0);
    }

    /**
     * Construct a copy of a {@link VectorImmutable}
     */
    public Vector(VectorImmutable vec) {
        this(vec.getX(), vec.getY(), vec.getZ());
    }

    /**
     * Construct a copy of a {@link Vector}
     */
    public Vector(Vector vec) {
        this(vec.x, vec.y, vec.z);
    }

    /**
     * Construct a copy of a {@link Vec3i}
     */
    public Vector(Vec3i vec) {
        this(vec.getX(), vec.getY(), vec.getZ());
    }

    /**
     * Construct a copy of a {@link Vec3d}
     */
    public Vector(Vec3d vec) {
        this(vec.x, vec.y, vec.z);
    }

    public Vector(double x, double y, double z, double[] rotationMatrix) {
        this(x, y, z);
        transform(rotationMatrix);
    }

    public Vector(Vector v, double scale) {
        this(v);
        multiply(scale);
    }

    public Vector(Entity entity) {
        this(entity.posX, entity.posY, entity.posZ);
    }

    public Vector(ByteBuf toRead) {
        this(toRead.readDouble(), toRead.readDouble(), toRead.readDouble());
    }

    public Vector(Vector theNormal, double[] matrixTransform) {
        this(theNormal.x, theNormal.y, theNormal.z, matrixTransform);
    }

    public Vector(EnumFacing facing) {
        switch (facing) {
            case DOWN:
                y = 1;
                break;
            case UP:
                y = -1;
                break;
            case EAST:
                x = -1;
                break;
            case NORTH:
                z = 1;
                break;
            case WEST:
                x = 1;
                break;
            case SOUTH:
                z = -1;
        }
    }

    public Vector(Vector3dc vector3dc) {
        this(vector3dc.x(), vector3dc.y(), vector3dc.z());
    }

    public static Vector[] generateAxisAlignedNorms() {
        return new Vector[]{new Vector(1.0D, 0.0D, 0.0D), new Vector(0.0D, 1.0D, 0.0D),
            new Vector(0.0D, 0.0D, 1.0D)};
    }

    public Vector getSubtraction(Vector v) {
        return new Vector(v.x - x, v.y - y, v.z - z);
    }

    public Vector getAddition(Vector v) {
        return new Vector(v.x + x, v.y + y, v.z + z);
    }

    public void subtract(Vector v) {
        subtract(v.x, v.y, v.z);
    }

    public void subtract(double x, double y, double z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
    }

    public void add(Vector v) {
        add(v.x, v.y, v.z);
    }

    public void add(double x, double y, double z) {
        this.x += x;
        this.y += y;
        this.z += z;
    }

    public double dot(Vector v) {
        return x * v.x + y * v.y + z * v.z;
    }

    public Vector cross(Vector v) {
        return new Vector(y * v.z - v.y * z, z * v.x - x * v.z, x * v.y - v.x * y);
    }

    public void setCross(Vector v1, Vector v2) {
        x = v1.y * v2.z - v2.y * v1.z;
        y = v1.z * v2.x - v1.x * v2.z;
        z = v1.x * v2.y - v2.x * v1.y;
    }

    public void multiply(double scale) {
        x *= scale;
        y *= scale;
        z *= scale;
    }

    public void divide(double scale) {
        x /= scale;
        y /= scale;
        z /= scale;
    }

    public Vector getProduct(double scale) {
        return new Vector(x * scale, y * scale, z * scale);
    }

    public Vec3d toVec3d() {
        return new Vec3d(x, y, z);
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
        return x * x + y * y + z * z;
    }

    public boolean isZero() {
        return lengthSq() < 1.0E-12D;
    }

    public void zero() {
        x = y = z = 0D;
    }

    public void roundToWhole() {
        x = Math.round(x);
        y = Math.round(y);
        z = Math.round(z);
    }

    @Override
    public String toString() {
        return "<" + x + ", " + y + ", " + z + ">";
    }

    public Vector crossAndUnit(Vector v) {
        Vector crossProduct = cross(v);
        crossProduct.normalize();
        return crossProduct;
    }

    public void writeToByteBuf(ByteBuf toWrite) {
        toWrite.writeDouble(x);
        toWrite.writeDouble(y);
        toWrite.writeDouble(z);
    }

    public void setSubtraction(Vector inLocal, Vector centerCoord) {
        x = inLocal.x - centerCoord.x;
        y = inLocal.y - centerCoord.y;
        z = inLocal.z - centerCoord.z;
    }

    public void transform(double[] rotationMatrix) {
        RotationMatrices.applyTransform(rotationMatrix, this);
    }

    public void setValue(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setValue(Vector toCopy) {
        setValue(toCopy.x, toCopy.y, toCopy.z);
    }

    public void setValue(Vec3d toCopy) {
        setValue(toCopy.x, toCopy.y, toCopy.z);
    }

    /**
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
     */
    public boolean isNaN() {
        return Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z);
    }

    public void setValue(Vector3dc copy) {
        this.x = copy.x();
        this.y = copy.y();
        this.z = copy.z();
    }

    public Vector3d toVector3d() {
        return new Vector3d(x, y, z);
    }
}