package org.valkyrienskies.mod.common.piloting;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;

public class PilotControlsMessageHandler implements
        IMessageHandler<PilotControlsMessage, IMessage> {

    @Override
    public IMessage onMessage(final PilotControlsMessage message, final MessageContext ctx) {
        IThreadListener mainThread = ctx.getServerHandler().server;
        mainThread.addScheduledTask(() -> {
            if (message.controlBlockPos != null) {
                World worldObj = ctx.getServerHandler().player.world;
                BlockPos posFor = message.controlBlockPos;
                TileEntity tile = worldObj.getTileEntity(posFor);

                if (tile instanceof ITileEntityPilotable) {
                    ((ITileEntityPilotable) tile)
                            .onPilotControlsMessage(message, ctx.getServerHandler().player);
                }
            } else {
                final PhysicsObject physicsObject = ValkyrienUtils.getPhysObjWorld(ctx.getServerHandler().player.world).getPhysObjectFromUUID(message.shipFor);
                if (physicsObject != null && physicsObject.getShipPilot() != null && physicsObject.getShipPilot().getPilot().equals(ctx.getServerHandler().player.getUniqueID())) {
                    physicsObject.getShipPilot().processControlMessage(message, ctx.getServerHandler().player);
                }
            }
        });
        return null;
    }

}
