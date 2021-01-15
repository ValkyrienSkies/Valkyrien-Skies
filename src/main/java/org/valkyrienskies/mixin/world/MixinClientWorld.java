package org.valkyrienskies.mixin.world;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.mod.common.config.VSConfig;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;
import org.valkyrienskies.mod.common.util.JOML;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
import org.valkyrienskies.mod.fixes.FixAccurateRain;
import valkyrienwarfare.api.TransformType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class contains the mixins for the World class that are client side only.
 */
@Mixin(World.class)
public class MixinClientWorld {

    private final World thisAsWorld = World.class.cast(this);

    /**
     * Cache to fix lag from the {@link VSConfig#accurateRain} setting.
     */
    private final Map<BlockPos, BlockPos> precipitationHeightCache = new HashMap<>();

    @Inject(method = "tick", at = @At("HEAD"))
    private void onPreTick(CallbackInfo ci) {
        precipitationHeightCache.clear();
    }

    /**
     * This is easier to have as an overwrite because there's less laggy hackery to be done then :P
     *
     * @author DaPorkchop_, rubydesic
     */
    @SideOnly(Side.CLIENT)
    @Overwrite
    public BlockPos getPrecipitationHeight(BlockPos input) {
        final World thisAsWorld = World.class.cast(this);
        final BlockPos originalHeight = thisAsWorld.getChunk(input).getPrecipitationHeight(input);

        // Only do accurate rain on client side
        if (thisAsWorld.isRemote) {
            // Try to use the precipitationHeightCache
            if (precipitationHeightCache.containsKey(input)) {
                final BlockPos cached = precipitationHeightCache.get(input);
                if (cached != null) {
                    return cached;
                }
            }
            final BlockPos rainPosWithShips = FixAccurateRain.getRainPosFromShips(thisAsWorld, originalHeight);
            precipitationHeightCache.put(input, rainPosWithShips);
            return rainPosWithShips;
        } else {
            precipitationHeightCache.put(input, originalHeight);
            return originalHeight;
        }
    }


    @SideOnly(Side.CLIENT)
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
