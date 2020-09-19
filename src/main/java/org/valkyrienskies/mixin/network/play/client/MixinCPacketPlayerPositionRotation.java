package org.valkyrienskies.mixin.network.play.client;

import net.minecraft.network.play.client.CPacketPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.mod.common.network.IHasPlayerMovementData;
import org.valkyrienskies.mod.common.network.PlayerMovementData;
import org.valkyrienskies.mod.common.network.PlayerMovementDataGenerator;

@Mixin(CPacketPlayer.PositionRotation.class)
public class MixinCPacketPlayerPositionRotation {

    @Inject(method = "<init>(DDDFFZ)V", at = @At(value = "RETURN"))
    private void postConstructor(final double x, final double y, final double z, final float yawIn, final float pitchIn, final boolean onGround, final CallbackInfo info) {
        final PlayerMovementData playerMovementData = PlayerMovementDataGenerator.generatePlayerMovementDataForClient();
        IHasPlayerMovementData.class.cast(this).setPlayerMovementData(playerMovementData);
    }

}
