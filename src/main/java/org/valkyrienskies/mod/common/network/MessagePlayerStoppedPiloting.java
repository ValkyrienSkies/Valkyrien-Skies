package org.valkyrienskies.mod.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.UUID;

public class MessagePlayerStoppedPiloting implements IMessage {

    public BlockPos posToStopPiloting;
    public UUID shipIDToStopPiloting;

    public MessagePlayerStoppedPiloting(BlockPos posToStopPiloting) {
        this.posToStopPiloting = posToStopPiloting;
        this.shipIDToStopPiloting = null;
    }

    public MessagePlayerStoppedPiloting(UUID shipIDToStopPiloting) {
        this.posToStopPiloting = null;
        this.shipIDToStopPiloting = shipIDToStopPiloting;
    }

    public MessagePlayerStoppedPiloting() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer packetBuf = new PacketBuffer(buf);
        final boolean isBlockPos = packetBuf.readBoolean();
        final boolean isUUID = packetBuf.readBoolean();

        if (isBlockPos) {
            posToStopPiloting = new BlockPos(
                    packetBuf.readInt(),
                    packetBuf.readInt(),
                    packetBuf.readInt()
            );
        }
        if (isUUID) {
            shipIDToStopPiloting = packetBuf.readUniqueId();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packetBuf = new PacketBuffer(buf);

        packetBuf.writeBoolean(posToStopPiloting != null);
        packetBuf.writeBoolean(shipIDToStopPiloting != null);

        if (posToStopPiloting != null) {
            packetBuf.writeInt(posToStopPiloting.getX());
            packetBuf.writeInt(posToStopPiloting.getY());
            packetBuf.writeInt(posToStopPiloting.getZ());
        }

        if (shipIDToStopPiloting != null) {
            packetBuf.writeUniqueId(shipIDToStopPiloting);
        }
        //use absolute coordinates instead of writeBlockPos in case we ever add compatibility with cubic chunks
    }
}
