package org.valkyrienskies.mod.common.util;

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.valkyrienskies.fixes.IPhysicsChunk;
import org.valkyrienskies.mod.common.coordinates.CoordinateSpaceType;
import org.valkyrienskies.mod.common.entity.EntityMountable;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.collision.polygons.Polygon;
import org.valkyrienskies.mod.common.physics.management.PhysicsObject;
import valkyrienwarfare.api.TransformType;

public class ValkyrienUtils {

    /**
     * The liver of this mod. Returns the PhysicsObject that managed the given pos in the given
     * world.
     *
     * @param world The World we are in
     * @param pos   A BlockPos within the physics object space.
     * @return The PhysicsObject that owns the chunk at pos within the given world.
     */
    public static Optional<PhysicsObject> getPhysicsObject(@Nullable World world,
        @Nullable BlockPos pos) {
        return getPhysicsObject(world, pos, false);
    }

    public static Optional<PhysicsObject> getPhysicsObject(@Nullable World world,
        @Nullable BlockPos pos, boolean includePartiallyLoaded) {
        // No physics object manages a null world or a null pos.
        if (world != null && pos != null && world.isBlockLoaded(pos)) {
            IPhysicsChunk physicsChunk = (IPhysicsChunk) world.getChunk(pos);
            Optional<PhysicsObject> physicsObject = physicsChunk.getPhysicsObjectOptional();
            if (physicsObject.isPresent()) {
                if (includePartiallyLoaded || physicsObject.get()
                    .isFullyLoaded()) {
                    return physicsObject;
                }
            }
        }
        return Optional.empty();
    }

    /**
     * If the given AxisAlignedBB is in ship space, then this will return that AxisAlignedBB
     * transformed to global space. Otherwise it just returns the input AxisAlignedBB.
     *
     * @param axisAlignedBB
     * @param world
     * @param pos
     * @return
     */
    public static AxisAlignedBB getAABBInGlobal(@Nonnull AxisAlignedBB axisAlignedBB,
        @Nullable World world, @Nullable BlockPos pos) {
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

    @Nonnull
    public static EntityShipMountData getMountedShipAndPos(@Nonnull Entity entity) {
        Entity ridingEntity = entity.ridingEntity;
        if (ridingEntity instanceof EntityMountable) {
            EntityMountable mountable = (EntityMountable) ridingEntity;
            Optional<PhysicsObject> mountedShip = mountable.getMountedShip();
            if (mountedShip.isPresent()) {
                return new EntityShipMountData(mountedShip.get(), mountable.getMountPos());
            }
        }
        return new EntityShipMountData();
    }

    public static void fixEntityToShip(@Nonnull Entity toFix, @Nonnull Vector posInLocal,
        @Nonnull PhysicsObject mountingShip) {
        World world = mountingShip.getWorld();
        EntityMountable entityMountable = new EntityMountable(world, posInLocal.toVec3d(),
            CoordinateSpaceType.SUBSPACE_COORDINATES, mountingShip.referenceBlockPos());
        world.spawnEntity(entityMountable);
        toFix.startRiding(entityMountable);
    }
}
