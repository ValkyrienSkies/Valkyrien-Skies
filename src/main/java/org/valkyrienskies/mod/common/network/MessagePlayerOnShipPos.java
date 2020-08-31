package org.valkyrienskies.mod.common.network;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.joml.Vector3dc;
import org.valkyrienskies.mod.common.util.JOML;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class MessagePlayerOnShipPos implements IMessage {

    private UUID shipId;
    private Vector3dc playerPosInShip;
    private Vector3dc playerLookInShip;
    private boolean onGround;
    private boolean moving;
    private boolean rotating;

    @Override
    public void fromBytes(final ByteBuf buf) {
        final PacketBuffer packetBuffer = new PacketBuffer(buf);
        shipId = packetBuffer.readUniqueId();
        playerPosInShip = JOML.readFromByteBuf(packetBuffer);
        playerLookInShip = JOML.readFromByteBuf(packetBuffer);
        onGround = packetBuffer.readBoolean();
        moving = packetBuffer.readBoolean();
        rotating = packetBuffer.readBoolean();
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        final PacketBuffer packetBuffer = new PacketBuffer(buf);
        packetBuffer.writeUniqueId(shipId);
        JOML.writeToByteBuf(playerPosInShip, packetBuffer);
        JOML.writeToByteBuf(playerLookInShip, packetBuffer);
        packetBuffer.writeBoolean(onGround);
        packetBuffer.writeBoolean(moving);
        packetBuffer.writeBoolean(rotating);
    }
}
