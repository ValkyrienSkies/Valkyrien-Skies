package org.valkyrienskies.mod.common.network;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.valkyrienskies.mod.common.physics.management.physo.ShipIndexedData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ShipIndexDataMessage implements IMessage {

    private static final ObjectMapper serializer = createMapper();
    protected final List<ShipIndexedData> indexedData;

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

        mapper.addMixIn(AxisAlignedBB.class, AABBMixinSerializer.class);

        return mapper;
    }

    public void addDataToMessage(Iterable<ShipIndexedData> indices) {
        for (ShipIndexedData data : indices) {
            indexedData.add(data);
        }
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
                ShipIndexedData data = serializer.readValue(bytes, ShipIndexedData.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(indexedData.size());
        for (ShipIndexedData data : indexedData) {
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
