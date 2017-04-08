package ValkyrienWarfareBase.Network;

import net.minecraft.client.Minecraft;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PlayerShipRefrenceHandler implements IMessageHandler<PlayerShipRefrenceMessage, IMessage> {

	@Override
	public IMessage onMessage(PlayerShipRefrenceMessage message, MessageContext ctx) {
		IThreadListener mainThread = Minecraft.getMinecraft();
		mainThread.addScheduledTask(new Runnable() {
			@Override
			public void run() {
				//Confirmed working as intended
//				System.out.println("Got the message");
			}
		});


		return null;
	}

}
