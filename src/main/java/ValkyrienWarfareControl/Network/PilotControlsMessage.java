package ValkyrienWarfareControl.Network;

import ValkyrienWarfareBase.KeyHandler;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PilotControlsMessage implements IMessage{

	public boolean airshipUp;
	public boolean airshipDown;
	public boolean airshipForward;
	public boolean airshipBackward;
	public boolean airshipLeft;
	public boolean airshipRight;
	
	public PilotControlsMessage(){}

	@Override
	public void fromBytes(ByteBuf buf) {
		airshipUp = buf.readBoolean();
		airshipDown = buf.readBoolean();
		airshipForward = buf.readBoolean();
		airshipBackward = buf.readBoolean();
		airshipLeft = buf.readBoolean();
		airshipRight = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(airshipUp);
		buf.writeBoolean(airshipDown);
		buf.writeBoolean(airshipForward);
		buf.writeBoolean(airshipBackward);
		buf.writeBoolean(airshipLeft);
		buf.writeBoolean(airshipRight);
	}
	
	public void assignKeyBooleans(){
		airshipUp = KeyHandler.airshipUp.isKeyDown();
		airshipDown = KeyHandler.airshipDown.isKeyDown();
		airshipForward = KeyHandler.airshipForward.isKeyDown();
		airshipBackward = KeyHandler.airshipBackward.isKeyDown();
		airshipLeft = KeyHandler.airshipLeft.isKeyDown();
		airshipRight = KeyHandler.airshipRight.isKeyDown();
		
	}

}
