package org.valkyrienskies.mod.common.util;

import lombok.experimental.UtilityClass;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.joml.Vector3dc;
import org.valkyrienskies.fixes.IPhysicsChunk;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.capability.VSWorldDataCapability;
import org.valkyrienskies.mod.common.config.VSConfig;
import org.valkyrienskies.mod.common.coordinates.CoordinateSpaceType;
import org.valkyrienskies.mod.common.coordinates.ShipTransform;
import org.valkyrienskies.mod.common.entity.EntityMountable;
import org.valkyrienskies.mod.common.math.VSMath;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.multithreaded.TickSyncCompletableFuture;
import org.valkyrienskies.mod.common.multithreaded.VSExecutors;
import org.valkyrienskies.mod.common.physics.collision.polygons.Polygon;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;
import org.valkyrienskies.mod.common.physics.management.physo.ShipData;
import org.valkyrienskies.mod.common.physmanagement.chunk.ShipChunkAllocator;
import org.valkyrienskies.mod.common.physmanagement.chunk.VSChunkClaim;
import org.valkyrienskies.mod.common.physmanagement.relocation.DetectorManager;
import org.valkyrienskies.mod.common.physmanagement.shipdata.QueryableShipData;
import org.valkyrienskies.mod.common.util.names.NounListNameGenerator;
import valkyrienwarfare.api.TransformType;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.UUID;

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
                    .getShipTransformationManager()
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
        World world = mountingShip.getWorld();
        EntityMountable entityMountable = new EntityMountable(world, posInLocal.toVec3d(),
                CoordinateSpaceType.SUBSPACE_COORDINATES, mountingShip.getReferenceBlockPos());
        world.spawnEntity(entityMountable);
        toFix.startRiding(entityMountable);
    }

    private static VSWorldDataCapability getWorldDataCapability(World world) {
        VSWorldDataCapability worldData = world
                .getCapability(ValkyrienSkiesMod.VS_WOR_DATA, null);
        if (worldData == null) {
            // I hate it when other mods add their custom worlds without calling the forge world
            // load events, so I don't feel bad crashing the game here. Although we could also get
            // away with just adding the capability to world instead of crashing.
            throw new IllegalStateException(
                    "World " + world + " doesn't have an VSWorldDataCapability. This is wrong!");
        }

        return worldData;
    }

    /**
     * This method basically grabs the {@link VSWorldDataCapability} capability from the world
     * and then returns the {@link QueryableShipData} associated with it
     *
     * @param world The world we are getting the QueryableShipData from
     * @return The QueryableShipData corresponding to the given world
     */
    public static QueryableShipData getQueryableData(World world) {
        return getWorldDataCapability(world).get().getQueryableShipData();
    }

    /**
     * This method basically grabs the {@link VSWorldDataCapability} capability from the world
     * and then returns the {@link ShipChunkAllocator} associated with it
     *
     * @param world The world we are getting the QueryableShipData from
     * @return The QueryableShipData corresponding to the given world
     */
    public static ShipChunkAllocator getShipChunkAllocator(World world) {
        return getWorldDataCapability(world).get().getShipChunkAllocator();
    }

    /**
     * Creates a new ShipIndexedData based on the inputs provided by the physics infuser block.
     */
    public static ShipData createNewShip(World world, BlockPos physInfuserPos) {
        String name = NounListNameGenerator.instance().generateName();
        UUID shipID = UUID.randomUUID();
        // Create ship chunk claims
        VSChunkClaim chunkClaim = ValkyrienUtils.getShipChunkAllocator(world).allocateNextChunkClaim();
        Vector3dc centerOfMassInitial = VSMath.toVector3d(chunkClaim.getRegionCenter());
        Vector3dc shipPosInitial = VSMath.toVector3d(physInfuserPos).add(0.5, 0.5, 0.5);
        ShipTransform initial = new ShipTransform(shipPosInitial, centerOfMassInitial);
        AxisAlignedBB axisAlignedBB = new AxisAlignedBB(shipPosInitial.x(), shipPosInitial.y(),
            shipPosInitial.z(), shipPosInitial.x(), shipPosInitial.y(), shipPosInitial.z());
        ShipData data = ShipData.createData(QueryableShipData.get(world).getAllShips(),
            name, chunkClaim, shipID, initial, axisAlignedBB, physInfuserPos);
        return data;
    }

    public static TickSyncCompletableFuture<Void> assembleShipAsOrderedByPlayer(World world, EntityPlayerMP creator, BlockPos physicsInfuserPos) {
        if (world.isRemote) {
            throw new IllegalStateException("This method cannot be invoked on client side!");
        }
        if (!(world instanceof WorldServer)) {
            throw new IllegalStateException(
                    "The world " + world + " wasn't an instance of WorldServer");
        }

        // Create the ship data that we will use to make the ship with later.
        ShipData shipData = createNewShip(world, physicsInfuserPos);
        BlockPos centerInWorld = physicsInfuserPos;

        System.out.println("E!");
        return TickSyncCompletableFuture
                .supplyAsync(
                        () -> DetectorManager.getDetectorFor(DetectorManager.DetectorIDs.ShipSpawnerGeneral, centerInWorld, world,
                                VSConfig.maxShipSize + 1, true))
                .thenAcceptAsync(detector -> {
                    System.out.println("Hello! " + Thread.currentThread().getName());
                    if (detector.foundSet.size() > VSConfig.maxShipSize || detector.cleanHouse) {
                        System.err.println("Ship too big or bedrock detected!");
                        if (creator != null) {
                            creator.sendMessage(new TextComponentString(
                                    "Ship construction canceled because its exceeding the ship size limit; "
                                            +
                                            "or because it's attached to bedrock. " +
                                            "Raise it with /physsettings maxshipsize [number]"));
                        }
                        return;
                    }

                    QueryableShipData.get(world).addShip(shipData);
                    PhysicsObject physicsObject = new PhysicsObject(world, shipData.getUuid(), true);
                    shipData.setPhyso(physicsObject);

                    physicsObject.assembleShip(creator, detector, physicsInfuserPos);

                    int i = 1;
                    // TODO: Do something with this?
                }, VSExecutors.forWorld((WorldServer) world));
    }
}
