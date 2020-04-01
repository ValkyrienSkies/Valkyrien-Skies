package org.valkyrienskies.mod.common.network;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;

public class SpawnPhysObjMessageHandler implements IMessageHandler<SpawnPhysObjMessage, IMessage> {

    @Override
    public IMessage onMessage(SpawnPhysObjMessage message, MessageContext ctx) {
        // Tell the client ship world to load a ship for the message ship data.
        ValkyrienUtils.getPhysObjWorld(Minecraft.getMinecraft().world).queueShipLoad(message.shipToSpawnID);
        return null;
    }
}
