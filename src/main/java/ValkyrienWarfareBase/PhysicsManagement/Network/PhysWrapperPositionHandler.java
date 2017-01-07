package ValkyrienWarfareBase.PhysicsManagement.Network;

import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PhysWrapperPositionHandler implements IMessageHandler<PhysWrapperPositionMessage, IMessage> {

	@Override
	public IMessage onMessage(final PhysWrapperPositionMessage message, MessageContext ctx) {
		if (Minecraft.getMinecraft().thePlayer == null) {
			return null;
		}

		IThreadListener mainThread = Minecraft.getMinecraft();
		mainThread.addScheduledTask(new Runnable() {
			@Override
			public void run() {
				Entity ent = Minecraft.getMinecraft().theWorld.getEntityByID(message.entityID);
				if (ent != null && ent instanceof PhysicsWrapperEntity) {
					PhysicsWrapperEntity wrapper = (PhysicsWrapperEntity) ent;

					wrapper.wrapping.coordTransform.stack.pushMessage(message);

					// wrapper.wrapping.centerCoord = message.centerOfMass;
					//
					// wrapper.posX = message.posX;
					// wrapper.posY = message.posY;
					// wrapper.posZ = message.posZ;
					//
					// wrapper.pitch = message.pitch;
					// wrapper.yaw = message.yaw;
					// wrapper.roll = message.roll;
					//
					// wrapper.wrapping.coordTransform.updateAllTransforms();
				}
			}
		});
		return null;
	}

}
