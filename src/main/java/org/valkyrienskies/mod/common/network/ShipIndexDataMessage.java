package org.valkyrienskies.mod.common.network;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.valkyrienskies.mod.common.ship_handling.ShipData;
import org.valkyrienskies.mod.common.util.jackson.VSJacksonUtil;

import java.io.IOException;
import java.util.*;

/**
 * Sends ShipData updates to the client, also tells it which ShipData to convert load/unload as PhysicsObject.
 */
public class ShipIndexDataMessage implements IMessage {

    private static final ObjectMapper serializer = VSJacksonUtil.getPacketMapper();
    final List<ShipData> indexedData;
    final List<UUID> shipsToLoad, shipsToUnload;

    public ShipIndexDataMessage() {
        this.indexedData = new ArrayList<>();
        this.shipsToLoad = new ArrayList<>();
        this.shipsToUnload = new ArrayList<>();
    }

    public void addData(Collection<ShipData> toSend) {
        indexedData.addAll(toSend);
    }

    public void addLoadUUID(UUID toLoad) {
        shipsToLoad.add(toLoad);
    }

    public void addUnloadUUID(UUID toUnload) {
        shipsToUnload.add(toUnload);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        int numberOfIndices = packetBuffer.readInt();
        int numberOfUUIDLoad = packetBuffer.readInt();
        int numberOfUUIDUnload = packetBuffer.readInt();
        for (int i = 0; i < numberOfIndices; i++) {
            // Read index data from the byte buffer.
            int bytesSize = packetBuffer.readInt();
            byte[] bytes = new byte[bytesSize];
            packetBuffer.readBytes(bytes);
            try {
                ShipData data = serializer.readValue(bytes, ShipData.class);
                this.indexedData.add(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (int i = 0; i < numberOfUUIDLoad; i++) {
            shipsToLoad.add(packetBuffer.readUniqueId());
        }
        for (int i = 0; i < numberOfUUIDUnload; i++) {
            shipsToUnload.add(packetBuffer.readUniqueId());
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        packetBuffer.writeInt(indexedData.size());
        packetBuffer.writeInt(shipsToLoad.size());
        packetBuffer.writeInt(shipsToUnload.size());
        for (ShipData data : indexedData) {
            // Write index data to the byte buffer.
            try {
                byte[] dataBytes = serializer.writeValueAsBytes(data);
                int bytesSize = dataBytes.length;
                packetBuffer.writeInt(bytesSize);
                packetBuffer.writeBytes(dataBytes);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        for (UUID toLoad : shipsToLoad) {
            packetBuffer.writeUniqueId(toLoad);
        }
        for (UUID toUnload : shipsToUnload) {
            packetBuffer.writeUniqueId(toUnload);
        }
    }
}
