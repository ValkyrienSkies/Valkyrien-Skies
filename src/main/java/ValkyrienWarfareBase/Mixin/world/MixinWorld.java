package ValkyrienWarfareBase.Mixin.world;

import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.Collision.Polygon;
import ValkyrienWarfareBase.CoreMod.CallRunner;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.ValkyrienWarfareMod;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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

        RayTraceResult result = CallRunner.onRayTraceBlocks(world, traceStart.toVec3d(), traceEnd.toVec3d(), true, true, false);

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
        if((boundingBox.maxX-boundingBox.minX)*(boundingBox.maxZ-boundingBox.minZ) > 1000000D){
            return new ArrayList();
        }

        //Prevents the players item pickup AABB from merging with a PhysicsWrapperEntity AABB
        if(entityIn instanceof EntityPlayer){
            EntityPlayer player = (EntityPlayer)entityIn;
            if (player.isRiding() && !player.getRidingEntity().isDead && player.getRidingEntity() instanceof PhysicsWrapperEntity){
                AxisAlignedBB axisalignedbb = player.getEntityBoundingBox().union(player.getRidingEntity().getEntityBoundingBox()).expand(1.0D, 0.0D, 1.0D);

                if(boundingBox.equals(axisalignedbb)){
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
}
