package org.valkyrienskies.mixin.world;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.config.VSConfig;
import org.valkyrienskies.mod.common.ships.ship_world.IWorldVS;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;
import org.valkyrienskies.mod.common.util.JOML;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
import valkyrienwarfare.api.TransformType;

import java.util.List;
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
        final BlockPos originalHeight = thisAsWorld.getChunk(input).getPrecipitationHeight(input);

        if (VSConfig.accurateRain && Minecraft.getMinecraft().player != null) {
            final AxisAlignedBB boundingBox = new AxisAlignedBB(input.getX() - .5, 0, input.getZ() - .5, input.getX() + .5, 255, input.getZ() + .5);
            final List<PhysicsObject> physicsObjectList = ValkyrienUtils.getPhysObjWorld(thisAsWorld).getPhysObjectsInAABB(boundingBox);

            final Vec3d traceStart = new Vec3d(originalHeight.getX() + .5, Minecraft.getMinecraft().player.posY + 50, originalHeight.getZ() + .5);
            final Vec3d traceEnd = new Vec3d(originalHeight.getX() + .5, originalHeight.getY() + .5, originalHeight.getZ() + .5);

            for (final PhysicsObject physicsObject : physicsObjectList) {
                final RayTraceResult result = ((IWorldVS) thisAsWorld).rayTraceBlocksInShip(traceStart, traceEnd, true, true, false, physicsObject);

                //noinspection ConstantConditions
                if (result != null && result.getBlockPos() != null && result.typeOfHit != RayTraceResult.Type.MISS) {
                    Vector3d blockPosVector = JOML.convertDouble(result.getBlockPos())
                            .add(.5, .5, .5);

                    physicsObject
                            .getShipTransformationManager()
                            .getCurrentTickTransform()
                            .getSubspaceToGlobal()
                            .transformPosition(blockPosVector);

                    return new BlockPos(originalHeight.getX(), blockPosVector.y(), originalHeight.getZ());
                }
            }
        }
        return originalHeight;
    }

    @Inject(method = "getCombinedLight(Lnet/minecraft/util/math/BlockPos;I)I", at = @At("HEAD"),
        cancellable = true)
    private void preGetCombinedLight(BlockPos pos, int lightValue,
        CallbackInfoReturnable<Integer> callbackInfoReturnable) {

        final World world = thisAsWorld;
        try {
            int i = world.getLightFromNeighborsFor(EnumSkyBlock.SKY, pos);
            int j = world.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, pos);
            AxisAlignedBB lightBB = new AxisAlignedBB(pos.getX() - 2, pos.getY() - 2,
                pos.getZ() - 2, pos.getX() + 2, pos.getY() + 2, pos.getZ() + 2);

            final List<PhysicsObject> physicsObjectList = ValkyrienUtils.getPhysObjWorld(world).getPhysObjectsInAABB(lightBB);

            final BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

            for (final PhysicsObject physicsObject : physicsObjectList) {
                final Vector3dc posInLocal = physicsObject.getShipTransformationManager().getRenderTransform()
                        .transformPositionNew(JOML.convertTo3d(pos).add(.5, .5, .5), TransformType.GLOBAL_TO_SUBSPACE);

                final int minX = (int) Math.floor(posInLocal.x());
                final int minY = (int) Math.floor(posInLocal.y());
                final int minZ = (int) Math.floor(posInLocal.z());

                int shipSkyLight = 0;

                for (int x = minX; x <= minX + 1; x++) {
                    for (int y = minY; y <= minY + 1; y++) {
                        for (int z = minZ; z <= minZ + 1; z++) {
                            mutableBlockPos.setPos(x, y, z);

                            final IBlockState blockState = world.getBlockState(mutableBlockPos);

                            // Ignore the light of full blocks
                            if (blockState.isFullBlock()) {
                                continue;
                            }

                            final int localBlockLight = world.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, mutableBlockPos);
                            final int localSkyLight = world.getLightFromNeighborsFor(EnumSkyBlock.SKY, mutableBlockPos);

                            j = Math.max(j, localBlockLight);
                            shipSkyLight = Math.max(shipSkyLight, localSkyLight);
                        }
                    }
                }

                if (i > shipSkyLight) {
                    i = shipSkyLight;
                }
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

}
