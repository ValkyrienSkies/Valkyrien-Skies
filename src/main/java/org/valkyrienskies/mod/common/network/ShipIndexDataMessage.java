package org.valkyrienskies.mod.common.network;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.valkyrienskies.mod.common.physics.management.physo.ShipData;
import org.valkyrienskies.mod.common.util.jackson.JOMLSerializationModule;
import org.valkyrienskies.mod.common.util.jackson.MinecraftSerializationModule;

public class ShipIndexDataMessage implements IMessage {

    private static final ObjectMapper serializer = createMapper();
    protected final List<ShipData> indexedData;

    public ShipIndexDataMessage() {
        indexedData = new ArrayList<>();
    }

    private static ObjectMapper createMapper() {
        ObjectMapper mapper = new CBORMapper();

        mapper.setVisibility(mapper.getVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE));

        mapper.registerModule(new MinecraftSerializationModule())
            .registerModule(new JOMLSerializationModule());

        return mapper;
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
