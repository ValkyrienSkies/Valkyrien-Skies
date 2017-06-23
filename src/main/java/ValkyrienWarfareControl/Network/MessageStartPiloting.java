package ValkyrienWarfareControl.Network;

import ValkyrienWarfareControl.Piloting.ControllerInputType;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class MessageStartPiloting implements IMessage {

	public BlockPos posToStartPiloting;
	public boolean setPhysicsWrapperEntityToPilot;
	public ControllerInputType controlType;

	public MessageStartPiloting(BlockPos posToStartPiloting, boolean setPhysicsWrapperEntityToPilot, ControllerInputType controlType) {
		this.posToStartPiloting = posToStartPiloting;
		this.setPhysicsWrapperEntityToPilot = setPhysicsWrapperEntityToPilot;
		this.controlType = controlType;
	}

	public MessageStartPiloting() {}

	@Override
	public void fromBytes(ByteBuf buf) {
		PacketBuffer packetBuf = new PacketBuffer(buf);
		posToStartPiloting = packetBuf.readBlockPos();
		setPhysicsWrapperEntityToPilot = packetBuf.readBoolean();
		controlType = packetBuf.readEnumValue(ControllerInputType.class);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		PacketBuffer packetBuf = new PacketBuffer(buf);
		packetBuf.writeBlockPos(posToStartPiloting);
		packetBuf.writeBoolean(setPhysicsWrapperEntityToPilot);
		packetBuf.writeEnumValue(controlType);
	}

}
