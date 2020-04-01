package org.valkyrienskies.mod.common.network;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;

public class UnloadPhysObjMessageHandler implements IMessageHandler<UnloadPhysObjMessage, IMessage> {

    @Override
    public IMessage onMessage(UnloadPhysObjMessage message, MessageContext ctx) {
        // Tell the client ship world to load a ship for the message ship data.
        ValkyrienUtils.getPhysObjWorld(Minecraft.getMinecraft().world).queueShipUnload(message.toUnloadID);
        return null;
    }
}
