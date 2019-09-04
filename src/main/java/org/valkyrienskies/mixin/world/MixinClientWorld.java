package org.valkyrienskies.mixin.world;

import java.util.List;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.entity.PhysicsWrapperEntity;
import valkyrienwarfare.api.TransformType;

/**
 * This class contains the mixins for the World class that are client side only.
 */
@Mixin(World.class)
public class MixinClientWorld {

    private final World world = World.class.cast(this);

    @Inject(method = "getCombinedLight(Lnet/minecraft/util/math/BlockPos;I)I", at = @At("HEAD"),
        cancellable = true)
    private void preGetCombinedLight(BlockPos pos, int lightValue,
        CallbackInfoReturnable<Integer> callbackInfoReturnable) {
        try {
            int i = world.getLightFromNeighborsFor(EnumSkyBlock.SKY, pos);
            int j = world.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, pos);
            AxisAlignedBB lightBB = new AxisAlignedBB(pos.getX() - 2, pos.getY() - 2,
                pos.getZ() - 2, pos.getX() + 2, pos.getY() + 2, pos.getZ() + 2);
            List<PhysicsWrapperEntity> physEnts = ValkyrienSkiesMod.VW_PHYSICS_MANAGER
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
}
