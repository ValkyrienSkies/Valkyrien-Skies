package org.valkyrienskies.mod.common.physics.bullet;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btTriangleMesh;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.Value;
import net.minecraft.util.math.BlockPos;
import org.valkyrienskies.mod.common.util.VSIterationUtils;

public class MeshCreator {

    public static btTriangleMesh getMesh(Collection<Triangle> triangles) {
        btTriangleMesh mesh = new btTriangleMesh();
        triangles.forEach(t -> mesh.addTriangle(t.a, t.b, t.c));
        return mesh;
    }

    /**
     * This algorithm is garbage. And I'm not even sure if it works lol
     * Basically, creates two triangles in the mesh for each open face per voxel
     * TODO: Use greedy meshing algorithm https://0fps.net/2012/06/30/meshing-in-a-minecraft-game/
     */
    public static List<Triangle> getMeshTriangles(Collection<BlockPos> blocks2) {
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
                // Basically, this is the point directly in between the two blocks
                // +----------------+----------------+
                // |                |                |
                // |                |                |
                // |                O                |
                // |            This point           |
                // |                |                |
                // |                |                |
                // |                |                |
                // +----------------+----------------+
                Vector3 centerOfFace = new Vector3(
                    ((float) x / 2) + block.getX(),
                    ((float) y / 2) + block.getY(),
                    ((float) z / 2) + block.getZ());

                // Create the two triangles which cover this face of the block
                // +------------------+
                // |               -/ |
                // |             -/   |
                // |           -/     |
                // |         -/       |
                // |       -/         |
                // |     -/           |
                // |   -/             |
                // | -/               |
                // +------------------+
                addTriangles(mesh, centerOfFace);
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
    private static void addTriangles(List<Triangle> toAddTo, Vector3 centerOfFace) {
        if (isDecimal(centerOfFace.y)) {
            Vector3 corner1 = new Vector3(centerOfFace).add(0.5f, 0f, 0.5f);
            Vector3 corner2 = new Vector3(centerOfFace).add(-0.5f, 0f, 0.5f);
            Vector3 corner3 = new Vector3(centerOfFace).add(0.5f, 0f, -0.5f);
            Vector3 corner4 = new Vector3(centerOfFace).add(-0.5f, 0f, -0.5f);

            toAddTo.add(new Triangle(corner1, corner2, corner3));
            toAddTo.add(new Triangle(corner2, corner3, corner4));
        } else if (isDecimal(centerOfFace.x)) {
            Vector3 corner1 = new Vector3(centerOfFace).add(0f, 0.5f, 0.5f);
            Vector3 corner2 = new Vector3(centerOfFace).add(0f, -0.5f, 0.5f);
            Vector3 corner3 = new Vector3(centerOfFace).add(0f, 0.5f, -0.5f);
            Vector3 corner4 = new Vector3(centerOfFace).add(0f, -0.5f, -0.5f);

            toAddTo.add(new Triangle(corner1, corner2, corner3));
            toAddTo.add(new Triangle(corner2, corner3, corner4));
        } else if (isDecimal(centerOfFace.z)) {
            Vector3 corner1 = new Vector3(centerOfFace).add(0.5f, 0.5f, 0f);
            Vector3 corner2 = new Vector3(centerOfFace).add(-0.5f, 0.5f, 0f);
            Vector3 corner3 = new Vector3(centerOfFace).add(0.5f, -0.5f, 0f);
            Vector3 corner4 = new Vector3(centerOfFace).add(-0.5f, -0.5f, 0f);

            toAddTo.add(new Triangle(corner1, corner2, corner3));
            toAddTo.add(new Triangle(corner2, corner3, corner4));
        }
    }

    /**
     * Data class for three points
     */
    @Value
    public static class Triangle {
        Vector3 a, b, c;
    }

    /**
     * @param val The number to check
     * @return True if the number is a decimal, false if it's a whole number
     */
    private static boolean isDecimal(double val) {
        return (val % 1) != val;
    }

}
