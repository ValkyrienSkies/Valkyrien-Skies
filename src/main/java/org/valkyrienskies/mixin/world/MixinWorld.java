package org.valkyrienskies.mixin.world;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Interface.Remap;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.addon.control.block.torque.IRotationNodeWorld;
import org.valkyrienskies.addon.control.block.torque.IRotationNodeWorldProvider;
import org.valkyrienskies.addon.control.block.torque.ImplRotationNodeWorld;
import org.valkyrienskies.fixes.MixinWorldIntrinsicMethods;
import org.valkyrienskies.mod.common.coordinates.ShipTransform;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.collision.polygons.Polygon;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;
import org.valkyrienskies.mod.common.physmanagement.interaction.IWorldVS;
import org.valkyrienskies.mod.common.ship_handling.IHasShipManager;
import org.valkyrienskies.mod.common.ship_handling.IPhysObjectWorld;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
import valkyrienwarfare.api.TransformType;

// TODO: This class is horrible
@Mixin(value = World.class, priority = 2018)
@Implements(@Interface(iface = MixinWorldIntrinsicMethods.class, prefix = "vs$", remap = Remap.NONE))
public abstract class MixinWorld implements IWorldVS, IHasShipManager,
    IRotationNodeWorldProvider {

    private static final double MAX_ENTITY_RADIUS_ALT = 2;
    private static final double BOUNDING_BOX_EDGE_LIMIT = 10000;
    private static final double BOUNDING_BOX_SIZE_LIMIT = 10000;
    private boolean dontIntercept = false;
    // Pork added on to this already bad code because it was already like this so he doesn't feel bad about it
    private PhysicsObject dontInterceptShip = null;

    // The IWorldShipManager
    private IPhysObjectWorld manager = null;
    // Rotation Node World fields. Note this is only used in multiplayer, but making a MixinWorldServer
    // just for one field would be wasteful.
    private ImplRotationNodeWorld rotationNodeWorld = new ImplRotationNodeWorld(null);

    @Shadow
    protected List<IWorldEventListener> eventListeners;

    @Shadow
    public abstract Biome getBiomeForCoordsBody(BlockPos pos);

    /**
     * Enables the correct weather on ships depending on their position.
     */
    @Intrinsic(displace = true)
    public Biome vs$getBiomeForCoordsBody(BlockPos pos) {
        Optional<PhysicsObject> physicsObject = ValkyrienUtils
            .getPhysoManagingBlock(World.class.cast(this), pos);

        if (physicsObject.isPresent()) {
            pos = physicsObject.get()
                .getShipTransformationManager()
                .getCurrentTickTransform().transform(pos, TransformType.SUBSPACE_TO_GLOBAL);
        }
        return getBiomeForCoordsBody(pos);
    }

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
            Vector newPosVec = new Vector(x, y, z);
            // RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWTransform,
            // newPosVec);
            physicsObject.get()
                .getShipTransformationManager()
                .getCurrentTickTransform()
                .transform(newPosVec,
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
        if (!this.dontIntercept) {
            callbackInfo.setReturnValue(rayTraceBlocksIgnoreShip(vec31, vec32, stopOnLiquid,
                ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock,
                this.dontInterceptShip));
        }
    }

    @Override
    public RayTraceResult rayTraceBlocksIgnoreShip(Vec3d vec31, Vec3d vec32, boolean stopOnLiquid,
                                                   boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock,
                                                   PhysicsObject toIgnore) {
        this.dontIntercept = true;
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

        List<PhysicsObject> nearbyShips = physObjectWorld.getNearbyPhysObjects(playerRangeBB);
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
                && resultInShip.typeOfHit == RayTraceResult.Type.BLOCK) {
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

        this.dontIntercept = false;
        return vanillaTrace;
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

    @Override
    public IRotationNodeWorld getPhysicsRotationNodeWorld() {
        return rotationNodeWorld;
    }


    /**
     * Fixes World.getBlockDensity() creating huge amounts of lag by telling it not to look for
     * ships when ray-tracing.
     */
    @Redirect(method = "getBlockDensity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;rayTraceBlocks(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/RayTraceResult;"))
    private RayTraceResult rayTraceBlocksForGetBlockDensity(World world, Vec3d start, Vec3d end) {
        // Don't look for ships when ray tracing.
        this.dontIntercept = true;
        RayTraceResult result = rayTraceBlocks(start, end);
        // Ok, now we can look for ships again.
        this.dontIntercept = false;
        return result;
    }

    /**
     * A shadow of the method getBlockDensity() uses to do ray-tracing.
     */
    @Shadow
    public abstract RayTraceResult rayTraceBlocks(Vec3d start, Vec3d end);

}
