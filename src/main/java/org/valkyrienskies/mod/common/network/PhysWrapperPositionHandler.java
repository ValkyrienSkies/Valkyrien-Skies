package org.valkyrienskies.mod.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.valkyrienskies.mod.common.entity.PhysicsWrapperEntity;

public class PhysWrapperPositionHandler implements
    IMessageHandler<WrapperPositionMessage, IMessage> {

    @Override
    public IMessage onMessage(final WrapperPositionMessage message, MessageContext ctx) {
        if (Minecraft.getMinecraft().player == null) {
            return null;
        }

        IThreadListener mainThread = Minecraft.getMinecraft();
        mainThread.addScheduledTask(new Runnable() {
            @Override
            public void run() {
                if (Minecraft.getMinecraft().world != null) {
                    Entity ent = Minecraft.getMinecraft().world
                        .getEntityByID(message.getEntityID());
                    if (ent instanceof PhysicsWrapperEntity) {
                        PhysicsWrapperEntity wrapper = (PhysicsWrapperEntity) ent;
                        wrapper.getPhysicsObject().getShipTransformationManager().serverBuffer
                            .pushMessage(message);
                    }
                }
            }
        });
        return null;
    }

}
