package org.valkyrienskies.mod.common.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Tuple;
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
import org.valkyrienskies.mod.common.entity.EntityShipMovementData;
import org.valkyrienskies.mod.common.ships.QueryableShipData;
import org.valkyrienskies.mod.common.ships.ShipData;
import org.valkyrienskies.mod.common.ships.block_relocation.BlockFinder;
import org.valkyrienskies.mod.common.ships.chunk_claims.ShipChunkAllocator;
import org.valkyrienskies.mod.common.ships.chunk_claims.VSChunkClaim;
import org.valkyrienskies.mod.common.ships.entity_interaction.EntityShipMountData;
import org.valkyrienskies.mod.common.ships.entity_interaction.IDraggable;
import org.valkyrienskies.mod.common.ships.ship_transform.CoordinateSpaceType;
import org.valkyrienskies.mod.common.ships.ship_transform.ShipTransform;
import org.valkyrienskies.mod.common.ships.ship_world.IHasShipManager;
import org.valkyrienskies.mod.common.ships.ship_world.IPhysObjectWorld;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;
import org.valkyrienskies.mod.common.ships.ship_world.WorldServerShipManager;
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
    @SuppressWarnings("ConstantConditions")
    public Optional<PhysicsObject> getPhysoManagingBlock(@Nullable World world, @Nullable BlockPos pos) {
        return getShipManagingBlock(world, pos)
            .map(shipData -> getPhysObjWorld(world).getPhysObjectFromUUID(shipData.getUuid()));
    }

    public Optional<PhysicsObject> getPhysoManagingBlockThreadSafe(@Nullable World world, @Nullable BlockPos pos) {
        for (PhysicsObject physicsObject : getPhysObjWorld(world).getAllLoadedThreadSafe()) {
            if (physicsObject.getChunkClaim().containsBlock(pos)) {
                return Optional.of(physicsObject);
            }
        }
        return Optional.empty();
    }

    public Optional<ShipData> getShipManagingBlock(@Nullable World world, @Nullable BlockPos pos) {
        if (world == null ||
            pos == null ||
            !ShipChunkAllocator.isChunkInShipyard(pos.getX() >> 4, pos.getZ() >> 4)) {
            return Optional.empty();
        }

        return QueryableShipData.get(world)
            .getShipFromChunk(pos.getX() >> 4, pos.getZ() >> 4);
    }

    /**
     * If the given AxisAlignedBB is in ship space, then this will return that AxisAlignedBB
     * transformed to global space. Otherwise it just returns the input AxisAlignedBB.
     */
    public AxisAlignedBB getAABBInGlobal(AxisAlignedBB axisAlignedBB,
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

    public EntityShipMountData getMountedShipAndPos(Entity entity) {
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

    public void fixEntityToShip(Entity toFix, Vector3dc posInLocal,
                                       PhysicsObject mountingShip) {
        World world = mountingShip.getWorld();
        EntityMountable entityMountable = new EntityMountable(world, JOML.toMinecraft(posInLocal),
                CoordinateSpaceType.SUBSPACE_COORDINATES, mountingShip.getReferenceBlockPos());
        world.spawnEntity(entityMountable);
        toFix.startRiding(entityMountable);
    }

    private VSWorldDataCapability getWorldDataCapability(World world) {
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
    public QueryableShipData getQueryableData(World world) {
        return getWorldDataCapability(world).get().getQueryableShipData();
    }

    /**
     * This method basically grabs the {@link VSWorldDataCapability} capability from the world
     * and then returns the {@link ShipChunkAllocator} associated with it
     *
     * @param world The world we are getting the QueryableShipData from
     * @return The QueryableShipData corresponding to the given world
     */
    public ShipChunkAllocator getShipChunkAllocator(World world) {
        return getWorldDataCapability(world).get().getShipChunkAllocator();
    }

    public WorldServerShipManager getServerShipManager(World world) {
        return (WorldServerShipManager) ((IHasShipManager) world).getManager();
    }

    /**
     * Creates a new ShipIndexedData based on the inputs provided by the physics infuser block.
     */
    public ShipData createNewShip(World world, BlockPos physInfuserPos) {
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
            name, chunkClaim, shipID, initial, axisAlignedBB);
    }

    public Iterable<PhysicsObject> getPhysosLoadedInWorld(World world) {
        return ((IHasShipManager) world).getManager().getAllLoadedPhysObj();
    }

    public void assembleShipAsOrderedByPlayer(World world, @Nullable EntityPlayerMP creator,
                                              BlockPos physicsInfuserPos, BlockFinder.BlockFinderType blockFinderType) {
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
        ((WorldServerShipManager) ValkyrienUtils.getPhysObjWorld(world)).queueShipSpawn(shipData, physicsInfuserPos, blockFinderType);
    }

    public IPhysObjectWorld getPhysObjWorld(World world) {
        return ((IHasShipManager) world).getManager();
    }

    /**
     * Applies the given transform matrix to the position/velocity/look of the given entity.
     * @param transform The transform matrix to be applied.
     * @param entity The entity that will be transformed.
     */
    public void transformEntity(final Matrix4dc transform, final Entity entity, final boolean transformEntityBoundingBox) {
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

        // Get the player pitch/yaw from the look vector
        final Tuple<Double, Double> pitchYawTuple = VSMath.getPitchYawFromVector(new Vector3d(entityLook.x, entityLook.y, entityLook.z));
        entity.rotationPitch = pitchYawTuple.getFirst().floatValue();
        entity.rotationYaw = pitchYawTuple.getSecond().floatValue();

        if (entity instanceof EntityFireball) {
            EntityFireball ball = (EntityFireball) entity;
            ball.accelerationX = entityMotion.x;
            ball.accelerationY = entityMotion.y;
            ball.accelerationZ = entityMotion.z;
        }

        entity.motionX = entityMotion.x;
        entity.motionY = entityMotion.y;
        entity.motionZ = entityMotion.z;

        if (transformEntityBoundingBox) {
            // Transform the bounding box too
            final AxisAlignedBB oldBB = entity.getEntityBoundingBox();
            final Polygon newBBPoly = new Polygon(oldBB, transform);
            final AxisAlignedBB newBB = newBBPoly.getEnclosedAABB();

            final double oldBBSize = (oldBB.maxX - oldBB.minX) * (oldBB.maxY - oldBB.minY) * (oldBB.maxZ - oldBB.minZ);
            final double newBBSize = (newBB.maxX - newBB.minX) * (newBB.maxY - newBB.minY) * (newBB.maxZ - newBB.minZ);
            final double scaleFactor = Math.pow(oldBBSize / newBBSize, 1.0 / 3.0);

            // Scale the bounding box such that the new bounding box is the same size as the old bounding box.
            final AxisAlignedBB newBBScaled = newBB.grow(
                    (scaleFactor - 1) * (newBB.maxX - newBB.minX) / 2.0,
                    (scaleFactor - 1) * (newBB.maxY - newBB.minY) / 2.0,
                    (scaleFactor - 1) * (newBB.maxZ - newBB.minZ) / 2.0
            );

            entity.setPosition(entityPos.x, entityPos.y, entityPos.z);
            entity.setEntityBoundingBox(newBBScaled);
        } else {
            // Just use the regular bounding box
            entity.setPosition(entityPos.x, entityPos.y, entityPos.z);
        }
    }

    /**
     * Used for bad code that tries reading tile entities from a non game thread. But hey it works (mostly).
     */
    @Nullable
    public TileEntity getTileEntitySafe(World world, BlockPos pos) {
        return world.getChunk(pos).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
    }

    @Nullable
    public ShipData getLastShipTouchedByEntity(final Entity entity) {
        final IDraggable asDraggable = IDraggable.class.cast(entity);
        return asDraggable.getEntityShipMovementData().getLastTouchedShip();
    }

    @NonNull
    public EntityShipMovementData getEntityShipMovementDataFor(final Entity entity) {
        final IDraggable asDraggable = IDraggable.class.cast(entity);
        return asDraggable.getEntityShipMovementData();
    }

}
