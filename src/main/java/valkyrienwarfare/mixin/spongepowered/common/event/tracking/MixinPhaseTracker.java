package valkyrienwarfare.mixin.spongepowered.common.event.tracking;

import org.spongepowered.asm.mixin.Mixin;

/**
 * Used to hook into the setBlockState() method when Sponge is loaded.
 *
 * @author thebest108
 */
@Mixin(targets = "org/spongepowered/common/event/tracking/PhaseTracker", remap = false)
public class MixinPhaseTracker {

    /**
     * This basically replaces the World.setBlockState() when Sponge is loaded, so
     * we'll have to inject our hooks here as well when Sponge is loaded. All
     * setBlockState() calls that occur in Sponge get sent through here.
     *
     * @param mixinWorld
     * @param pos
     * @param newState
     * @param flag
     * @param info
     */

    /*
    @Inject(method = "setBlockState(Lorg/spongepowered/common/interfaces/world/IMixinWorldServer;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lorg/spongepowered/api/world/BlockChangeFlag;)Z", at = @At(value = "HEAD"))
    public void preSetBlockState2(IMixinWorldServer mixinWorld, BlockPos pos, IBlockState newState,
                                  BlockChangeFlag flag, CallbackInfoReturnable info) {
        // For some reason sponge doesn't call this.
        World world = (World) mixinWorld;
        IPhysicsChunk.class.cast(world.getChunkFromBlockCoords(pos)).onSetBlockStatePre(pos, newState);
    }

     */
}
