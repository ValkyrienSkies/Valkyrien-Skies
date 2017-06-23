package ValkyrienWarfareControl.Network;

import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareControl.Piloting.IShipPilot;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SetZepplinPilotMessageHandler implements IMessageHandler<SetZepplinPilotMessage, IMessage> {

	@Override
	public IMessage onMessage(SetZepplinPilotMessage message, MessageContext ctx) {
        IThreadListener mainThread = Minecraft.getMinecraft();
        mainThread.addScheduledTask(new Runnable() {
            @Override
            public void run() {
            	World world = Minecraft.getMinecraft().world;
            	EntityPlayer player = world.getPlayerEntityByUUID(message.newPilotID);
            	PhysicsWrapperEntity wrapper = null;
            	for (int i = 0; i < world.loadedEntityList.size(); ++i){
                    Entity entity = world.loadedEntityList.get(i);
                    if(entity.getUniqueID().equals(message.physicsObjectID)){
                    	wrapper = (PhysicsWrapperEntity) entity;
                    }
                }
            	if(wrapper != null) {
            		if(player == Minecraft.getMinecraft().player) {
            			IShipPilot shipPilot = (IShipPilot) player;

            			System.out.println("set the pilot ship");
            			shipPilot.setPilotedShip(wrapper);

            			Entity entity = shipPilot.getPilotedShip();

            			if(entity != null) {
            				System.out.println("SUCCESS!");
            			}
            		}
            	}
            }
        });

		return null;
	}

}
