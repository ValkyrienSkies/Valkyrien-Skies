package org.valkyrienskies.mod.common.network;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.valkyrienskies.mod.common.ship_handling.ShipData;
import org.valkyrienskies.mod.common.util.jackson.VSJacksonUtil;

public class ShipIndexDataMessage implements IMessage {

    private static final ObjectMapper serializer = VSJacksonUtil.getPacketMapper();
    protected final List<ShipData> indexedData;

    public ShipIndexDataMessage() {
        indexedData = new ArrayList<>();
    }

    public void addDataToMessage(Iterable<ShipData> indices) {
        indices.forEach(indexedData::add);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int numberOfIndices = buf.readInt();
        for (int i = 0; i < numberOfIndices; i++) {
            // Read index data from the byte buffer.
            int bytesSize = buf.readInt();
            byte[] bytes = new byte[bytesSize];
            buf.readBytes(bytes);
            try {
                ShipData data = serializer.readValue(bytes, ShipData.class);
                this.indexedData.add(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(indexedData.size());
        for (ShipData data : indexedData) {
            // Write index data to the byte buffer.
            try {
                byte[] dataBytes = serializer.writeValueAsBytes(data);
                int bytesSize = dataBytes.length;
                buf.writeInt(bytesSize);
                buf.writeBytes(dataBytes);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }
}
