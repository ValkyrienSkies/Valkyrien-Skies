package ValkyrienWarfareBase.Mixin.world;

import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.Collision.Polygon;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.PhysicsManagement.WorldPhysObjectManager;
import ValkyrienWarfareBase.ValkyrienWarfareMod;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.*;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Mixin(World.class)
public abstract class MixinWorld {

    public static double MAX_ENTITY_RADIUS_ALT = 2.0D;
    @Shadow
    public final boolean isRemote;
    @Shadow
    protected List<IWorldEventListener> eventListeners;

    {
        isRemote = false;
        //dirty hack lol
    }

    public static BlockPos onGetPrecipitationHeightClient(World world, BlockPos posToCheck) {
        BlockPos pos = world.getPrecipitationHeight(posToCheck);
        // Servers shouldn't bother running this code

        Vector traceStart = new Vector(pos.getX() + .5D, Minecraft.getMinecraft().player.posY + 50D, pos.getZ() + .5D);
        Vector traceEnd = new Vector(pos.getX() + .5D, pos.getY() + .5D, pos.getZ() + .5D);

//		System.out.println(traceStart);
//		System.out.println(traceEnd);

        RayTraceResult result = MixinWorld.onRayTraceBlocks(world, traceStart.toVec3d(), traceEnd.toVec3d(), true, true, false);

        if (result != null && result.typeOfHit != RayTraceResult.Type.MISS && result.getBlockPos() != null) {

            PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(world, result.getBlockPos());
            if (wrapper != null) {
//				System.out.println("test");
                Vector blockPosVector = new Vector(result.getBlockPos().getX() + .5D, result.getBlockPos().getY() + .5D, result.getBlockPos().getZ() + .5D);
                wrapper.wrapping.coordTransform.fromLocalToGlobal(blockPosVector);
                BlockPos toReturn = new BlockPos(pos.getX(), blockPosVector.Y + .5D, pos.getZ());
                return toReturn;
            }
        }

        return pos;
    }

    @Shadow
    public IBlockState getBlockState(BlockPos pos) { return null; }

    public static RayTraceResult onRayTraceBlocks(World world, Vec3d vec31, Vec3d vec32, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock) {
        return rayTraceBlocksIgnoreShip(world, vec31, vec32, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock, null);
    }

    public static RayTraceResult rayTraceBlocksIgnoreShip(World world, Vec3d vec31, Vec3d vec32, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock, PhysicsWrapperEntity toIgnore) {
        RayTraceResult vanillaTrace = MixinWorld.rayTraceBlocksOriginal(world, vec31, vec32, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock);
        WorldPhysObjectManager physManager = ValkyrienWarfareMod.physicsManager.getManagerForWorld(world);
        if (physManager == null) {
            return vanillaTrace;
        }

        Vec3d playerEyesPos = vec31;
        Vec3d playerReachVector = vec32.subtract(vec31);

        AxisAlignedBB playerRangeBB = new AxisAlignedBB(vec31.xCoord, vec31.yCoord, vec31.zCoord, vec32.xCoord, vec32.yCoord, vec32.zCoord);

        List<PhysicsWrapperEntity> nearbyShips = physManager.getNearbyPhysObjects(playerRangeBB);
        //Get rid of the Ship that we're not supposed to be RayTracing for
        nearbyShips.remove(toIgnore);

        boolean changed = false;

        double reachDistance = playerReachVector.lengthVector();
        double worldResultDistFromPlayer = 420000000D;
        if (vanillaTrace != null && vanillaTrace.hitVec != null) {
            worldResultDistFromPlayer = vanillaTrace.hitVec.distanceTo(vec31);
        }
        for (PhysicsWrapperEntity wrapper : nearbyShips) {
            playerEyesPos = vec31;
            playerReachVector = vec32.subtract(vec31);
            // TODO: Re-enable
            if (world.isRemote) {
                // ValkyrienWarfareMod.proxy.updateShipPartialTicks(wrapper);
            }
            // Transform the coordinate system for the player eye pos
            playerEyesPos = RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.RwToLTransform, playerEyesPos);
            playerReachVector = RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.RwToLRotation, playerReachVector);
            Vec3d playerEyesReachAdded = playerEyesPos.addVector(playerReachVector.xCoord * reachDistance, playerReachVector.yCoord * reachDistance, playerReachVector.zCoord * reachDistance);
            RayTraceResult resultInShip = MixinWorld.rayTraceBlocksOriginal(world, playerEyesPos, playerEyesReachAdded, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock);
            if (resultInShip != null && resultInShip.hitVec != null && resultInShip.typeOfHit == RayTraceResult.Type.BLOCK) {
                double shipResultDistFromPlayer = resultInShip.hitVec.distanceTo(playerEyesPos);
                if (shipResultDistFromPlayer < worldResultDistFromPlayer) {
                    worldResultDistFromPlayer = shipResultDistFromPlayer;
                    resultInShip.hitVec = RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.RlToWTransform, resultInShip.hitVec);
                    vanillaTrace = resultInShip;
                }
            }
        }

        return vanillaTrace;
    }

    @Nullable
    public static RayTraceResult rayTraceBlocksOriginal(World world, Vec3d vec31, Vec3d vec32, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock) {
        if (!Double.isNaN(vec31.xCoord) && !Double.isNaN(vec31.yCoord) && !Double.isNaN(vec31.zCoord)) {
            if (!Double.isNaN(vec32.xCoord) && !Double.isNaN(vec32.yCoord) && !Double.isNaN(vec32.zCoord)) {
                int i = MathHelper.floor(vec32.xCoord);
                int j = MathHelper.floor(vec32.yCoord);
                int k = MathHelper.floor(vec32.zCoord);
                int l = MathHelper.floor(vec31.xCoord);
                int i1 = MathHelper.floor(vec31.yCoord);
                int j1 = MathHelper.floor(vec31.zCoord);
                BlockPos blockpos = new BlockPos(l, i1, j1);
                IBlockState iblockstate = world.getBlockState(blockpos);
                Block block = iblockstate.getBlock();

                if ((!ignoreBlockWithoutBoundingBox || iblockstate.getCollisionBoundingBox(world, blockpos) != Block.NULL_AABB) && block.canCollideCheck(iblockstate, stopOnLiquid)) {
                    RayTraceResult raytraceresult = iblockstate.collisionRayTrace(world, blockpos, vec31, vec32);

                    if (raytraceresult != null) {
                        return raytraceresult;
                    }
                }

                RayTraceResult raytraceresult2 = null;
                int k1 = 200;

                while (k1-- >= 0) {
                    if (Double.isNaN(vec31.xCoord) || Double.isNaN(vec31.yCoord) || Double.isNaN(vec31.zCoord)) {
                        return null;
                    }

                    if (l == i && i1 == j && j1 == k) {
                        return returnLastUncollidableBlock ? raytraceresult2 : null;
                    }

                    boolean flag2 = true;
                    boolean flag = true;
                    boolean flag1 = true;
                    double d0 = 999.0D;
                    double d1 = 999.0D;
                    double d2 = 999.0D;

                    if (i > l) {
                        d0 = (double) l + 1.0D;
                    } else if (i < l) {
                        d0 = (double) l + 0.0D;
                    } else {
                        flag2 = false;
                    }

                    if (j > i1) {
                        d1 = (double) i1 + 1.0D;
                    } else if (j < i1) {
                        d1 = (double) i1 + 0.0D;
                    } else {
                        flag = false;
                    }

                    if (k > j1) {
                        d2 = (double) j1 + 1.0D;
                    } else if (k < j1) {
                        d2 = (double) j1 + 0.0D;
                    } else {
                        flag1 = false;
                    }

                    double d3 = 999.0D;
                    double d4 = 999.0D;
                    double d5 = 999.0D;
                    double d6 = vec32.xCoord - vec31.xCoord;
                    double d7 = vec32.yCoord - vec31.yCoord;
                    double d8 = vec32.zCoord - vec31.zCoord;

                    if (flag2) {
                        d3 = (d0 - vec31.xCoord) / d6;
                    }

                    if (flag) {
                        d4 = (d1 - vec31.yCoord) / d7;
                    }

                    if (flag1) {
                        d5 = (d2 - vec31.zCoord) / d8;
                    }

                    if (d3 == -0.0D) {
                        d3 = -1.0E-4D;
                    }

                    if (d4 == -0.0D) {
                        d4 = -1.0E-4D;
                    }

                    if (d5 == -0.0D) {
                        d5 = -1.0E-4D;
                    }

                    EnumFacing enumfacing;

                    if (d3 < d4 && d3 < d5) {
                        enumfacing = i > l ? EnumFacing.WEST : EnumFacing.EAST;
                        vec31 = new Vec3d(d0, vec31.yCoord + d7 * d3, vec31.zCoord + d8 * d3);
                    } else if (d4 < d5) {
                        enumfacing = j > i1 ? EnumFacing.DOWN : EnumFacing.UP;
                        vec31 = new Vec3d(vec31.xCoord + d6 * d4, d1, vec31.zCoord + d8 * d4);
                    } else {
                        enumfacing = k > j1 ? EnumFacing.NORTH : EnumFacing.SOUTH;
                        vec31 = new Vec3d(vec31.xCoord + d6 * d5, vec31.yCoord + d7 * d5, d2);
                    }

                    l = MathHelper.floor(vec31.xCoord) - (enumfacing == EnumFacing.EAST ? 1 : 0);
                    i1 = MathHelper.floor(vec31.yCoord) - (enumfacing == EnumFacing.UP ? 1 : 0);
                    j1 = MathHelper.floor(vec31.zCoord) - (enumfacing == EnumFacing.SOUTH ? 1 : 0);
                    blockpos = new BlockPos(l, i1, j1);
                    IBlockState iblockstate1 = world.getBlockState(blockpos);
                    Block block1 = iblockstate1.getBlock();

                    if (!ignoreBlockWithoutBoundingBox || iblockstate1.getMaterial() == Material.PORTAL || iblockstate1.getCollisionBoundingBox(world, blockpos) != Block.NULL_AABB) {
                        if (block1.canCollideCheck(iblockstate1, stopOnLiquid)) {
                            RayTraceResult raytraceresult1 = iblockstate1.collisionRayTrace(world, blockpos, vec31, vec32);

                            if (raytraceresult1 != null) {
                                return raytraceresult1;
                            }
                        } else {
                            raytraceresult2 = new RayTraceResult(RayTraceResult.Type.MISS, vec31, enumfacing, blockpos);
                        }
                    }
                }

                return returnLastUncollidableBlock ? raytraceresult2 : null;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Overwrite
    public RayTraceResult rayTraceBlocks(Vec3d vec31, Vec3d vec32, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock)    {
        return onRayTraceBlocks(World.class.cast(this), vec31, vec32, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock);
    }

    @Shadow
    protected abstract boolean isChunkLoaded(int x, int z, boolean allowEmpty);

    @Shadow
    public Chunk getChunkFromChunkCoords(int chunkX, int chunkZ) {
        return null;
    }

    public <T extends Entity> List<T> getEntitiesWithinAABBOriginal(Class<? extends T> clazz, AxisAlignedBB aabb, @Nullable Predicate<? super T> filter) {
        int i = MathHelper.floor((aabb.minX - MAX_ENTITY_RADIUS_ALT) / 16.0D);
        int j = MathHelper.ceil((aabb.maxX + MAX_ENTITY_RADIUS_ALT) / 16.0D);
        int k = MathHelper.floor((aabb.minZ - MAX_ENTITY_RADIUS_ALT) / 16.0D);
        int l = MathHelper.ceil((aabb.maxZ + MAX_ENTITY_RADIUS_ALT) / 16.0D);
        List<T> list = Lists.<T>newArrayList();

        for (int i1 = i; i1 < j; ++i1) {
            for (int j1 = k; j1 < l; ++j1) {
                if (this.isChunkLoaded(i1, j1, true)) {
                    this.getChunkFromChunkCoords(i1, j1).getEntitiesOfTypeWithinAABB(clazz, aabb, list, filter);
                }
            }
        }

        return list;
    }

    public List<Entity> getEntitiesInAABBexcludingOriginal(@Nullable Entity entityIn, AxisAlignedBB boundingBox, @Nullable Predicate<? super Entity> predicate) {
        List<Entity> list = Lists.<Entity>newArrayList();
        int i = MathHelper.floor((boundingBox.minX - MAX_ENTITY_RADIUS_ALT) / 16.0D);
        int j = MathHelper.floor((boundingBox.maxX + MAX_ENTITY_RADIUS_ALT) / 16.0D);
        int k = MathHelper.floor((boundingBox.minZ - MAX_ENTITY_RADIUS_ALT) / 16.0D);
        int l = MathHelper.floor((boundingBox.maxZ + MAX_ENTITY_RADIUS_ALT) / 16.0D);

        for (int i1 = i; i1 <= j; ++i1) {
            for (int j1 = k; j1 <= l; ++j1) {
                if (this.isChunkLoaded(i1, j1, true)) {
                    this.getChunkFromChunkCoords(i1, j1).getEntitiesWithinAABBForEntity(entityIn, boundingBox, list, predicate);
                }
            }
        }

        return list;
    }

    @Overwrite
    public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
//		System.out.println((x2-x1)*(y2-y1)*(z2-z1));
//		System.out.println(x1+":"+x2+":"+y1+":"+y2+":"+z1+":"+z2);

        //Stupid OpenComputers fix, blame those assholes
        if (x2 == 1 && y1 == 0 && z2 == 1) {
            x2 = x1 + 1;
            x1--;

            y1 = y2 - 1;
            y2++;

            z2 = z1 + 1;
            z2--;
        }

        int midX = (x1 + x2) / 2;
        int midY = (y1 + y2) / 2;
        int midZ = (z1 + z2) / 2;
        BlockPos newPos = new BlockPos(midX, midY, midZ);
        //.....................................................................................Don't mind this ugly fix, the mixin technically isn't a World so i need this
        //TODO: test ugly fix
        PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(World.class.cast(this), newPos);
        if (wrapper != null && wrapper.wrapping.renderer != null) {
            wrapper.wrapping.renderer.updateRange(x1 - 1, y1 - 1, z1 - 1, x2 + 1, y2 + 1, z2 + 1);
        }

        if (wrapper == null) {
            this.markBlockRangeForRenderUpdateOriginal(x1, y1, z1, x2, y2, z2);
        }
    }

    public void markBlockRangeForRenderUpdateOriginal(int x1, int y1, int z1, int x2, int y2, int z2) {
        for (int i = 0; i < this.eventListeners.size(); ++i) {
            ((IWorldEventListener) this.eventListeners.get(i)).markBlockRangeForRenderUpdate(x1, y1, z1, x2, y2, z2);
        }
    }

    @Inject(method = "getPrecipitationHeight(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/BlockPos;", at = @At("HEAD"), cancellable = true)
    public void preGetPrecipitationHeight(BlockPos pos, CallbackInfoReturnable callbackInfo) {
        if (this.isRemote && ValkyrienWarfareMod.accurateRain) {
            BlockPos accuratePos = MixinWorld.onGetPrecipitationHeightClient(World.class.cast(this), pos);
            callbackInfo.setReturnValue(accuratePos);
            callbackInfo.cancel(); //return the injected value, preventing vanilla code from running
        }
        //if vw didn't change the pos, run the vanilla code
    }

    @Overwrite
    public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> clazz, AxisAlignedBB aabb, @Nullable Predicate<? super T> filter) {
        List toReturn = this.getEntitiesWithinAABBOriginal(clazz, aabb, filter);

        BlockPos pos = new BlockPos((aabb.minX + aabb.maxX) / 2D, (aabb.minY + aabb.maxY) / 2D, (aabb.minZ + aabb.maxZ) / 2D);
        PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(World.class.cast(this), pos);
        if (wrapper != null) {
            Polygon poly = new Polygon(aabb, wrapper.wrapping.coordTransform.lToWTransform);
            aabb = poly.getEnclosedAABB();//.contract(.3D);
            toReturn.addAll(this.getEntitiesWithinAABBOriginal(clazz, aabb, filter));
        }
        return toReturn;
    }

    @Overwrite
    public List<Entity> getEntitiesInAABBexcluding(@Nullable Entity entityIn, AxisAlignedBB boundingBox, @Nullable Predicate<? super Entity> predicate) {
        if ((boundingBox.maxX - boundingBox.minX) * (boundingBox.maxZ - boundingBox.minZ) > 1000000D) {
            return new ArrayList();
        }

        //Prevents the players item pickup AABB from merging with a PhysicsWrapperEntity AABB
        if (entityIn instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entityIn;
            if (player.isRiding() && !player.getRidingEntity().isDead && player.getRidingEntity() instanceof PhysicsWrapperEntity) {
                AxisAlignedBB axisalignedbb = player.getEntityBoundingBox().union(player.getRidingEntity().getEntityBoundingBox()).expand(1.0D, 0.0D, 1.0D);

                if (boundingBox.equals(axisalignedbb)) {
                    boundingBox = player.getEntityBoundingBox().expand(1.0D, 0.5D, 1.0D);
                }
            }
        }

        List toReturn = this.getEntitiesInAABBexcludingOriginal(entityIn, boundingBox, predicate);

        BlockPos pos = new BlockPos((boundingBox.minX + boundingBox.maxX) / 2D, (boundingBox.minY + boundingBox.maxY) / 2D, (boundingBox.minZ + boundingBox.maxZ) / 2D);
        PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(World.class.cast(this), pos);
        if (wrapper != null) {
            Polygon poly = new Polygon(boundingBox, wrapper.wrapping.coordTransform.lToWTransform);
            boundingBox = poly.getEnclosedAABB().contract(.3D);
            toReturn.addAll(this.getEntitiesInAABBexcludingOriginal(entityIn, boundingBox, predicate));
        }
        return toReturn;
    }

    //TODO: acutally move the sound, i don't think this does anything yet
    @Inject(method = "playSound(DDDLnet/minecraft/util/SoundEvent;Lnet/minecraft/util/SoundCategory;FFZ)V", at = @At("HEAD"))
    public void prePlaySound(double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch, boolean distanceDelay, CallbackInfo callbackInfo) {
        BlockPos pos = new BlockPos(x, y, z);
        PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(World.class.cast(this), pos);
        if (wrapper != null) {
            Vector posVec = new Vector(x, y, z);
            wrapper.wrapping.coordTransform.fromLocalToGlobal(posVec);
            x = posVec.X;
            y = posVec.Y;
            z = posVec.Z;
        }
    }

    @Inject(method = "", at = @At("HEAD"))
    public void preSetBlockState(BlockPos pos, IBlockState newState, int flags, CallbackInfo callbackInfo)  {
        IBlockState oldState = this.getBlockState(pos);
        PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(World.class.cast(this), pos);
        if (wrapper != null) {
            wrapper.wrapping.onSetBlockState(oldState, newState, pos);
        }
    }
}
