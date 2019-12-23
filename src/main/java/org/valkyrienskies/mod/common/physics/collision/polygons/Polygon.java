package org.valkyrienskies.mod.common.physics.collision.polygons;

import net.minecraft.util.math.AxisAlignedBB;
import org.valkyrienskies.mod.common.coordinates.ShipTransform;
import org.valkyrienskies.mod.common.math.Vector;
import valkyrienwarfare.api.TransformType;

/**
 * The basis for the entire collision engine, this implementation of Polygon stores normals as well
 * as vertices and supports transformations, creating AABB, and checking for collision with other
 * Polygon objects. The polygon can theoretically support an arbitrary amount of vertices and
 * normals, but typically eight vertices and three normals are used. Only supports convex polygons.
 *
 * @author thebest108
 */
public class Polygon {

    private final Vector[] vertices;
    private final Vector[] normals;

    public Polygon(AxisAlignedBB bb) {
        this.vertices = getCornersForAABB(bb);
        this.normals = Vector.generateAxisAlignedNorms();
    }

    public Polygon(AxisAlignedBB bb, ShipTransform transformation, TransformType transformType) {
        this(bb);
        transform(transformation, transformType);
    }

    // Copies one polygon onto another.
    protected Polygon(Polygon other) {
        this.vertices = new Vector[other.vertices.length];
        this.normals = other.normals;
    }

    private static Vector[] getCornersForAABB(AxisAlignedBB bb) {
        return new Vector[]{new Vector(bb.minX, bb.minY, bb.minZ),
            new Vector(bb.minX, bb.maxY, bb.minZ),
            new Vector(bb.minX, bb.minY, bb.maxZ), new Vector(bb.minX, bb.maxY, bb.maxZ),
            new Vector(bb.maxX, bb.minY, bb.minZ), new Vector(bb.maxX, bb.maxY, bb.minZ),
            new Vector(bb.maxX, bb.minY, bb.maxZ), new Vector(bb.maxX, bb.maxY, bb.maxZ)};
    }

    public Vector[] getVertices() {
        return vertices;
    }

    public Vector[] getNormals() {
        return normals;
    }

    public double[] getProjectionOnVector(Vector axis) {
        double[] distances = new double[vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            distances[i] = axis.dot(vertices[i]);
        }
        return distances;
    }

    public Vector getCenter() {
        Vector center = new Vector();
        for (Vector v : vertices) {
            center.add(v);
        }
        center.divide(vertices.length);
        return center;
    }

    public void transform(ShipTransform transformation, TransformType transformType) {
        for (Vector vertex : vertices) {
            transformation.transform(vertex, transformType);
        }
        for (Vector normal : normals) {
            transformation.rotate(normal, transformType);
        }
    }

    public AxisAlignedBB getEnclosedAABB() {
        Vector firstVertex = vertices[0];
        double mnX = firstVertex.x;
        double mnY = firstVertex.y;
        double mnZ = firstVertex.z;
        double mxX = firstVertex.x;
        double mxY = firstVertex.y;
        double mxZ = firstVertex.z;
        for (int i = 1; i < vertices.length; i++) {
            Vector vertex = vertices[i];
            mnX = Math.min(mnX, vertex.x);
            mnY = Math.min(mnY, vertex.y);
            mnZ = Math.min(mnZ, vertex.z);
            mxX = Math.max(mxX, vertex.x);
            mxY = Math.max(mxY, vertex.y);
            mxZ = Math.max(mxZ, vertex.z);
        }
        return new AxisAlignedBB(mnX, mnY, mnZ, mxX, mxY, mxZ);
    }

}