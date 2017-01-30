package ValkyrienWarfareControl.Piloting;

import java.util.UUID;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PilotControlsMessageHandler implements IMessageHandler<PilotControlsMessage, IMessage> {

	@Override
	public IMessage onMessage(final PilotControlsMessage message, final MessageContext ctx) {
		IThreadListener mainThread = ctx.getServerHandler().serverController;
		mainThread.addScheduledTask(new Runnable() {
			@Override
			public void run() {
				World worldObj = ctx.getServerHandler().playerEntity.worldObj;
				if(ValkyrienWarfareMod.physicsManager.getManagerForWorld(worldObj) != null){
					UUID shipId = message.shipFor;
					for(PhysicsWrapperEntity entity:ValkyrienWarfareMod.physicsManager.getManagerForWorld(worldObj).physicsEntities){
						if(entity.getUniqueID().equals(shipId)){
							entity.wrapping.pilotingController.receivePilotControlsMessage(message, ctx.getServerHandler().playerEntity);
						}
					}
				}
			}
		});

		return null;
	}

}
