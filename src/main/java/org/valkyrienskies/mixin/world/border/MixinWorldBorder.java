package org.valkyrienskies.mixin.world.border;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.mod.common.physmanagement.chunk.ShipChunkAllocator;

/**
 * We need this otherwise players can't interact with ships in worlds with a world border.
 */
@Mixin(WorldBorder.class)
public abstract class MixinWorldBorder {

    @Inject(method = "contains(Lnet/minecraft/util/math/BlockPos;)Z",
        at = @At("HEAD"),
        cancellable = true)
    public void preContains(BlockPos pos, CallbackInfoReturnable<Boolean> callbackInfo) {
        if (ShipChunkAllocator.isBlockInShipyard(pos)) {
            callbackInfo.setReturnValue(true);
        }
    }
}
