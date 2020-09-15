package org.valkyrienskies.mod.common.network;

import lombok.Value;
import net.minecraft.network.PacketBuffer;
import org.joml.Vector3dc;
import org.valkyrienskies.mod.common.util.JOML;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

@Value
public class PlayerMovementData {

    @Nullable
    UUID lastTouchedShipId;
    int ticksSinceTouchedLastShip;
    int ticksPartOfGround;
    @Nonnull
    Vector3dc playerPosInShip;
    @Nonnull
    Vector3dc playerLookInShip;
    boolean onGround;

    /**
     * Reads the raw packet data from the data stream.
     */
    public static PlayerMovementData readData(final PacketBuffer packetBuffer) {
        final boolean hasLastTouchShipId = packetBuffer.readBoolean();
        final UUID lastTouchedShipId = hasLastTouchShipId ? packetBuffer.readUniqueId() : null;
        final int ticksSinceTouchedLastShip = packetBuffer.readInt();
        final int ticksPartOfGround = packetBuffer.readInt();
        final Vector3dc playerPosInShip = JOML.readFromByteBuf(packetBuffer);
        final Vector3dc playerLookInShip = JOML.readFromByteBuf(packetBuffer);
        final boolean onGround = packetBuffer.readBoolean();
        return new PlayerMovementData(
                lastTouchedShipId,
                ticksSinceTouchedLastShip,
                ticksPartOfGround,
                playerPosInShip,
                playerLookInShip,
                onGround
        );
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writeData(final PacketBuffer packetBuffer) {
        packetBuffer.writeBoolean(lastTouchedShipId != null);
        if (lastTouchedShipId != null) {
            packetBuffer.writeUniqueId(lastTouchedShipId);
        }
        packetBuffer.writeInt(ticksSinceTouchedLastShip);
        packetBuffer.writeInt(ticksPartOfGround);
        JOML.writeToByteBuf(playerPosInShip, packetBuffer);
        JOML.writeToByteBuf(playerLookInShip, packetBuffer);
        packetBuffer.writeBoolean(onGround);
    }
}
