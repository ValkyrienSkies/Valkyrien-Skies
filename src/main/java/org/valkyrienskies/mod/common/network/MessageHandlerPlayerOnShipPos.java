package org.valkyrienskies.mod.common.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.joml.Vector3d;
import org.valkyrienskies.mod.common.ships.QueryableShipData;
import org.valkyrienskies.mod.common.ships.ShipData;
import org.valkyrienskies.mod.common.ships.entity_interaction.IDraggable;
import org.valkyrienskies.mod.common.ships.ship_transform.ShipTransform;
import org.valkyrienskies.mod.common.util.VSMath;
import valkyrienwarfare.api.TransformType;

import java.util.Optional;

public class MessageHandlerPlayerOnShipPos implements IMessageHandler<MessagePlayerOnShipPos, IMessage> {

    /**
     * This replaces the vanilla behavior of processing player position/rotation packets when the player is on a ship.
     */
    @Override
    public IMessage onMessage(final MessagePlayerOnShipPos message, final MessageContext ctx) {
        final IThreadListener mainThread = ctx.getServerHandler().server;
        mainThread.addScheduledTask(() -> {
                    final EntityPlayer player = ctx.getServerHandler().player;
                    final World world = player.getEntityWorld();
                    final Optional<ShipData> shipDataOptional = QueryableShipData.get(world).getShip(message.getShipId());
                    if (shipDataOptional.isPresent()) {
                        final ShipData shipData = shipDataOptional.get();
                        final ShipTransform shipTransform = shipData.getShipTransform();
                        final Vector3d playerPosInGlobal = new Vector3d(message.getPlayerPosInShip());
                        shipTransform.transformPosition(playerPosInGlobal, TransformType.SUBSPACE_TO_GLOBAL);
                        final Vector3d playerLookInGlobal = new Vector3d(message.getPlayerLookInShip());
                        shipTransform.transformDirection(playerLookInGlobal, TransformType.SUBSPACE_TO_GLOBAL);

                        final double playerPitchInGlobal = VSMath.getPitchFromVector(playerLookInGlobal);
                        final double playerYawInGlobal = VSMath.getYawFromVector(playerLookInGlobal, playerPitchInGlobal);

                        final CPacketPlayer.PositionRotation cPacketPlayer = new CPacketPlayer.PositionRotation();
                        cPacketPlayer.x = playerPosInGlobal.x();
                        cPacketPlayer.y = playerPosInGlobal.y();
                        cPacketPlayer.z = playerPosInGlobal.z();
                        cPacketPlayer.pitch = (float) playerPitchInGlobal;
                        cPacketPlayer.yaw = (float) playerYawInGlobal;
                        cPacketPlayer.onGround = message.isOnGround();
                        cPacketPlayer.rotating = message.isRotating();
                        cPacketPlayer.moving = message.isMoving();

                        ctx.getServerHandler().processPlayer(cPacketPlayer);
                        // Maybe just set the position directly?
                        // player.setPositionAndRotation(cPacketPlayer.x, cPacketPlayer.y, cPacketPlayer.z, cPacketPlayer.yaw, cPacketPlayer.pitch);
                        // player.onGround = cPacketPlayer.onGround;
                        player.setRotationYawHead(cPacketPlayer.yaw);

                        final IDraggable playerAsDraggable = IDraggable.class.cast(player);
                        playerAsDraggable.setEntityShipMovementData(playerAsDraggable.getEntityShipMovementData().withLastTouchedShip(shipData));
                    }
                }
        );
        return null;
    }
}
