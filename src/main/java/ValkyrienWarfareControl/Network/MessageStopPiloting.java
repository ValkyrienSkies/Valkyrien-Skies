package ValkyrienWarfareControl.Network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class MessageStopPiloting implements IMessage {

	public BlockPos posToStopPiloting;

	public MessageStopPiloting(BlockPos pos) {
		posToStopPiloting = pos;
	}

	public MessageStopPiloting() {}

	@Override
	public void fromBytes(ByteBuf buf) {
		PacketBuffer packetBuf = new PacketBuffer(buf);
		posToStopPiloting = packetBuf.readBlockPos();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		PacketBuffer packetBuf = new PacketBuffer(buf);
		packetBuf.writeBlockPos(posToStopPiloting);
	}

}
