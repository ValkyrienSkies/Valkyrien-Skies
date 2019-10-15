package org.valkyrienskies.mod.common.util;

import java.util.Optional;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import lombok.experimental.UtilityClass;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.valkyrienskies.fixes.IPhysicsChunk;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.coordinates.CoordinateSpaceType;
import org.valkyrienskies.mod.common.entity.EntityMountable;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.collision.polygons.Polygon;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;
import org.valkyrienskies.mod.common.physmanagement.shipdata.IValkyrienSkiesWorldData;
import org.valkyrienskies.mod.common.physmanagement.shipdata.QueryableShipData;
import valkyrienwarfare.api.TransformType;

/**
 * This class contains various helper functions for Valkyrien Skies.
 */
@UtilityClass
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
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
     */
    public static AxisAlignedBB getAABBInGlobal(AxisAlignedBB axisAlignedBB,
        @Nullable World world, @Nullable BlockPos pos) {
        Optional<PhysicsObject> physicsObject = ValkyrienUtils.getPhysicsObject(world, pos);
        if (physicsObject.isPresent()) {
            // We're in a physics object; convert the bounding box to a polygon; put its coordinates
            // in global space, and then return the bounding box that encloses all the points.
            Polygon bbAsPoly = new Polygon(axisAlignedBB, physicsObject.get()
                .shipTransformationManager()
                .getCurrentTickTransform(), TransformType.SUBSPACE_TO_GLOBAL);
            return bbAsPoly.getEnclosedAABB();
        } else {
            return axisAlignedBB;
        }
    }

    public static EntityShipMountData getMountedShipAndPos(Entity entity) {
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

    public static void fixEntityToShip(Entity toFix, Vector posInLocal,
        PhysicsObject mountingShip) {
        World world = mountingShip.world();
        EntityMountable entityMountable = new EntityMountable(world, posInLocal.toVec3d(),
            CoordinateSpaceType.SUBSPACE_COORDINATES, mountingShip.referenceBlockPos());
        world.spawnEntity(entityMountable);
        toFix.startRiding(entityMountable);
    }

    /**
     * This method basically grabs the {@link IValkyrienSkiesWorldData} capability from the world
     * and then returns the QueryableShipData associated with it
     *
     * @param world The world we are getting the QueryableShipData from
     * @return The QueryableShipData corresponding to the given world
     */
    public static QueryableShipData getQueryableData(World world) {
        IValkyrienSkiesWorldData worldData = world
            .getCapability(ValkyrienSkiesMod.VS_WORLD_DATA, null);
        if (worldData == null) {
            // I hate it when other mods add their custom worlds without calling the forge world
            // load events, so I don't feel bad crashing the game here. Although we could also get
            // away with just adding the capability to world instead of crashing.
            throw new IllegalStateException(
                "World " + world + " doesn't have an IVSWorldDataCapability. This is wrong!");
        }
        return worldData.getQueryableShipData();
    }
}
