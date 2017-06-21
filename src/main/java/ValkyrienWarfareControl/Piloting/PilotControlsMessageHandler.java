package ValkyrienWarfareControl.Piloting;

import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.ValkyrienWarfareMod;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class PilotControlsMessageHandler implements IMessageHandler<PilotControlsMessage, IMessage> {

    @Override
    public IMessage onMessage(final PilotControlsMessage message, final MessageContext ctx) {
        IThreadListener mainThread = ctx.getServerHandler().serverController;
        mainThread.addScheduledTask(new Runnable() {
            @Override
            public void run() {
                World worldObj = ctx.getServerHandler().player.world;
                if (ValkyrienWarfareMod.physicsManager.getManagerForWorld(worldObj) != null) {
                    UUID shipId = message.shipFor;
                    for (PhysicsWrapperEntity entity : ValkyrienWarfareMod.physicsManager.getManagerForWorld(worldObj).physicsEntities) {
                        if (entity.getUniqueID().equals(shipId)) {
                            entity.wrapping.pilotingController.receivePilotControlsMessage(message, ctx.getServerHandler().player);
                        }
                    }
                }
            }
        });

        return null;
    }

}
