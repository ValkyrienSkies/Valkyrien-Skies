package org.valkyrienskies.addon.control.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class MessagePlayerStoppedPiloting implements IMessage {

    public BlockPos posToStopPiloting;

    public MessagePlayerStoppedPiloting(BlockPos posToStopPiloting) {
        this.posToStopPiloting = posToStopPiloting;
    }

    public MessagePlayerStoppedPiloting() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer packetBuf = new PacketBuffer(buf);
        posToStopPiloting = new BlockPos(
            packetBuf.readInt(),
            packetBuf.readInt(),
            packetBuf.readInt()
        );
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packetBuf = new PacketBuffer(buf);
        packetBuf.writeInt(posToStopPiloting.getX());
        packetBuf.writeInt(posToStopPiloting.getY());
        packetBuf.writeInt(posToStopPiloting.getZ());
        //use absolute coordinates instead of writeBlockPos in case we ever add compatibility with cubic chunks
    }
}
