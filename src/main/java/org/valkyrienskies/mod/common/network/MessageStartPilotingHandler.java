package org.valkyrienskies.mod.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.valkyrienskies.mod.common.piloting.IShipPilotClient;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;

import java.util.Optional;

public class MessageStartPilotingHandler implements
    IMessageHandler<MessageStartPiloting, IMessage> {

    @Override
    @SideOnly(Side.CLIENT)
    public IMessage onMessage(MessageStartPiloting message, MessageContext ctx) {
        IThreadListener mainThread = Minecraft.getMinecraft();
        mainThread.addScheduledTask(() -> {
            IShipPilotClient pilot = (IShipPilotClient) Minecraft.getMinecraft().player;

            pilot.setControllerInputEnum(message.controlType);

            if (message.posToStartPiloting != null) {
                pilot.setPosBeingControlled(message.posToStartPiloting);

                if (message.setPhysicsWrapperEntityToPilot) {
                    Optional<PhysicsObject> physicsObject = ValkyrienUtils
                            .getPhysoManagingBlock(Minecraft.getMinecraft().world, message.posToStartPiloting);
                    if (physicsObject.isPresent()) {
                        pilot.setPilotedShip(physicsObject.get());
                    } else {
                        new IllegalStateException("Received incorrect piloting message!")
                                .printStackTrace();
                    }
                } else {
                    pilot.setPilotedShip(null);
                }
            }

            if (message.shipPilotingId != null) pilot.setShipIDBeingControlled(message.shipPilotingId);
        });
        return null;
    }

}
