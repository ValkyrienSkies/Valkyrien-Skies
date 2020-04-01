package org.valkyrienskies.mod.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.UUID;

/**
 * Sending this messages tells a client to unload a Ship.
 */
public class UnloadPhysObjMessage implements IMessage {

    UUID toUnloadID;

    public UnloadPhysObjMessage() {
        this.toUnloadID = null;
    }

    public void initializeData(UUID toUnloadID) {
        this.toUnloadID = toUnloadID;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        toUnloadID = new UUID(buf.readLong(), buf.readLong());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(toUnloadID.getMostSignificantBits());
        buf.writeLong(toUnloadID.getLeastSignificantBits());
    }
}
