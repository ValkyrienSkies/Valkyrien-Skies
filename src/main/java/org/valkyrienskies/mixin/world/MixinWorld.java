package org.valkyrienskies.mixin.world;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.Interface.Remap;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.mod.common.collision.EntityPolygonCollider;
import org.valkyrienskies.mod.common.collision.Polygon;
import org.valkyrienskies.mod.common.collision.ShipPolygon;
import org.valkyrienskies.mod.common.config.VSConfig;
import org.valkyrienskies.mod.common.config.VSConfig.ExplosionMode;
import org.valkyrienskies.mod.common.ships.ship_transform.ShipTransform;
import org.valkyrienskies.mod.common.ships.ship_world.IHasShipManager;
import org.valkyrienskies.mod.common.ships.ship_world.IPhysObjectWorld;
import org.valkyrienskies.mod.common.ships.ship_world.IWorldVS;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;
import org.valkyrienskies.mod.common.util.VSMath;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
import org.valkyrienskies.mod.fixes.MixinWorldIntrinsicMethods;
import valkyrienwarfare.api.TransformType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

// TODO: This class is horrible
//       Who cares lol ~Tri0de
@Mixin(value = World.class, priority = 2018)
@Implements(@Interface(iface = MixinWorldIntrinsicMethods.class, prefix = "vs$", remap = Remap.NONE))
public abstract class MixinWorld implements IWorldVS, IHasShipManager {

    private static final double MAX_ENTITY_RADIUS_ALT = 2;
    private static final double BOUNDING_BOX_EDGE_LIMIT = 1000;
    private static final double BOUNDING_BOX_SIZE_LIMIT = 10000;
    private boolean shouldInterceptRayTrace = true;
    // Pork added on to this already bad code because it was already like this so he doesn't feel bad about it
    private PhysicsObject dontInterceptShip = null;

    // The IWorldShipManager
    private IPhysObjectWorld manager = null;

    @Shadow
    protected List<IWorldEventListener> eventListeners;

    private static boolean isBoundingBoxTooLarge(AxisAlignedBB alignedBB) {
        if ((alignedBB.maxX - alignedBB.minX) * (alignedBB.maxY - alignedBB.minY) * (alignedBB.maxZ
            - alignedBB.minZ) > BOUNDING_BOX_SIZE_LIMIT) {
            return true;
        }
        if (alignedBB.maxX - alignedBB.minX > BOUNDING_BOX_EDGE_LIMIT ||
            alignedBB.maxY - alignedBB.minY > BOUNDING_BOX_EDGE_LIMIT ||
            alignedBB.maxZ - alignedBB.minZ > BOUNDING_BOX_EDGE_LIMIT) {
            return true;
        }
        return alignedBB.maxX > Integer.MAX_VALUE || alignedBB.maxX < Integer.MIN_VALUE
            || alignedBB.minX > Integer.MAX_VALUE || alignedBB.minX < Integer.MIN_VALUE
            || alignedBB.maxY > Integer.MAX_VALUE || alignedBB.maxY < Integer.MIN_VALUE
            || alignedBB.minY > Integer.MAX_VALUE || alignedBB.minY < Integer.MIN_VALUE
            || alignedBB.maxZ > Integer.MAX_VALUE || alignedBB.maxZ < Integer.MIN_VALUE
            || alignedBB.minZ > Integer.MAX_VALUE || alignedBB.minZ < Integer.MIN_VALUE;
    }

    /**
     * This is easier to have as an overwrite because there's less laggy hackery to be done then :P
     *
     * @author DaPorkchop_
     */
    @Overwrite
    public void spawnParticle(int particleID, boolean ignoreRange, double x, double y, double z,
        double xSpeed,
        double ySpeed, double zSpeed, int... parameters) {
        BlockPos pos = new BlockPos(x, y, z);
        Optional<PhysicsObject> physicsObject = ValkyrienUtils
            .getPhysoManagingBlock(World.class.cast(this), pos);

        if (physicsObject.isPresent()) {
            Vector3d newPosVec = new Vector3d(x, y, z);
            // RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWTransform,
            // newPosVec);
            physicsObject.get()
                .getShipTransformationManager()
                .getCurrentTickTransform()
                .transformPosition(newPosVec,
                    TransformType.SUBSPACE_TO_GLOBAL);
            x = newPosVec.x;
            y = newPosVec.y;
            z = newPosVec.z;
        }
        for (int i = 0; i < this.eventListeners.size(); ++i) {
            this.eventListeners.get(i)
                .spawnParticle(particleID, ignoreRange, x, y, z, xSpeed, ySpeed, zSpeed,
                    parameters);
        }
    }

    @Shadow
    public IBlockState getBlockState(BlockPos pos) {
        return null;
    }

    @Shadow
    protected abstract boolean isChunkLoaded(int x, int z, boolean allowEmpty);

    @Shadow
    public abstract Chunk getChunk(int chunkX, int chunkZ);

    @Shadow
    public abstract List<AxisAlignedBB> getCollisionBoxes(@Nullable Entity entityIn, AxisAlignedBB aabb);

    /**
     * Used for two purposes. The first, is to prevent the game from freezing by limiting the size of aabb. The second
     * is to fix player sneaking on ships.
     */
    @Inject(method = "getCollisionBoxes(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;ZLjava/util/List;)Z", at = @At("HEAD"), cancellable = true)
    private void preGetCollisionBoxes(@Nullable Entity entityIn, AxisAlignedBB aabb,
        boolean p_191504_3_,
        @Nullable List<AxisAlignedBB> outList, CallbackInfoReturnable<Boolean> callbackInfo) {
        double deltaX = Math.abs(aabb.maxX - aabb.minX);
        double deltaY = Math.abs(aabb.maxY - aabb.minY);
        double deltaZ = Math.abs(aabb.maxZ - aabb.minZ);
        if (Math.max(deltaX, Math.max(deltaY, deltaZ)) > 99999D) {
            System.err.println(entityIn + "\ntried going extremely fast during the collision step");
            new Exception().printStackTrace();
            callbackInfo.setReturnValue(Boolean.FALSE);
            callbackInfo.cancel();
        }

        // Fix player sneaking. I know its strange that the fix would be here of all places, but check
        // Entity.moveEntity() to see for yourself. Minecraft checks if there's any colliding bounding boxes in a given
        // direction, and uses that to determine if can you sneak.
        if (entityIn instanceof EntityPlayer && entityIn.isSneaking()) {
            // Add at most once ship block AABB that is colliding with the player. This is ONLY to properly allow
            // players to sneak while on ships.
            List<PhysicsObject> ships = getManager().getPhysObjectsInAABB(aabb);
            for (PhysicsObject wrapper : ships) {
                Polygon playerInLocal = new Polygon(aabb,
                        wrapper.getShipTransformationManager()
                                .getCurrentTickTransform(),
                        TransformType.GLOBAL_TO_SUBSPACE);
                AxisAlignedBB bb = playerInLocal.getEnclosedAABB();

                if ((bb.maxX - bb.minX) * (bb.maxZ - bb.minZ) > 9898989) {
                    // This is too big, something went wrong here
                    System.err.println("Why did transforming a players bounding box result in a giant bounding box?");
                    System.err.println(bb + "\n" + wrapper.getShipData() + "\n" + entityIn.toString());
                    new Exception().printStackTrace();
                    return;
                }

                List<AxisAlignedBB> collidingBBs = getCollisionBoxes(null, bb);
                Polygon entityPoly = new Polygon(aabb.grow(-.2, 0, -.2));
                for (AxisAlignedBB inLocal : collidingBBs) {
                    ShipPolygon poly = new ShipPolygon(inLocal,
                            wrapper.getShipTransformationManager()
                                    .getCurrentTickTransform(),
                            TransformType.SUBSPACE_TO_GLOBAL,
                            wrapper.getShipTransformationManager().normals,
                            wrapper);

                    EntityPolygonCollider collider = new EntityPolygonCollider(entityPoly, poly, poly.normals, new Vector3d());
                    collider.processData();

                    if (!collider.arePolygonsSeparated()) {
                        outList.add(inLocal);
                        // We only want to add at most ONE aabb to the return value. Once we have at least one, the
                        // vanilla sneak code will work correctly.
                        return;
                    }
                }
            }
        }
    }

    private <T extends Entity> List<T> getEntitiesWithinAABBOriginal(Class<? extends T> clazz,
        AxisAlignedBB aabb,
        @Nullable Predicate<? super T> filter) {
        int i = MathHelper.floor((aabb.minX - MAX_ENTITY_RADIUS_ALT) / 16.0D);
        int j = MathHelper.ceil((aabb.maxX + MAX_ENTITY_RADIUS_ALT) / 16.0D);
        int k = MathHelper.floor((aabb.minZ - MAX_ENTITY_RADIUS_ALT) / 16.0D);
        int l = MathHelper.ceil((aabb.maxZ + MAX_ENTITY_RADIUS_ALT) / 16.0D);
        List<T> list = Lists.newArrayList();

        for (int i1 = i; i1 < j; ++i1) {
            for (int j1 = k; j1 < l; ++j1) {
                if (this.isChunkLoaded(i1, j1, true)) {
                    this.getChunk(i1, j1)
                        .getEntitiesOfTypeWithinAABB(clazz, aabb, list, filter);
                }
            }
        }

        return list;
    }

    private List<Entity> getEntitiesInAABBexcludingOriginal(@Nullable Entity entityIn,
        AxisAlignedBB boundingBox,
        @Nullable Predicate<? super Entity> predicate) {
        List<Entity> list = Lists.newArrayList();
        int i = MathHelper.floor((boundingBox.minX - MAX_ENTITY_RADIUS_ALT) / 16.0D);
        int j = MathHelper.floor((boundingBox.maxX + MAX_ENTITY_RADIUS_ALT) / 16.0D);
        int k = MathHelper.floor((boundingBox.minZ - MAX_ENTITY_RADIUS_ALT) / 16.0D);
        int l = MathHelper.floor((boundingBox.maxZ + MAX_ENTITY_RADIUS_ALT) / 16.0D);

        if (isBoundingBoxTooLarge(boundingBox)) {
            new Exception("Tried getting entities from giant bounding box of " + boundingBox)
                .printStackTrace();
            return list;
        }
        for (int i1 = i; i1 <= j; ++i1) {
            for (int j1 = k; j1 <= l; ++j1) {
                if (this.isChunkLoaded(i1, j1, true)) {
                    this.getChunk(i1, j1)
                        .getEntitiesWithinAABBForEntity(entityIn, boundingBox, list,
                            predicate);
                }
            }
        }

        return list;
    }

    /**
     * @author thebest108
     */
    @Overwrite
    public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> clazz,
        AxisAlignedBB aabb, @Nullable Predicate<? super T> filter) {
        List<T> toReturn = this.getEntitiesWithinAABBOriginal(clazz, aabb, filter);
        BlockPos pos = new BlockPos((aabb.minX + aabb.maxX) / 2D, (aabb.minY + aabb.maxY) / 2D,
            (aabb.minZ + aabb.maxZ) / 2D);
        Optional<PhysicsObject> physicsObject = ValkyrienUtils
            .getPhysoManagingBlock(World.class.cast(this), pos);

        if (physicsObject.isPresent()) {
            Polygon poly = new Polygon(aabb, physicsObject.get()
                .getShipTransformationManager()
                .getCurrentTickTransform(),
                TransformType.SUBSPACE_TO_GLOBAL);
            aabb = poly.getEnclosedAABB();// .contract(.3D);
            toReturn.addAll(this.getEntitiesWithinAABBOriginal(clazz, aabb, filter));
        }
        return toReturn;
    }

    /**
     * aa
     *
     * @author xd
     */
    @Overwrite
    public List<Entity> getEntitiesInAABBexcluding(@Nullable Entity entityIn,
        AxisAlignedBB boundingBox, @Nullable Predicate<? super Entity> predicate) {
        if (isBoundingBoxTooLarge(boundingBox)) {
            new Exception("Tried getting entities from giant bounding box of " + boundingBox)
                .printStackTrace();
            return new ArrayList<>();
        }

        List<Entity> toReturn = this
            .getEntitiesInAABBexcludingOriginal(entityIn, boundingBox, predicate);

        BlockPos pos = new BlockPos((boundingBox.minX + boundingBox.maxX) / 2D,
            (boundingBox.minY + boundingBox.maxY) / 2D, (boundingBox.minZ + boundingBox.maxZ) / 2D);

        Optional<PhysicsObject> physicsObject = ValkyrienUtils
            .getPhysoManagingBlock(World.class.cast(this), pos);

        if (physicsObject.isPresent()) {
            Polygon poly = new Polygon(boundingBox, physicsObject.get()
                .getShipTransformationManager()
                .getCurrentTickTransform(),
                TransformType.SUBSPACE_TO_GLOBAL);
            boundingBox = poly.getEnclosedAABB().shrink(.3D);

            if (isBoundingBoxTooLarge(boundingBox)) {
                new Exception("Tried getting entities from giant bounding box of " + boundingBox)
                    .printStackTrace();
                return new ArrayList<>();
            }

            toReturn
                .addAll(this.getEntitiesInAABBexcludingOriginal(entityIn, boundingBox, predicate));
        }
        return toReturn;
    }

    // This is a forge method not vanilla, so we don't remap this.
    @Shadow(remap = false)
    public abstract Iterator<Chunk> getPersistentChunkIterable(Iterator<Chunk> chunkIterator);

    @Intrinsic(displace = true)
    public Iterator<Chunk> vs$getPersistentChunkIterable(Iterator<Chunk> chunkIterator) {
        ArrayList<Chunk> persistentChunks = new ArrayList<>();
        while (chunkIterator.hasNext()) {
            Chunk chunk = chunkIterator.next();
            persistentChunks.add(chunk);
        }
        Iterator<Chunk> replacementIterator = persistentChunks.iterator();

        return getPersistentChunkIterable(replacementIterator);
    }

    @Override
    public void excludeShipFromRayTracer(PhysicsObject entity) {
        if (this.dontInterceptShip != null) {
            throw new IllegalStateException("excluded ship is already set!");
        }
        this.dontInterceptShip = entity;
    }

    @Override
    public void unexcludeShipFromRayTracer(PhysicsObject entity) {
        if (this.dontInterceptShip != entity) {
            throw new IllegalStateException("must exclude the same ship!");
        }
        this.dontInterceptShip = null;
    }

    @Inject(method = "rayTraceBlocks(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;ZZZ)Lnet/minecraft/util/math/RayTraceResult;", at = @At("HEAD"), cancellable = true)
    private void preRayTraceBlocks(Vec3d vec31, Vec3d vec32, boolean stopOnLiquid,
        boolean ignoreBlockWithoutBoundingBox,
        boolean returnLastUncollidableBlock, CallbackInfoReturnable<RayTraceResult> callbackInfo) {
        if (this.shouldInterceptRayTrace) {
            callbackInfo.setReturnValue(rayTraceBlocksIgnoreShip(vec31, vec32, stopOnLiquid,
                ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock,
                this.dontInterceptShip));
        }
    }

    @Override
    public RayTraceResult rayTraceBlocksIgnoreShip(Vec3d vec31, Vec3d vec32, boolean stopOnLiquid,
                                                   boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock,
                                                   PhysicsObject toIgnore) {
        this.shouldInterceptRayTrace = false;
        RayTraceResult vanillaTrace = World.class.cast(this)
            .rayTraceBlocks(vec31, vec32, stopOnLiquid,
                ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock);


        IPhysObjectWorld physObjectWorld = ((IHasShipManager) (this)).getManager();

        if (physObjectWorld == null) {
            return vanillaTrace;
        }

        Vec3d playerReachVector = vec32.subtract(vec31);

        AxisAlignedBB playerRangeBB = new AxisAlignedBB(vec31.x, vec31.y, vec31.z, vec32.x, vec32.y,
            vec32.z);

        List<PhysicsObject> nearbyShips = physObjectWorld.getPhysObjectsInAABB(playerRangeBB);
        // Get rid of the Ship that we're not supposed to be RayTracing for
        nearbyShips.remove(toIgnore);

        double reachDistance = playerReachVector.length();
        double worldResultDistFromPlayer = 420000000D;
        if (vanillaTrace != null && vanillaTrace.hitVec != null) {
            worldResultDistFromPlayer = vanillaTrace.hitVec.distanceTo(vec31);
        }

        for (PhysicsObject wrapper : nearbyShips) {
            Vec3d playerEyesPos = vec31;
            playerReachVector = vec32.subtract(vec31);

            ShipTransform shipTransform = wrapper.getShipTransformationManager()
                .getRenderTransform();

            playerEyesPos = shipTransform.transform(playerEyesPos,
                TransformType.GLOBAL_TO_SUBSPACE);
            playerReachVector = shipTransform.rotate(playerReachVector,
                TransformType.GLOBAL_TO_SUBSPACE);

            Vec3d playerEyesReachAdded = playerEyesPos.add(playerReachVector.x * reachDistance,
                playerReachVector.y * reachDistance, playerReachVector.z * reachDistance);
            RayTraceResult resultInShip = World.class.cast(this)
                .rayTraceBlocks(playerEyesPos, playerEyesReachAdded,
                    stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock);
            if (resultInShip != null && resultInShip.hitVec != null
                && resultInShip.typeOfHit == Type.BLOCK) {
                double shipResultDistFromPlayer = resultInShip.hitVec.distanceTo(playerEyesPos);
                if (shipResultDistFromPlayer < worldResultDistFromPlayer) {
                    worldResultDistFromPlayer = shipResultDistFromPlayer;
                    // The hitVec must ALWAYS be in global coordinates.
                    resultInShip.hitVec = shipTransform
                        .transform(resultInShip.hitVec, TransformType.SUBSPACE_TO_GLOBAL);
                    vanillaTrace = resultInShip;
                }
            }
        }

        this.shouldInterceptRayTrace = true;
        return vanillaTrace;
    }

    @Override
    public RayTraceResult rayTraceBlocksInShip(Vec3d vec31, Vec3d vec32, boolean stopOnLiquid,
                                               boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock,
                                               PhysicsObject toUse) {
        this.shouldInterceptRayTrace = false;

        final ShipTransform shipTransform = toUse.getShipTransformationManager()
                .getRenderTransform();

        final Vec3d traceStart = shipTransform.transform(vec31,
                TransformType.GLOBAL_TO_SUBSPACE);
        final Vec3d traceEnd = shipTransform.transform(vec32,
                TransformType.GLOBAL_TO_SUBSPACE);

        final RayTraceResult resultInShip = World.class.cast(this)
                .rayTraceBlocks(traceStart, traceEnd,
                        stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock);
        if (resultInShip != null && resultInShip.hitVec != null && resultInShip.typeOfHit == Type.BLOCK) {
            // The hitVec must ALWAYS be in global coordinates.
            resultInShip.hitVec = shipTransform
                    .transform(resultInShip.hitVec, TransformType.SUBSPACE_TO_GLOBAL);
            this.shouldInterceptRayTrace = true;
            return resultInShip;
        }

        this.shouldInterceptRayTrace = true;
        return null;
    }


    @Override
    public IPhysObjectWorld getManager() {
        if (manager == null) {
            throw new IllegalStateException(
                "We can't be accessing this manager since WorldEvent.load() was never called!");
        }
        return manager;
    }

    @Override
    public void setManager(Function<World, IPhysObjectWorld> managerSupplier) {
        manager = managerSupplier.apply(World.class.cast(this));
    }

    private static final RayTraceResult DUMMY_RAYTRACE_RESULT = new RayTraceResult(Vec3d.ZERO, EnumFacing.DOWN);

    /**
     * Use Bresenham's tracing algorithm instead of raytracing for getBlockDensity, makes it way faster
     */
    @Redirect(
        method = "getBlockDensity",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;rayTraceBlocks(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/RayTraceResult;"
        )
    )
    private RayTraceResult rayTraceBlocksForGetBlockDensity(World world, Vec3d start, Vec3d end) {
        if (VSConfig.explosionMode == ExplosionMode.VANILLA) {
            // Vanilla raytrace, ignore ships and perform the function like normal
            this.shouldInterceptRayTrace = false;
            RayTraceResult result = rayTraceBlocks(start, end);
            this.shouldInterceptRayTrace = true;
            return result;
        } else if (VSConfig.explosionMode == ExplosionMode.SLOW_VANILLA) {
            // Vanilla raytrace, include ships and perform the function like normal
            return rayTraceBlocks(start, end);
        }

        java.util.function.Predicate<BlockPos> canCollide = pos -> {
            IBlockState blockState = world.getBlockState(pos);
            return blockState.getBlock().canCollideCheck(blockState, false);
        };

        // Get all the blocks between start and end
        List<BlockPos> blocks = VSMath.generateLineBetween(start, end, BlockPos::new);
        // Whether or not this ray trace hit a block that was collidable.
        boolean collided = blocks.stream().anyMatch(canCollide);

        IPhysObjectWorld physObjectWorld = ((IHasShipManager) (this)).getManager();

        if (physObjectWorld != null) {
            List<PhysicsObject> nearbyShips = physObjectWorld.getPhysObjectsInAABB(
                new AxisAlignedBB(start.x, start.y, start.z, end.x, end.y, end.z));

            for (PhysicsObject obj : nearbyShips) {
                Vec3d transformedStart = obj.transformVector(start, TransformType.GLOBAL_TO_SUBSPACE);
                Vec3d transformedEnd = obj.transformVector(end, TransformType.GLOBAL_TO_SUBSPACE);

                // Transform the raytrace into ship space and check whether or not it hit a block
                List<BlockPos> physoBlocks = VSMath.generateLineBetween(transformedStart, transformedEnd, BlockPos::new);
                collided |= physoBlocks.stream().anyMatch(canCollide);
            }
        }

        // The method only checks if the return object is null, so we don't need any
        // important information in the ray trace result.
        return collided ? DUMMY_RAYTRACE_RESULT : null;
    }

    /**
     * A shadow of the method getBlockDensity() uses to do ray-tracing.
     */
    @Shadow
    public abstract RayTraceResult rayTraceBlocks(Vec3d start, Vec3d end);

    @Shadow
    public abstract boolean checkBlockCollision(AxisAlignedBB axisAlignedBB);

    /**
     * This mixin fixes players getting kicked for flying when they're standing on the ground.
     */
    @Inject(method = "checkBlockCollision", at = @At("HEAD"), cancellable = true)
    public void postCheckBlockCollision(final AxisAlignedBB axisAlignedBB, final CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        // If there wasn't a collision in the world, then check if there is a collision in ships
        final List<PhysicsObject> physObjectsInAABB = getManager().getPhysObjectsInAABB(axisAlignedBB);
        for (final PhysicsObject physicsObject : physObjectsInAABB) {
            final ShipTransform shipTransform = physicsObject.getShipTransform();
            final AxisAlignedBB aabbInShipSpace = new Polygon(axisAlignedBB, shipTransform.getGlobalToSubspace()).getEnclosedAABB();
            final boolean collisionInShip = this.checkBlockCollision(aabbInShipSpace);
            if (collisionInShip) {
                callbackInfoReturnable.setReturnValue(true);
                return;
            }
        }
    }
}
