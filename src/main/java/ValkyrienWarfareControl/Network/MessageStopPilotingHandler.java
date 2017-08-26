package ValkyrienWarfareControl.Network;

import ValkyrienWarfareControl.Piloting.IShipPilotClient;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageStopPilotingHandler implements IMessageHandler<MessageStopPiloting, IMessage> {

	@Override
	public IMessage onMessage(MessageStopPiloting message, MessageContext ctx) {
		IThreadListener mainThread = Minecraft.getMinecraft();
		mainThread.addScheduledTask(new Runnable() {
			@Override
			public void run() {
				IShipPilotClient pilot = IShipPilotClient.class.cast(Minecraft.getMinecraft().player);

				BlockPos posToStopPiloting = message.posToStopPiloting;

				if (pilot.getPosBeingControlled() != null && pilot.getPosBeingControlled().equals(posToStopPiloting)) {
					pilot.stopPilotingEverything();
				} else {
					//Wtf is this?
				}
			}
		});
		return null;
	}

}
