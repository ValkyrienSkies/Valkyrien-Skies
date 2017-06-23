package ValkyrienWarfareControl.Network;

import java.util.UUID;

import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class SetZepplinPilotMessage implements IMessage {

	public UUID newPilotID;
	public UUID physicsObjectID;

	public SetZepplinPilotMessage(PhysicsWrapperEntity wrapper, EntityPlayer player) {
		newPilotID = player.getUniqueID();
		physicsObjectID = wrapper.getUniqueID();
	}

	public SetZepplinPilotMessage() {}

	@Override
	public void fromBytes(ByteBuf buf) {
		PacketBuffer packetBuf = new PacketBuffer(buf);
		newPilotID = packetBuf.readUniqueId();
		physicsObjectID = packetBuf.readUniqueId();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		PacketBuffer packetBuf = new PacketBuffer(buf);
		packetBuf.writeUniqueId(newPilotID);
		packetBuf.writeUniqueId(physicsObjectID);
	}

}
