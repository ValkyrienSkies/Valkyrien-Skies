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

    // Piggyback this with every CPacketPlayer.
    private PlayerMovementData addedPlayerMovementData;

    @Inject(method = "processPacket", at = @At(value = "HEAD"), cancellable = true)
    private void preProcessPacket(final INetHandlerPlayServer handler, final CallbackInfo info) {
        final EntityPlayer player = ((NetHandlerPlayServer) handler).player;
        final World world = player.world;

        final UUID lastTouchedShipId = addedPlayerMovementData.getLastTouchedShipId();
        final int ticksSinceTouchedLastShip = addedPlayerMovementData.getTicksSinceTouchedLastShip();
        final int ticksPartOfGround = addedPlayerMovementData.getTicksPartOfGround();
        final Vector3d playerPosInShip = new Vector3d(addedPlayerMovementData.getPlayerPosInShip());
        final Vector3d playerLookInShip = new Vector3d(addedPlayerMovementData.getPlayerLookInShip());

        ShipData lastTouchedShip = null;
        if (lastTouchedShipId != null) {
            final QueryableShipData queryableShipData = QueryableShipData.get(world);
            final Optional<ShipData> shipDataOptional = queryableShipData.getShip(lastTouchedShipId);
            if (shipDataOptional.isPresent()) {
                lastTouchedShip = shipDataOptional.get();
                final ShipTransform shipTransform = lastTouchedShip.getShipTransform();
                shipTransform.transformPosition(playerPosInShip, TransformType.SUBSPACE_TO_GLOBAL);
                shipTransform.transformDirection(playerLookInShip, TransformType.SUBSPACE_TO_GLOBAL);
            } else {
                // Rare case, just ignore this
                return;
            }
        }

        // Get the player pitch/yaw from the look vector
        final Tuple<Double, Double> pitchYawTuple = VSMath.getPitchYawFromVector(playerLookInShip);
        final double playerPitchInGlobal = pitchYawTuple.getFirst();
        final double playerYawInGlobal = pitchYawTuple.getSecond();

        // Maybe just set the position directly?
        player.setPositionAndRotation(playerPosInShip.x(), playerPosInShip.y(), playerPosInShip.z(),
                (float) playerYawInGlobal, (float) playerPitchInGlobal);
        player.setRotationYawHead((float) playerYawInGlobal);
        player.onGround = addedPlayerMovementData.isOnGround();

        final IDraggable playerAsDraggable = IDraggable.class.cast(player);
        playerAsDraggable.setEntityShipMovementData(
                playerAsDraggable.getEntityShipMovementData()
                        .withLastTouchedShip(lastTouchedShip)
                        .withAddedLinearVelocity(new Vector3d())
                        .withAddedYawVelocity(0)
                        .withTicksPartOfGround(ticksPartOfGround)
                        .withTicksSinceTouchedShip(ticksSinceTouchedLastShip)
        );

        info.cancel();
    }

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
