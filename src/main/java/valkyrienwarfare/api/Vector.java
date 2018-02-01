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

package valkyrienwarfare.api;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Custom Vector Class used by Valkyrien Warfare
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

    public Vector(double x, double y, double z, double[] rotationMatrix) {
        X = x;
        Y = y;
        Z = z;
        RotationMatrices.applyTransform(rotationMatrix, this);
    }

    public Vector(Vector v) {
        X = v.X;
        Y = v.Y;
        Z = v.Z;
    }

    public Vector(Vector v, double scale) {
        X = v.X * scale;
        Y = v.Y * scale;
        Z = v.Z * scale;
    }

    public Vector(Vec3d positionVector) {
        X = positionVector.x;
        Y = positionVector.y;
        Z = positionVector.z;
    }

    public Vector(Entity entity) {
        X = entity.posX;
        Y = entity.posY;
        Z = entity.posZ;
    }

    public Vector() {
        X = Y = Z = 0D;
    }

    public Vector(ByteBuf toRead) {
        this(toRead.readDouble(), toRead.readDouble(), toRead.readDouble());
    }

    public Vector(Vector theNormal, double[] matrixTransform) {
        this(theNormal.X, theNormal.Y, theNormal.Z, matrixTransform);
    }

    public static Vector[] generateAxisAlignedNorms() {
        Vector[] norms = new Vector[]{new Vector(1.0D, 0.0D, 0.0D), new Vector(0.0D, 1.0D, 0.0D), new Vector(0.0D, 0.0D, 1.0D)};
        return norms;
    }

    public Vector getSubtraction(Vector v) {
        return new Vector(v.X - X, v.Y - Y, v.Z - Z);
    }

    public Vector getAddition(Vector v) {
        return new Vector(v.X + X, v.Y + Y, v.Z + Z);
    }

    public void subtract(Vector v) {
        X -= v.X;
        Y -= v.Y;
        Z -= v.Z;
    }

    public void subtract(double x, double y, double z) {
        X -= x;
        Y -= y;
        Z -= z;
    }

    public void subtract(Vec3d vec) {
        X -= vec.x;
        Y -= vec.y;
        Z -= vec.z;
    }

    public final void add(Vector v) {
        X += v.X;
        Y += v.Y;
        Z += v.Z;
    }

    public final void add(double x, double y, double z) {
        X += x;
        Y += y;
        Z += z;
    }

    public void add(Vec3d vec) {
        X += vec.x;
        Y += vec.y;
        Z += vec.z;
    }

    public double dot(Vector v) {
        return X * v.X + Y * v.Y + Z * v.Z;
    }

    public Vector cross(Vector v) {
        return new Vector(Y * v.Z - v.Y * Z, Z * v.X - X * v.Z, X * v.Y - v.X * Y);
    }

    // v.X and v.Z = 0
    public Vector upCross(Vector v) {
        return new Vector(-v.Y * Z, 0, X * v.Y);
    }

    public final void setCross(Vector v1, Vector v) {
        X = v1.Y * v.Z - v.Y * v1.Z;
        Y = v1.Z * v.X - v1.X * v.Z;
        Z = v1.X * v.Y - v.X * v1.Y;
    }

    public void multiply(double scale) {
        X *= scale;
        Y *= scale;
        Z *= scale;
    }

    public Vector getProduct(double scale) {
        return new Vector(X * scale, Y * scale, Z * scale);
    }

    public Vec3d toVec3d() {
        return new Vec3d(X, Y, Z);
    }

    public void normalize() {
        double d = MathHelper.sqrt(X * X + Y * Y + Z * Z);
        if (d < 1.0E-6D) {
            X = 0.0D;
            Y = 0.0D;
            Z = 0.0D;
        } else {
            X /= d;
            Y /= d;
            Z /= d;
        }
    }

    public double length() {
        return Math.sqrt(X * X + Y * Y + Z * Z);
    }

    public double lengthSq() {
        return X * X + Y * Y + Z * Z;
    }

    public boolean isZero() {
        return (X * X + Y * Y + Z * Z) < 1.0E-12D;
    }

    public void zero() {
        X = Y = Z = 0D;
    }

    public void roundToWhole() {
        X = Math.round(X);
        Y = Math.round(Y);
        Z = Math.round(Z);
    }

    public boolean equals(Vector vec) {
        return (vec.X == X) && (vec.Y == Y) && (vec.Z == Z);
    }

    public String toString() {
        String coords = new String("<" + X + ", " + Y + ", " + Z + ">");
        return coords;
    }

    public String toRoundedString() {
        String coords = new String("<" + Math.round(X * 100.0) / 100.0 + ", " + Math.round(Y * 100.0) / 100.0 + ", " + Math.round(Z * 100.0) / 100.0 + ">");
        return coords;
    }

    public Vector crossAndUnit(Vector v) {
        Vector crossProduct = new Vector(Y * v.Z - v.Y * Z, Z * v.X - X * v.Z, X * v.Y - v.X * Y);
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

}