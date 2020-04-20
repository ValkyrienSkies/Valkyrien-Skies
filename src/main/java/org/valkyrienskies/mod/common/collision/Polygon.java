package org.valkyrienskies.mod.common.collision;

import lombok.Getter;
import net.minecraft.util.math.AxisAlignedBB;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.mod.common.ships.ship_transform.ShipTransform;
import valkyrienwarfare.api.TransformType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The basis for the entire collision engine, this implementation of Polygon stores normals as well
 * as vertices and supports transformations, creating AABB, and checking for collision with other
 * Polygon objects. The polygon can theoretically support an arbitrary amount of vertices and
 * normals, but typically eight vertices and three normals are used. Only supports convex polygons.
 *
 * @author thebest108
 */
public class Polygon {

    @Getter
    private final Vector3dc[] vertices;
    @Getter
    private final Vector3dc[] normals;

    public Polygon(@Nonnull AxisAlignedBB bb, @Nullable ShipTransform transformation, @Nullable TransformType transformType) {
        Vector3d[] verticesMutable = getCornersForAABB(bb);
        Vector3d[] normalsMutable = generateAxisAlignedNorms();
        if (transformation != null && transformType != null) {
            transform(verticesMutable, normalsMutable, transformation, transformType);
        }
        this.vertices = verticesMutable;
        this.normals = normalsMutable;
    }

    public Polygon(@Nonnull AxisAlignedBB bb) {
        this(bb, null, null);
    }

    public static Vector3d[] generateAxisAlignedNorms() {
        return new Vector3d[]{
                new Vector3d(1.0D, 0.0D, 0.0D),
                new Vector3d(0.0D, 1.0D, 0.0D),
                new Vector3d(0.0D, 0.0D, 1.0D)
        };
    }

    private static Vector3d[] getCornersForAABB(AxisAlignedBB bb) {
        return new Vector3d[]{
                new Vector3d(bb.minX, bb.minY, bb.minZ),
                new Vector3d(bb.minX, bb.maxY, bb.minZ),
                new Vector3d(bb.minX, bb.minY, bb.maxZ),
                new Vector3d(bb.minX, bb.maxY, bb.maxZ),
                new Vector3d(bb.maxX, bb.minY, bb.minZ),
                new Vector3d(bb.maxX, bb.maxY, bb.minZ),
                new Vector3d(bb.maxX, bb.minY, bb.maxZ),
                new Vector3d(bb.maxX, bb.maxY, bb.maxZ)
        };
    }

    public double[] getProjectionOnVector(Vector3dc axis) {
        double[] distances = new double[vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            distances[i] = axis.dot(vertices[i]);
        }
        return distances;
    }

    public Vector3d getCenter() {
        Vector3d center = new Vector3d();
        for (Vector3dc v : vertices) {
            center.add(v);
        }
        center.mul(1.0 / vertices.length);
        return center;
    }

    private static void transform(Vector3d[] vertices, Vector3d[] normals, ShipTransform transformation, TransformType transformType) {
        for (Vector3d vertex : vertices) {
            transformation.transformPosition(vertex, transformType);
        }
        for (Vector3d normal : normals) {
            transformation.transformDirection(normal, transformType);
        }
    }

    public AxisAlignedBB getEnclosedAABB() {
        Vector3dc firstVertex = vertices[0];
        double mnX = firstVertex.x();
        double mnY = firstVertex.y();
        double mnZ = firstVertex.z();
        double mxX = firstVertex.x();
        double mxY = firstVertex.y();
        double mxZ = firstVertex.z();
        for (int i = 1; i < vertices.length; i++) {
            Vector3dc vertex = vertices[i];
            mnX = Math.min(mnX, vertex.x());
            mnY = Math.min(mnY, vertex.y());
            mnZ = Math.min(mnZ, vertex.z());
            mxX = Math.max(mxX, vertex.x());
            mxY = Math.max(mxY, vertex.y());
            mxZ = Math.max(mxZ, vertex.z());
        }
        return new AxisAlignedBB(mnX, mnY, mnZ, mxX, mxY, mxZ);
    }

}