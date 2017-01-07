package ValkyrienWarfareControl.Piloting;

import java.util.UUID;

import ValkyrienWarfareBase.KeyHandler;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PilotControlsMessage implements IMessage {

	public boolean airshipUp;
	public boolean airshipDown;
	public boolean airshipForward;
	public boolean airshipBackward;
	public boolean airshipLeft;
	public boolean airshipRight;
	public UUID shipFor;

	public PilotControlsMessage() {
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		PacketBuffer packetBuf = new PacketBuffer(buf);
		airshipUp = packetBuf.readBoolean();
		airshipDown = packetBuf.readBoolean();
		airshipForward = packetBuf.readBoolean();
		airshipBackward = packetBuf.readBoolean();
		airshipLeft = packetBuf.readBoolean();
		airshipRight = packetBuf.readBoolean();
		shipFor = packetBuf.readUuid();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		PacketBuffer packetBuf = new PacketBuffer(buf);
		packetBuf.writeBoolean(airshipUp);
		packetBuf.writeBoolean(airshipDown);
		packetBuf.writeBoolean(airshipForward);
		packetBuf.writeBoolean(airshipBackward);
		packetBuf.writeBoolean(airshipLeft);
		packetBuf.writeBoolean(airshipRight);
		packetBuf.writeUuid(shipFor);
	}

	public void assignKeyBooleans(PhysicsWrapperEntity shipPiloting) {
		airshipUp = KeyHandler.airshipUp.isKeyDown();
		airshipDown = KeyHandler.airshipDown.isKeyDown();
		airshipForward = KeyHandler.airshipForward.isKeyDown();
		airshipBackward = KeyHandler.airshipBackward.isKeyDown();
		airshipLeft = KeyHandler.airshipLeft.isKeyDown();
		airshipRight = KeyHandler.airshipRight.isKeyDown();
		shipFor = shipPiloting.getUniqueID();
	}

}
