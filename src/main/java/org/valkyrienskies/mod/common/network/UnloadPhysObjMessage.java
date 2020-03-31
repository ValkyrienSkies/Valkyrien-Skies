package org.valkyrienskies.mod.common.network;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.valkyrienskies.mod.common.ship_handling.ShipData;
import org.valkyrienskies.mod.common.util.jackson.VSJacksonUtil;

import java.io.IOException;

/**
 * Sending this messages tells a client to unload a Ship.
 */
public class UnloadPhysObjMessage implements IMessage {

    private static final ObjectMapper serializer = VSJacksonUtil.getPacketMapper();
    ShipData shipToUnload;

    public UnloadPhysObjMessage() {
        this.shipToUnload = null;
    }

    public void initializeData(ShipData shipToSpawnID) {
        this.shipToUnload = shipToSpawnID;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        try {
            shipToUnload = serializer.readValue(bytes, ShipData.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        try {
            byte[] dataBytes = serializer.writeValueAsBytes(shipToUnload);
            buf.writeBytes(dataBytes);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
