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

package valkyrienwarfare.physics.collision;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import valkyrienwarfare.api.RotationMatrices;
import valkyrienwarfare.api.Vector;

import java.util.List;

/**
 * Stores vertices for a polygon, and also has some other operations
 *
 * @author thebest108
 */
public class Polygon {

    public final Vector[] vertices;
    final boolean isAxisAligned;
    public Vector velocity = new Vector(0, 0, 0);

    public Polygon(AxisAlignedBB bb, double[] rotationMatrix) {
        vertices = getCornersForAABB(bb);
        isAxisAligned = false;
        for (int i = 0; i < vertices.length; i++) {
            RotationMatrices.applyTransform(rotationMatrix, vertices[i]);
        }
    }

    public Polygon(Entity entity, double dx, double dy, double dz) {
        this(entity.getEntityBoundingBox());
        velocity = new Vector(dx, dy, dz);
    }

    public Polygon(Vector[] points) {
        vertices = points;
        isAxisAligned = false;
    }

    public Polygon(List<Polygon> polysToMerge) {
        int totalVertices = 0;
        for (Polygon p : polysToMerge) {
            totalVertices += p.vertices.length;
        }
        vertices = new Vector[totalVertices];
        totalVertices = 0;
        for (Polygon p : polysToMerge) {
            for (Vector v : p.vertices) {
                vertices[totalVertices] = v;
                totalVertices++;
            }
        }
        isAxisAligned = false;
    }

    public Polygon(AxisAlignedBB bb) {
        vertices = getCornersForAABB(bb);
        isAxisAligned = true;
    }

    public static Vector[] getCornersForAABB(AxisAlignedBB bb) {
        return new Vector[]{new Vector(bb.minX, bb.minY, bb.minZ), new Vector(bb.minX, bb.maxY, bb.minZ), new Vector(bb.minX, bb.minY, bb.maxZ), new Vector(bb.minX, bb.maxY, bb.maxZ), new Vector(bb.maxX, bb.minY, bb.minZ), new Vector(bb.maxX, bb.maxY, bb.minZ), new Vector(bb.maxX, bb.minY, bb.maxZ), new Vector(bb.maxX, bb.maxY, bb.maxZ)};
    }

    public void setAABBAndMatrix(AxisAlignedBB bb, double[] matrix) {
        setAABBCorners(bb);
        RotationMatrices.applyTransform(matrix, vertices[0]);
        RotationMatrices.applyTransform(matrix, vertices[1]);
        RotationMatrices.applyTransform(matrix, vertices[2]);
        RotationMatrices.applyTransform(matrix, vertices[3]);
        RotationMatrices.applyTransform(matrix, vertices[4]);
        RotationMatrices.applyTransform(matrix, vertices[5]);
        RotationMatrices.applyTransform(matrix, vertices[6]);
        RotationMatrices.applyTransform(matrix, vertices[7]);
    }

    public void setAABBCorners(AxisAlignedBB bb) {
        vertices[0].X = bb.minX;
        vertices[0].Y = bb.minY;
        vertices[0].Z = bb.minZ;
        vertices[1].X = bb.minX;
        vertices[1].Y = bb.maxY;
        vertices[1].Z = bb.minZ;
        vertices[2].X = bb.minX;
        vertices[2].Y = bb.minY;
        vertices[2].Z = bb.maxZ;
        vertices[3].X = bb.minX;
        vertices[3].Y = bb.maxY;
        vertices[3].Z = bb.maxZ;
        vertices[4].X = bb.maxX;
        vertices[4].Y = bb.minY;
        vertices[4].Z = bb.minZ;
        vertices[5].X = bb.maxX;
        vertices[5].Y = bb.maxY;
        vertices[5].Z = bb.minZ;
        vertices[6].X = bb.maxX;
        vertices[6].Y = bb.minY;
        vertices[6].Z = bb.maxZ;
        vertices[7].X = bb.maxX;
        vertices[7].Y = bb.maxY;
        vertices[7].Z = bb.maxZ;
    }

    public void offsetCorners(AxisAlignedBB bb, double x, double y, double z) {
        vertices[0].X = bb.minX + x;
        vertices[0].Y = bb.minY + y;
        vertices[0].Z = bb.minZ + z;
        vertices[1].X = bb.minX + x;
        vertices[1].Y = bb.maxY + y;
        vertices[1].Z = bb.minZ + z;
        vertices[2].X = bb.minX + x;
        vertices[2].Y = bb.minY + y;
        vertices[2].Z = bb.maxZ + z;
        vertices[3].X = bb.minX + x;
        vertices[3].Y = bb.maxY + y;
        vertices[3].Z = bb.maxZ + z;
        vertices[4].X = bb.maxX + x;
        vertices[4].Y = bb.minY + y;
        vertices[4].Z = bb.minZ + z;
        vertices[5].X = bb.maxX + x;
        vertices[5].Y = bb.maxY + y;
        vertices[5].Z = bb.minZ + z;
        vertices[6].X = bb.maxX + x;
        vertices[6].Y = bb.minY + y;
        vertices[6].Z = bb.maxZ + z;
        vertices[7].X = bb.maxX + x;
        vertices[7].Y = bb.maxY + y;
        vertices[7].Z = bb.maxZ + z;
    }

    public void offsetCornersAndTransform(AxisAlignedBB aabb, double x, double y, double z, double[] matrix) {
        offsetCorners(aabb, x, y, z);
        RotationMatrices.applyTransform(matrix, vertices[0]);
        RotationMatrices.applyTransform(matrix, vertices[1]);
        RotationMatrices.applyTransform(matrix, vertices[2]);
        RotationMatrices.applyTransform(matrix, vertices[3]);
        RotationMatrices.applyTransform(matrix, vertices[4]);
        RotationMatrices.applyTransform(matrix, vertices[5]);
        RotationMatrices.applyTransform(matrix, vertices[6]);
        RotationMatrices.applyTransform(matrix, vertices[7]);
    }

    public double[] getProjectionOnVector(Vector axis) {
        double[] distances = new double[vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            distances[i] = axis.dot(vertices[i]);
        }
        return distances;
    }

    public Vector getCenter() {
        Vector center = new Vector(0, 0, 0);
        for (Vector v : vertices) {
            center.add(v);
        }
        center.multiply(1D / vertices.length);
        return center;
    }

    public AxisAlignedBB getEnclosedAABB() {
        Vector c = vertices[0];
        double x = c.X;
        double y = c.Y;
        double z = c.Z;
        double mnX = x;
        double mnY = y;
        double mnZ = z;
        double mxX = x;
        double mxY = y;
        double mxZ = z;
        for (int i = 0; i < vertices.length; i++) {
            c = vertices[i];
            x = c.X;
            y = c.Y;
            z = c.Z;
            if (mnX > x) {
                mnX = x;
            }
            if (mnY > y) {
                mnY = y;
            }
            if (mnZ > z) {
                mnZ = z;
            }
            if (mxX < x) {
                mxX = x;
            }
            if (mxY < y) {
                mxY = y;
            }
            if (mxZ < z) {
                mxZ = z;
            }
        }
        return new AxisAlignedBB(mnX, mnY, mnZ, mxX, mxY, mxZ);
    }

}