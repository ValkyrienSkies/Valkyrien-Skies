package org.valkyrienskies.mixin.world;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.valkyrienskies.mod.common.config.VSConfig;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;
import org.valkyrienskies.mod.common.util.JOML;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;

import java.util.Optional;

/**
 * This class contains the mixins for the World class that are client side only.
 */
@Mixin(World.class)
public class MixinClientWorld {

    private final World thisAsWorld = World.class.cast(this);

    /**
     * This is easier to have as an overwrite because there's less laggy hackery to be done then :P
     *
     * @author DaPorkchop_, rubydesic
     */
    @Overwrite
    public BlockPos getPrecipitationHeight(BlockPos input) {
        BlockPos pos = thisAsWorld.getChunk(input).getPrecipitationHeight(input);

        if (VSConfig.accurateRain && Minecraft.getMinecraft().player != null) {
            Vec3d traceStart = new Vec3d(pos.getX() + .5D, Minecraft.getMinecraft().player.posY + 50D, pos.getZ() + .5D);
            Vec3d traceEnd = new Vec3d(pos.getX() + .5D, pos.getY() + .5D, pos.getZ() + .5D);

            RayTraceResult result = thisAsWorld.rayTraceBlocks(traceStart, traceEnd, true, true, false);

            //noinspection ConstantConditions
            if (result != null && result.getBlockPos() != null && result.typeOfHit != RayTraceResult.Type.MISS) {
                Optional<PhysicsObject> physicsObject = ValkyrienUtils.getPhysoManagingBlock(thisAsWorld, result.getBlockPos());

                if (physicsObject.isPresent()) {
                    Vector3d blockPosVector = JOML.convertDouble(result.getBlockPos())
                        .add(0.5, 0.5, 0.5);

                    physicsObject.get()
                        .getShipTransformationManager()
                        .getCurrentTickTransform()
                        .getSubspaceToGlobal()
                        .transformPosition(blockPosVector);

                    return new BlockPos(pos.getX(), blockPosVector.y() + .5D, pos.getZ());
                }
            }
        }
        return pos;
    }

    /*
    @Inject(method = "getCombinedLight(Lnet/minecraft/util/math/BlockPos;I)I", at = @At("HEAD"),
        cancellable = true)
    private void preGetCombinedLight(BlockPos pos, int lightValue,
        CallbackInfoReturnable<Integer> callbackInfoReturnable) {
        try {
            int i = world.getLightFromNeighborsFor(EnumSkyBlock.SKY, pos);
            int j = world.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, pos);
            AxisAlignedBB lightBB = new AxisAlignedBB(pos.getX() - 2, pos.getY() - 2,
                pos.getZ() - 2, pos.getX() + 2, pos.getY() + 2, pos.getZ() + 2);
            List<PhysicsWrapperEntity> physEnts = ValkyrienSkiesMod.VS_PHYSICS_MANAGER
                .getManagerForWorld(world).getNearbyPhysObjects(lightBB);

            for (PhysicsWrapperEntity physEnt : physEnts) {
                BlockPos posInLocal = physEnt.getPhysicsObject().getShipTransformationManager()
                    .getCurrentTickTransform().transform(pos, TransformType.GLOBAL_TO_SUBSPACE);
                int localI = world.getLightFromNeighborsFor(EnumSkyBlock.SKY, posInLocal);
                int localJ = world.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, posInLocal);
                if (localI == 0 && localJ == 0) {
                    localI = world.getLightFromNeighborsFor(EnumSkyBlock.SKY, posInLocal.up());
                    localJ = world.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, posInLocal.up());
                }
                if (localI == 0 && localJ == 0) {
                    localI = world.getLightFromNeighborsFor(EnumSkyBlock.SKY, posInLocal.down());
                    localJ = world.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, posInLocal.down());
                }
                if (localI == 0 && localJ == 0) {
                    localI = world.getLightFromNeighborsFor(EnumSkyBlock.SKY, posInLocal.north());
                    localJ = world.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, posInLocal.north());
                }
                if (localI == 0 && localJ == 0) {
                    localI = world.getLightFromNeighborsFor(EnumSkyBlock.SKY, posInLocal.south());
                    localJ = world.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, posInLocal.south());
                }
                if (localI == 0 && localJ == 0) {
                    localI = world.getLightFromNeighborsFor(EnumSkyBlock.SKY, posInLocal.east());
                    localJ = world.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, posInLocal.east());
                }
                if (localI == 0 && localJ == 0) {
                    localI = world.getLightFromNeighborsFor(EnumSkyBlock.SKY, posInLocal.west());
                    localJ = world.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, posInLocal.west());
                }

                i = Math.min(localI, i);
                j = Math.max(localJ, j);
            }

            if (j < lightValue) {
                j = lightValue;
            }

            callbackInfoReturnable.setReturnValue(i << 20 | j << 4);
        } catch (Exception e) {
            System.err
                .println("Something just went wrong here, getting default light value instead!");
            e.printStackTrace();
        }
    }
    */
}
