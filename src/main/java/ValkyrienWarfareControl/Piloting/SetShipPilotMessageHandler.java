package ValkyrienWarfareControl.Piloting;

import java.util.UUID;

import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SetShipPilotMessageHandler implements IMessageHandler<SetShipPilotMessage, IMessage>{

	@Override
	public IMessage onMessage(final SetShipPilotMessage message, final MessageContext ctx) {
		IThreadListener mainThread = Minecraft.getMinecraft();
		mainThread.addScheduledTask(new Runnable() {
			@Override
			public void run() {
				UUID entityId = message.entityUniqueID;
				if(entityId.getLeastSignificantBits() == 0L && entityId.getMostSignificantBits() == 0L){
					ClientPilotingManager.setPilotedWrapperEntity(null);
//					System.out.println("got set to null");
				}else{
					Entity foundEntity = null;
					for(Entity entity:Minecraft.getMinecraft().theWorld.getLoadedEntityList()){
						if(entity.entityUniqueID.equals(entityId)){
							ClientPilotingManager.setPilotedWrapperEntity((PhysicsWrapperEntity) entity);
//							System.out.println("Found the Pilot on client side");
							foundEntity = entity;
						}
					}
					if(foundEntity == null){
						ClientPilotingManager.setPilotedWrapperEntity(null);
					}
				}
			}
		});
		
		return null;
	}

}
