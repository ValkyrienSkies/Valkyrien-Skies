package org.valkyrienskies.addon.control.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.valkyrienskies.addon.control.piloting.ITileEntityPilotable;

public class MessagePlayerStoppedPilotingHandler implements
    IMessageHandler<MessagePlayerStoppedPiloting, IMessage> {

    @Override
    public IMessage onMessage(MessagePlayerStoppedPiloting message, MessageContext ctx) {
        IThreadListener mainThread = ctx.getServerHandler().server;
        mainThread.addScheduledTask(() -> {
                BlockPos pos = message.posToStopPiloting;
                EntityPlayerMP player = ctx.getServerHandler().player;

                TileEntity tileEntity = player.world.getTileEntity(pos);

                if (tileEntity instanceof ITileEntityPilotable) {
                    ((ITileEntityPilotable) tileEntity).playerWantsToStopPiloting(player);
                }
            }
        );
        return null;
    }

}
