package org.valkyrienskies.mod.common.network;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.valkyrienskies.mod.common.physics.management.physo.ShipData;
import org.valkyrienskies.mod.common.util.jackson.VSJacksonUtil;

/**
 * Sending this message gets the client to spawn a PhysicsObject from a ShipData.
 */
public class SpawnPhysObjMessage implements IMessage {

    private static final ObjectMapper serializer = createMapper();
    ShipData shipToSpawnData;

    public SpawnPhysObjMessage() {
        this.shipToSpawnData = null;
    }

    private static ObjectMapper createMapper() {
        ObjectMapper mapper = new CBORMapper();

        VSJacksonUtil.configureMapper(mapper);

        return mapper;
    }

    public void initializeData(ShipData shipToSpawnID) {
        this.shipToSpawnData = shipToSpawnID;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        try {
            shipToSpawnData = serializer.readValue(bytes, ShipData.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        try {
            byte[] dataBytes = serializer.writeValueAsBytes(shipToSpawnData);
            buf.writeBytes(dataBytes);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
