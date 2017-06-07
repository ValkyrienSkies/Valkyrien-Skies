package ValkyrienWarfareBase.Mixin.world;

import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.ValkyrienWarfareMod;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(World.class)
public abstract class MixinWorldCLIENT {

    @Shadow
    public int getLightFromNeighborsFor(EnumSkyBlock type, BlockPos pos) { return 0; }

    @Inject(method = "getCombinedLight(Lnet/minectaft/util/math/BlockPos;I)I", at = @At("HEAD"), cancellable = true)
    public void preGetCombinedLight(BlockPos pos, int lightValue, CallbackInfoReturnable callbackInfoReturnable)    {
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
}
