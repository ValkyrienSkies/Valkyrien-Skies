package ValkyrienWarfareControl.Piloting;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class SetShipPilotMessage implements IMessage{

	//If its just 0L and 0L then the player is no longer piloting a Ship
	public UUID entityUniqueID;

	public SetShipPilotMessage(){}

	public SetShipPilotMessage(UUID toSend){
		entityUniqueID = toSend;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		PacketBuffer packetBuffer = new PacketBuffer(buf);
		entityUniqueID = packetBuffer.readUuid();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		PacketBuffer packetBuffer = new PacketBuffer(buf);
		packetBuffer.writeUuid(entityUniqueID);
	}

}