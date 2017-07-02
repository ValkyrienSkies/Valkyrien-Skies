package ValkyrienWarfareBase.Mixin.world;

import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.ValkyrienWarfareMod;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(World.class)
public abstract class MixinWorldCLIENT {

    @Shadow
    public int getLightFromNeighborsFor(EnumSkyBlock type, BlockPos pos) {
        return 0;
    }

    @Shadow
    public Chunk getChunkFromBlockCoords(BlockPos pos) {
        return null;
    }

    @Inject(method = "getCombinedLight(Lnet/minecraft/util/math/BlockPos;I)I", at = @At("HEAD"), cancellable = true)
    public void preGetCombinedLight(BlockPos pos, int lightValue, CallbackInfoReturnable callbackInfoReturnable) {
        try {
            int i = this.getLightFromNeighborsFor(EnumSkyBlock.SKY, pos);
            int j = this.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, pos);
            AxisAlignedBB lightBB = new AxisAlignedBB(pos.getX() - 2, pos.getY() - 2, pos.getZ() - 2, pos.getX() + 2, pos.getY() + 2, pos.getZ() + 2);
            List<PhysicsWrapperEntity> physEnts = ValkyrienWarfareMod.physicsManager.getManagerForWorld(World.class.cast(this)).getNearbyPhysObjects(lightBB);

            for (PhysicsWrapperEntity physEnt : physEnts) {
                BlockPos posInLocal = RotationMatrices.applyTransform(physEnt.wrapping.coordTransform.wToLTransform, pos);
                int localI = this.getLightFromNeighborsFor(EnumSkyBlock.SKY, posInLocal);
                int localJ = this.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, posInLocal);
                if (localI == 0 && localJ == 0) {
                    localI = this.getLightFromNeighborsFor(EnumSkyBlock.SKY, posInLocal.up());
                    localJ = this.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, posInLocal.up());
                }
                if (localI == 0 && localJ == 0) {
                    localI = this.getLightFromNeighborsFor(EnumSkyBlock.SKY, posInLocal.down());
                    localJ = this.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, posInLocal.down());
                }
                if (localI == 0 && localJ == 0) {
                    localI = this.getLightFromNeighborsFor(EnumSkyBlock.SKY, posInLocal.north());
                    localJ = this.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, posInLocal.north());
                }
                if (localI == 0 && localJ == 0) {
                    localI = this.getLightFromNeighborsFor(EnumSkyBlock.SKY, posInLocal.south());
                    localJ = this.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, posInLocal.south());
                }
                if (localI == 0 && localJ == 0) {
                    localI = this.getLightFromNeighborsFor(EnumSkyBlock.SKY, posInLocal.east());
                    localJ = this.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, posInLocal.east());
                }
                if (localI == 0 && localJ == 0) {
                    localI = this.getLightFromNeighborsFor(EnumSkyBlock.SKY, posInLocal.west());
                    localJ = this.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, posInLocal.west());
                }

                i = Math.min(localI, i);
                j = Math.max(localJ, j);
            }

            if (j < lightValue) {
                j = lightValue;
            }

            callbackInfoReturnable.setReturnValue(i << 20 | j << 4);
            callbackInfoReturnable.cancel();
            return;
        } catch (Exception e) {
            System.err.println("Something just went wrong here, getting default light value instead!!!!");
            e.printStackTrace();
        }
    }

    @Shadow
    public abstract RayTraceResult rayTraceBlocks(Vec3d start, Vec3d end, boolean bool1, boolean bool2, boolean bool3);

    @Overwrite
    public BlockPos getPrecipitationHeight(BlockPos input) {
        BlockPos pos = this.getChunkFromBlockCoords(input).getPrecipitationHeight(input);
        if (ValkyrienWarfareMod.accurateRain) {
            Vector traceStart = new Vector(pos.getX() + .5D, Minecraft.getMinecraft().player.posY + 50D, pos.getZ() + .5D);
            Vector traceEnd = new Vector(pos.getX() + .5D, pos.getY() + .5D, pos.getZ() + .5D);

            RayTraceResult result = rayTraceBlocks(traceStart.toVec3d(), traceEnd.toVec3d(), true, true, false);

            if (result != null && result.typeOfHit != RayTraceResult.Type.MISS && result.getBlockPos() != null) {

                PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(World.class.cast(this), result.getBlockPos());
                if (wrapper != null) {
//				System.out.println("test");
                    Vector blockPosVector = new Vector(result.getBlockPos().getX() + .5D, result.getBlockPos().getY() + .5D, result.getBlockPos().getZ() + .5D);
                    wrapper.wrapping.coordTransform.fromLocalToGlobal(blockPosVector);
                    BlockPos toReturn = new BlockPos(pos.getX(), blockPosVector.Y + .5D, pos.getZ());
                    return toReturn;
                }
            }
        }
        return pos;
    }
}
