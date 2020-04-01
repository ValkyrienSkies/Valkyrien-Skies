package org.valkyrienskies.mod.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.UUID;

/**
 * Sending this message gets the client to spawn a PhysicsObject from a ShipData.
 */
public class SpawnPhysObjMessage implements IMessage {

    UUID shipToSpawnID;

    public SpawnPhysObjMessage() {
        this.shipToSpawnID = null;
    }

    public void initializeData(UUID shipToSpawnID) {
        this.shipToSpawnID = shipToSpawnID;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        shipToSpawnID = new UUID(buf.readLong(), buf.readLong());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(shipToSpawnID.getMostSignificantBits());
        buf.writeLong(shipToSpawnID.getLeastSignificantBits());
    }
}
