package valkyrienwarfare.mod.common.util;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import valkyrienwarfare.api.TransformType;
import valkyrienwarfare.fixes.IPhysicsChunk;
import valkyrienwarfare.mod.common.physics.collision.polygons.Polygon;
import valkyrienwarfare.mod.common.physics.management.PhysicsObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class ValkyrienUtils {

    /**
     * The liver of this mod. Returns the PhysicsObject that managed the given pos in the given world.
     *
     * @param world
     * @param pos
     * @return
     */
    public static Optional<PhysicsObject> getPhysicsObject(@Nullable World world, @Nullable BlockPos pos) {
        // No physics object manages a null world or a null pos.
        if (world != null && pos != null && world.isBlockLoaded(pos)) {
            IPhysicsChunk physicsChunk = (IPhysicsChunk) world.getChunk(pos);
            Optional<PhysicsObject> physicsObject = physicsChunk.getPhysicsObjectOptional();
            if (physicsObject.isPresent()) {
                if (physicsObject.get()
                        .getShipTransformationManager() != null) {
                    return physicsObject;
                } else {
                    new IllegalStateException("Tried accessing ship at " + pos + " but it wasn't fully initialized").printStackTrace();
                }
            }
        }
        return Optional.empty();
    }

    /**
     * If the given AxisAlignedBB is in ship space, then this will return that AxisAlignedBB transformed to global space. Otherwise it just returns the input AxisAlignedBB.
     *
     * @param axisAlignedBB
     * @param world
     * @param pos
     * @return
     */
    public static AxisAlignedBB getAABBInGlobal(@Nonnull AxisAlignedBB axisAlignedBB, @Nullable World world, @Nullable BlockPos pos) {
        Optional<PhysicsObject> physicsObject = ValkyrienUtils.getPhysicsObject(world, pos);
        if (physicsObject.isPresent()) {
            // We're in a physics object; convert the bounding box to a polygon; put its coordinates in global space, and then return the bounding box that encloses
            // all the points.
            Polygon bbAsPoly = new Polygon(axisAlignedBB, physicsObject.get()
                    .getShipTransformationManager()
                    .getCurrentTickTransform(), TransformType.SUBSPACE_TO_GLOBAL);
            return bbAsPoly.getEnclosedAABB();
        } else {
            return axisAlignedBB;
        }
    }
}
