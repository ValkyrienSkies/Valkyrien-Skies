package valkyrienwarfare.mixin.world;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

@Mixin(value = WorldServer.class, priority = 1005)
public abstract class MixinWorldServer {

    private final WorldServer thisWorldServer = WorldServer.class.cast(this);
    
    @Inject(method = "setBlockState", at = @At("HEAD"))
    public void duringMarkAndNotifyBlock(BlockPos pos, IBlockState newState, int flags,
            CallbackInfoReturnable callbackInfo) {
        PhysicsWrapperEntity physEntity = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(thisWorldServer,
                pos);
        if (physEntity != null) {
            IBlockState oldState = this.getBlockState(pos);
            physEntity.wrapping.onSetBlockState(oldState, newState, pos);
            if (oldState != newState) {
                System.out.println(oldState.getBlock().getLocalizedName());
                System.out.println(newState.getBlock().getLocalizedName());
            }
        }
    }
    
    @Shadow public abstract IBlockState getBlockState(BlockPos pos);
}
