package valkyrienwarfare.network;

import valkyrienwarfare.api.Vector;
import valkyrienwarfare.physicsmanagement.PhysicsWrapperEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class EntityRelativePositionMessageHandler implements IMessageHandler<EntityRelativePositionMessage, IMessage> {
	
	@Override
	public IMessage onMessage(EntityRelativePositionMessage message, MessageContext ctx) {
		if (Minecraft.getMinecraft().player == null) {
			return null;
		}
		
		IThreadListener mainThread = Minecraft.getMinecraft();
		mainThread.addScheduledTask(new Runnable() {
			@Override
			public void run() {
				Entity ent = Minecraft.getMinecraft().world.getEntityByID(message.wrapperEntityId);
				if (ent != null && ent instanceof PhysicsWrapperEntity) {
					PhysicsWrapperEntity wrapper = (PhysicsWrapperEntity) ent;
					double[] lToWTransform = wrapper.wrapping.coordTransform.lToWTransform;
					
					for (int i = 0; i < message.listSize; i++) {
						int entityID = message.entitiesToSendIDs.get(i);
						Vector entityPosition = message.entitiesRelativePosition.get(i);
						
						Entity entity = Minecraft.getMinecraft().world.getEntityByID(entityID);
						
						if (entity != null && entity != Minecraft.getMinecraft().player) {
//                    		System.out.println("worked");
							entityPosition.transform(lToWTransform);

//                    		entity.setPosition(entityPosition.X, entityPosition.Y, entityPosition.Z);
						}
					}
				}
			}
		});
		return null;
	}
	
}
