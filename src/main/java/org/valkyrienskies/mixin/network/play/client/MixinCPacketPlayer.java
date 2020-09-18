package org.valkyrienskies.mixin.network.play.client;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.World;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.mod.common.network.IHasPlayerMovementData;
import org.valkyrienskies.mod.common.network.PlayerMovementData;
import org.valkyrienskies.mod.common.ships.QueryableShipData;
import org.valkyrienskies.mod.common.ships.ShipData;
import org.valkyrienskies.mod.common.ships.entity_interaction.IDraggable;
import org.valkyrienskies.mod.common.ships.ship_transform.ShipTransform;
import org.valkyrienskies.mod.common.util.VSMath;
import valkyrienwarfare.api.TransformType;

import java.util.Optional;
import java.util.UUID;

@Mixin(CPacketPlayer.class)
public class MixinCPacketPlayer implements IHasPlayerMovementData {

    @Shadow
    public double x;
    @Shadow
    public double y;
    @Shadow
    public double z;
    @Shadow
    public float yaw;
    @Shadow
    public float pitch;

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
