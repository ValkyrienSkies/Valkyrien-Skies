package org.valkyrienskies.mod.common.util;

import lombok.experimental.UtilityClass;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import org.joml.Matrix4dc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.mod.common.capability.VSCapabilityRegistry;
import org.valkyrienskies.mod.common.capability.VSWorldDataCapability;
import org.valkyrienskies.mod.common.collision.Polygon;
import org.valkyrienskies.mod.common.entity.EntityMountable;
import org.valkyrienskies.mod.common.ships.QueryableShipData;
import org.valkyrienskies.mod.common.ships.ShipData;
import org.valkyrienskies.mod.common.ships.chunk_claims.ShipChunkAllocator;
import org.valkyrienskies.mod.common.ships.chunk_claims.VSChunkClaim;
import org.valkyrienskies.mod.common.ships.entity_interaction.EntityShipMountData;
import org.valkyrienskies.mod.common.ships.ship_transform.CoordinateSpaceType;
import org.valkyrienskies.mod.common.ships.ship_transform.ShipTransform;
import org.valkyrienskies.mod.common.ships.ship_world.IHasShipManager;
import org.valkyrienskies.mod.common.ships.ship_world.IPhysObjectWorld;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;
import org.valkyrienskies.mod.common.ships.ship_world.WorldServerShipManager;
import org.valkyrienskies.mod.common.util.multithreaded.CalledFromWrongThreadException;
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
    public static Optional<PhysicsObject> getPhysoManagingBlock(@Nullable World world, @Nullable BlockPos pos)
            throws CalledFromWrongThreadException {
        if (world == null || pos == null) {
            return Optional.empty();
        }
        if (!ShipChunkAllocator.isChunkInShipyard(pos.getX() >> 4, pos.getZ() >> 4)) {
            return Optional.empty();
        }
        QueryableShipData queryableShipData = QueryableShipData.get(world);
        Optional<ShipData> shipData = queryableShipData.getShipFromChunk(pos.getX() >> 4, pos.getZ() >> 4);
        if (shipData.isPresent()) {
            PhysicsObject object = getPhysObjWorld(world).getPhysObjectFromUUID(shipData.get().getUuid());
            if (object != null) {
                return Optional.of(object);
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
        Optional<PhysicsObject> physicsObject = ValkyrienUtils.getPhysoManagingBlock(world, pos);
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

    public static void fixEntityToShip(Entity toFix, Vector3dc posInLocal,
                                       PhysicsObject mountingShip) {
        World world = mountingShip.getWorld();
        EntityMountable entityMountable = new EntityMountable(world, JOML.toMinecraft(posInLocal),
                CoordinateSpaceType.SUBSPACE_COORDINATES, mountingShip.getReferenceBlockPos());
        world.spawnEntity(entityMountable);
        toFix.startRiding(entityMountable);
    }

    private static VSWorldDataCapability getWorldDataCapability(World world) {
        VSWorldDataCapability worldData = world
                .getCapability(VSCapabilityRegistry.VS_WORLD_DATA, null);
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
        String name = NounListNameGenerator.getInstance().generateName();
        UUID shipID = UUID.randomUUID();
        // Create ship chunk claims
        VSChunkClaim chunkClaim = ValkyrienUtils.getShipChunkAllocator(world).allocateNextChunkClaim();
        Vector3dc centerOfMassInitial = VSMath.toVector3d(chunkClaim.getRegionCenter());
        Vector3dc shipPosInitial = VSMath.toVector3d(physInfuserPos);
        ShipTransform initial = new ShipTransform(shipPosInitial, centerOfMassInitial);
        AxisAlignedBB axisAlignedBB = new AxisAlignedBB(shipPosInitial.x(), shipPosInitial.y(),
            shipPosInitial.z(), shipPosInitial.x(), shipPosInitial.y(), shipPosInitial.z());
        return ShipData.createData(QueryableShipData.get(world).getAllShips(),
            name, chunkClaim, shipID, initial, axisAlignedBB, physInfuserPos);
    }

    public static Iterable<PhysicsObject> getPhysosLoadedInWorld(World world) {
        return ((IHasShipManager) world).getManager().getAllLoadedPhysObj();
    }

    public static void assembleShipAsOrderedByPlayer(World world,
        @Nullable EntityPlayerMP creator, BlockPos physicsInfuserPos) {
        if (world.isRemote) {
            throw new IllegalStateException("This method cannot be invoked on client side!");
        }
        if (!(world instanceof WorldServer)) {
            throw new IllegalStateException(
                    "The world " + world + " wasn't an instance of WorldServer");
        }

        // Create the ship data that we will use to make the ship with later.
        ShipData shipData = createNewShip(world, physicsInfuserPos);

        // Queue the ship spawn operation
        ((WorldServerShipManager) ValkyrienUtils.getPhysObjWorld(world)).queueShipSpawn(shipData);
    }

    public static IPhysObjectWorld getPhysObjWorld(World world) {
        return ((IHasShipManager) world).getManager();
    }

    /**
     * Applies the given transform matrix to the position/velocity/look of the given entity.
     * @param transform The transform matrix to be applied.
     * @param entity The entity that will be transformed.
     */
    public static void transformEntity(Matrix4dc transform, Entity entity) {
        Vec3d entityLookMc = entity.getLook(1.0F);

        Vector3d entityPos = new Vector3d(entity.posX, entity.posY, entity.posZ);
        Vector3d entityLook = new Vector3d(entityLookMc.x, entityLookMc.y, entityLookMc.z);
        Vector3d entityMotion = new Vector3d(entity.motionX, entity.motionY, entity.motionZ);

        if (entity instanceof EntityFireball) {
            EntityFireball ball = (EntityFireball) entity;
            entityMotion.x = ball.accelerationX;
            entityMotion.y = ball.accelerationY;
            entityMotion.z = ball.accelerationZ;
        }

        transform.transformPosition(entityPos);
        transform.transformDirection(entityLook);
        transform.transformDirection(entityMotion);

        entityLook.normalize();

        // This is correct, works properly when tested with cows
        if (entity instanceof EntityLiving) {
            EntityLiving living = (EntityLiving) entity;
            living.rotationYawHead = entity.rotationYaw;
            living.prevRotationYawHead = entity.rotationYaw;
        }

        // ===== Fix change the entity rotation to be proper relative to ship space =====
        Vector3dc entityLookImmutable = new Vector3d(entityLook.x, entityLook.y, entityLook.z);
        double pitch = VSMath.getPitchFromVector(entityLookImmutable);
        double yaw = VSMath.getYawFromVector(entityLookImmutable, pitch);

        entity.rotationYaw = (float) yaw;
        entity.rotationPitch = (float) pitch;

        if (entity instanceof EntityFireball) {
            EntityFireball ball = (EntityFireball) entity;
            ball.accelerationX = entityMotion.x;
            ball.accelerationY = entityMotion.y;
            ball.accelerationZ = entityMotion.z;
        }

        entity.motionX = entityMotion.x;
        entity.motionY = entityMotion.y;
        entity.motionZ = entityMotion.z;

        entity.setPosition(entityPos.x, entityPos.y, entityPos.z);
    }

    /**
     * Used for bad code that tries reading tile entities from a non game thread. But hey it works (mostly).
     */
    @Nullable
    public static TileEntity getTileEntitySafe(World world, BlockPos pos) {
        return world.getChunk(pos).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
    }

}
