package org.valkyrienskies.mixin.network.play.client;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.mod.common.network.IHasPlayerMovementData;
import org.valkyrienskies.mod.common.network.PlayerMovementData;

@Mixin(CPacketPlayer.class)
public class MixinCPacketPlayer implements IHasPlayerMovementData {

    // Piggyback this with every CPacketPlayer.
    private PlayerMovementData addedPlayerMovementData;

    @Inject(method = "readPacketData", at = @At(value = "HEAD"))
    private void preReadPacketData(final PacketBuffer packetBuffer, final CallbackInfo info) {
        addedPlayerMovementData = PlayerMovementData.readData(packetBuffer);
    }

    @Inject(method = "writePacketData", at = @At(value = "HEAD"))
    private void preWritePacketData(final PacketBuffer packetBuffer, final CallbackInfo info) {
        addedPlayerMovementData.writeData(packetBuffer);
    }

    @Override
    public void setPlayerMovementData(final PlayerMovementData playerMovementData) {
        addedPlayerMovementData = playerMovementData;
    }

    @Override
    public PlayerMovementData getPlayerMovementData() {
        return addedPlayerMovementData;
    }
}
