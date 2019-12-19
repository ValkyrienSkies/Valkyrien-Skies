package org.valkyrienskies.mod.common.physics.bullet;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btTriangleMesh;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.Value;
import net.minecraft.util.math.BlockPos;
import org.joml.Matrix4dc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.mod.common.util.JOML;
import org.valkyrienskies.mod.common.util.VSIterationUtils;

public class MeshCreator {

    public static btTriangleMesh getMesh(Collection<Triangle> triangles) {
        btTriangleMesh mesh = new btTriangleMesh();
        triangles.forEach(t -> mesh.addTriangle(t.a, t.b, t.c, true));
        return mesh;
    }

    /**
     * This algorithm is garbage. And I'm not even sure if it works lol
     * Basically, creates two triangles in the mesh for each open face per voxel
     * TODO: Use greedy meshing algorithm https://0fps.net/2012/06/30/meshing-in-a-minecraft-game/
     */
    public static List<Triangle> getMeshTriangles(Collection<BlockPos> blocks2, Vector3 offset) {
        List<Triangle> mesh = new ArrayList<>();
        // Create a set copy of the block collection, which we'll need because we're going to lookup
        // a lot of block positions to check if they're adjacent
        ImmutableSet<BlockPos> blocks = ImmutableSet.copyOf(blocks2);

        // For every block in the list of blocks
        blocks.forEach(block ->
            // Iterate over each adjacent block
            // (x, y, z) is the offset in this case, since the origin is (0, 0, 0)
            VSIterationUtils.iterateAdjacent3d(0, 0, 0, (x, y, z) -> {
                // Calculate the adjacent block position
                BlockPos adjacent = block.add(x, y, z);
                // If the adjacent block is present in the list of blocks, then we don't want to
                // add mesh triangles because they would be covered up
                if (blocks.contains(adjacent)) return;

                // Get the face of this block facing the adjacent block (which is air)
                // Basically, this is the point directly in between the two blocks, in the center of
                // the block's face
                // +----------------+----------------+
                // |                |                |
                // |     Block      |      Air       |
                // |                O                |
                // |            This point           |
                // |                |                |
                // |                |                |
                // |                |                |
                // +----------------+----------------+
                Vector3 centerOfFace = new Vector3(
                    ((float) x / 2) + block.getX() + 0.5f,
                    ((float) y / 2) + block.getY() + 0.5f,
                    ((float) z / 2) + block.getZ() + 0.5f);

                // Create the two triangles which cover this face of the block
                // +------------------+
                // |               -/ |
                // |             -/   |
                // |           -/     |
                // |       Block      |
                // |       -/         |
                // |     -/           |
                // |   -/             |
                // | -/               |
                // +------------------+
                addTriangles(mesh, centerOfFace, offset);
            })
        );

        return mesh;
    }

    /**
     * Adds triangles to the mesh
     * @param toAddTo the list to add to
     * @param centerOfFace The center of the face of the block/voxel that is faces the air
     *                     (one value here should be mod 0.5)
     */
    private static void addTriangles(List<Triangle> toAddTo, Vector3 centerOfFace, Vector3 offset) {
        Vector3 corner1, corner2, corner3, corner4;

        // Here we generate the corners for the plane that we're operating on
        if (isInteger(centerOfFace.y)) {
            // The Y coordinate is an integer, so its the face that we're operating on
            corner1 = new Vector3(centerOfFace).add(0.5f, 0f, 0.5f).add(offset);
            corner2 = new Vector3(centerOfFace).add(-0.5f, 0f, 0.5f).add(offset);
            corner3 = new Vector3(centerOfFace).add(0.5f, 0f, -0.5f).add(offset);
            corner4 = new Vector3(centerOfFace).add(-0.5f, 0f, -0.5f).add(offset);
        } else if (isInteger(centerOfFace.x)) {
            // The X coordinate is an integer, so its the face that we're operating on
            corner1 = new Vector3(centerOfFace).add(0f, 0.5f, 0.5f).add(offset);
            corner2 = new Vector3(centerOfFace).add(0f, -0.5f, 0.5f).add(offset);
            corner3 = new Vector3(centerOfFace).add(0f, 0.5f, -0.5f).add(offset);
            corner4 = new Vector3(centerOfFace).add(0f, -0.5f, -0.5f).add(offset);
        } else if (isInteger(centerOfFace.z)) {
            // The Z coordinate is an integer, so its the face that we're operating on
            corner1 = new Vector3(centerOfFace).add(0.5f, 0.5f, 0f).add(offset);
            corner2 = new Vector3(centerOfFace).add(-0.5f, 0.5f, 0f).add(offset);
            corner3 = new Vector3(centerOfFace).add(0.5f, -0.5f, 0f).add(offset);
            corner4 = new Vector3(centerOfFace).add(-0.5f, -0.5f, 0f).add(offset);
        } else {
            throw new IllegalArgumentException("There was no integer coordinate, this isn't right");
        }

        toAddTo.add(new Triangle(corner1, corner2, corner3));
        toAddTo.add(new Triangle(corner2, corner3, corner4));
    }

    /**
     * Data class for three points
     */
    @Value
    public static class Triangle {
        Vector3 a, b, c;

        public Triangle transformPosition(Matrix4dc transform) {
            Vector3d a = JOML.convertDouble(this.a);
            Vector3d b = JOML.convertDouble(this.b);
            Vector3d c = JOML.convertDouble(this.c);

            transform.transformPosition(a);
            transform.transformPosition(b);
            transform.transformPosition(c);

            return new Triangle(JOML.toGDX(a), JOML.toGDX(b), JOML.toGDX(c));
        }
    }

    /**
     * @param val The number to check
     * @return True if the number is an integer, false if it's a decimal
     */
    private static boolean isInteger(double val) {
        return (val % 1) == 0;
    }

}
