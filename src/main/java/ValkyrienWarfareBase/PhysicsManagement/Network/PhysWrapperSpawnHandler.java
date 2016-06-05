package ValkyrienWarfareBase.PhysicsManagement.Network;

import net.minecraft.client.Minecraft;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PhysWrapperSpawnHandler implements IMessageHandler<PhysWrapperSpawnMessage, IMessage> {

	@Override
	public IMessage onMessage(final PhysWrapperSpawnMessage message, MessageContext ctx) {
		if(Minecraft.getMinecraft().thePlayer==null){
			return null;
		}
		IThreadListener mainThread = Minecraft.getMinecraft();
		mainThread.addScheduledTask(new Runnable(){
            @Override
            public void run(){
            	System.out.println("SPawned a physWrapper at "+ message.toSpawn.posX+":"+message.toSpawn.posY+":"+message.toSpawn.posZ);
            }
        });
		return null;
	}

}
