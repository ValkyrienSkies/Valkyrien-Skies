package ValkyrienWarfareControl.Piloting;

import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PilotControlsMessageHandler implements IMessageHandler<PilotControlsMessage, IMessage> {

	@Override
	public IMessage onMessage(final PilotControlsMessage message, MessageContext ctx) {
		IThreadListener mainThread = ctx.getServerHandler().serverController;
		mainThread.addScheduledTask(new Runnable() {
			@Override
			public void run() {
				if (message.airshipBackward || message.airshipDown || message.airshipForward || message.airshipLeft || message.airshipRight || message.airshipUp) {
					 System.out.println("Got the keys");
				}
			}
		});

		return null;
	}

}
