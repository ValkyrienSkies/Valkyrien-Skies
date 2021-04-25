package org.valkyrienskies.mod.common.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.valkyrienskies.mod.common.piloting.ITileEntityPilotable;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;

import java.util.UUID;

public class MessagePlayerStoppedPilotingHandler implements
    IMessageHandler<MessagePlayerStoppedPiloting, IMessage> {

    @Override
    public IMessage onMessage(MessagePlayerStoppedPiloting message, MessageContext ctx) {
        IThreadListener mainThread = ctx.getServerHandler().server;
        mainThread.addScheduledTask(() -> {
                    if (message.posToStopPiloting != null) {
                        BlockPos pos = message.posToStopPiloting;
                        EntityPlayerMP player = ctx.getServerHandler().player;

                        TileEntity tileEntity = player.world.getTileEntity(pos);

                        if (tileEntity instanceof ITileEntityPilotable) {
                            ((ITileEntityPilotable) tileEntity).playerWantsToStopPiloting(player);
                        }
                    } else {
                        final UUID shipID = message.shipIDToStopPiloting;
                        final PhysicsObject physicsObject = ValkyrienUtils.getPhysObjWorld(ctx.getServerHandler().player.world).getPhysObjectFromUUID(shipID);
                        if (physicsObject != null && physicsObject.getShipPilot() != null && ctx.getServerHandler().player.getUniqueID().equals(physicsObject.getShipPilot().getPilot())) {
                            physicsObject.setShipPilot(null);
                        }
                    }
                }
        );
        return null;
    }

}
