package ValkyrienWarfareControl.Network;

import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class EntityFixMessageHandler implements IMessageHandler<EntityFixMessage, IMessage> {

	@Override
	public IMessage onMessage(final EntityFixMessage message, MessageContext ctx) {
		IThreadListener mainThread = Minecraft.getMinecraft();
		mainThread.addScheduledTask(new Runnable() {
			@Override
			public void run() {
				PhysicsWrapperEntity toFixOn = (PhysicsWrapperEntity) Minecraft.getMinecraft().theWorld.getEntityByID(message.shipId);
				if (toFixOn != null) {
					if (message.isFixing) {
						toFixOn.wrapping.fixEntityUUID(message.entityUUID, message.localPosition);
					} else {
						toFixOn.wrapping.removeEntityUUID(message.entityUUID);
					}
				}
			}
		});
		return null;
	}

}
