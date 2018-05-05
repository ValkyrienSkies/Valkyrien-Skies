package valkyrienwarfare.mixin.network.play.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.play.client.CPacketPlayer;

@Mixin(CPacketPlayer.PositionRotation.class)
public class MixinCPacketPlayer$PositionRotation extends CPacketPlayer {

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void postInit(CallbackInfo info) {
        System.out.println("Position Rotation Check");
    }

}
