package org.valkyrienskies.addon.control.network;

import net.minecraft.client.Minecraft;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.valkyrienskies.addon.control.piloting.IShipPilotClient;

public class MessageStopPilotingHandler implements IMessageHandler<MessageStopPiloting, IMessage> {

    @Override
    public IMessage onMessage(MessageStopPiloting message, MessageContext ctx) {
        IThreadListener mainThread = Minecraft.getMinecraft();
        mainThread.addScheduledTask(() -> {
            IShipPilotClient pilot = (IShipPilotClient) Minecraft.getMinecraft().player;

            BlockPos posToStopPiloting = message.posToStopPiloting;

            if (pilot.getPosBeingControlled() != null && pilot.getPosBeingControlled()
                .equals(posToStopPiloting)) {
                pilot.stopPilotingEverything();
            }
        });
        return null;
    }

}
