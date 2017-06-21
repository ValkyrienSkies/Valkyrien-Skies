package ValkyrienWarfareBase.Network;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PlayerShipRefrenceHandler implements IMessageHandler<PlayerShipRefrenceMessage, IMessage> {

    @Override
    public IMessage onMessage(PlayerShipRefrenceMessage message, MessageContext ctx) {
        //This seems to be being called on the server!!!
        /*IThreadListener mainThread = Minecraft.getMinecraft();
		mainThread.addScheduledTask(new Runnable() {
			@Override
			public void run() {
				//Confirmed working as intended
//				System.out.println("Got the message");
			}
		});*/
        return null;
    }

}
