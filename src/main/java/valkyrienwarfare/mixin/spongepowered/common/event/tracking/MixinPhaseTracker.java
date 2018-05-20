package valkyrienwarfare.mixin.spongepowered.common.event.tracking;

import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.world.WorldUtil;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

/**
 * Used to hook into the setBlockState() method when Sponge is loaded.
 * 
 * @author thebest108
 *
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
    @Inject(method = "setBlockState(Lorg/spongepowered/common/interfaces/world/IMixinWorldServer;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lorg/spongepowered/api/world/BlockChangeFlag;)Z", at = @At(value = "HEAD"))
    public void preSetBlockState2(IMixinWorldServer mixinWorld, BlockPos pos, IBlockState newState,
            BlockChangeFlag flag, CallbackInfoReturnable info) {
        World world = WorldUtil.asNative(mixinWorld);
        PhysicsWrapperEntity physEntity = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(world, pos);
        if (physEntity != null) {
            IBlockState oldState = world.getBlockState(pos);
            physEntity.wrapping.onSetBlockState(oldState, newState, pos);
        }
    }
}
