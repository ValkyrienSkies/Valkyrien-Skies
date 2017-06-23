package ValkyrienWarfareControl.Network;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareControl.Piloting.IShipPilotClient;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageStartPilotingHandler implements IMessageHandler<MessageStartPiloting, IMessage> {

	@Override
	public IMessage onMessage(MessageStartPiloting message, MessageContext ctx) {
		IThreadListener mainThread = Minecraft.getMinecraft();
        mainThread.addScheduledTask(new Runnable() {
            @Override
            public void run() {
            	IShipPilotClient pilot = IShipPilotClient.class.cast(Minecraft.getMinecraft().player);

            	pilot.setPosBeingControlled(message.posToStartPiloting);
            	pilot.setControllerInputEnum(message.controlType);

            	if(message.setPhysicsWrapperEntityToPilot) {
            		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(Minecraft.getMinecraft().world, message.posToStartPiloting);
            		pilot.setPilotedShip(wrapper);
            	}else{
            		pilot.setPilotedShip(null);
            	}
            }
        });
		return null;
	}

}
