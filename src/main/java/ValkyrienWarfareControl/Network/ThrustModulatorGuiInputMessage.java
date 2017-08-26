package ValkyrienWarfareControl.Network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class ThrustModulatorGuiInputMessage implements IMessage {

	public BlockPos tileEntityPos;
	public float idealYHeight;
	public float maximumYVelocity;

	public ThrustModulatorGuiInputMessage(BlockPos tileEntityPos, float idealYHeight, float maximumYVelocity) {
		this.tileEntityPos = tileEntityPos;
		this.idealYHeight = idealYHeight;
		this.maximumYVelocity = maximumYVelocity;
	}

	public ThrustModulatorGuiInputMessage() {
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		PacketBuffer packetBuf = new PacketBuffer(buf);

		tileEntityPos = packetBuf.readBlockPos();
		idealYHeight = packetBuf.readFloat();
		maximumYVelocity = packetBuf.readFloat();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		PacketBuffer packetBuf = new PacketBuffer(buf);

		packetBuf.writeBlockPos(tileEntityPos);
		packetBuf.writeFloat(idealYHeight);
		packetBuf.writeFloat(maximumYVelocity);
	}

}
