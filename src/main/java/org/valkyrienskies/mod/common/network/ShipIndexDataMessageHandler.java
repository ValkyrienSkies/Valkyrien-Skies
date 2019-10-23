package org.valkyrienskies.mod.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.valkyrienskies.mod.common.physics.management.physo.ShipData;
import org.valkyrienskies.mod.common.physmanagement.shipdata.QueryableShipData;

public class ShipIndexDataMessageHandler implements IMessageHandler<ShipIndexDataMessage, IMessage> {

    @Override
    public IMessage onMessage(ShipIndexDataMessage message, MessageContext ctx) {
        IThreadListener mainThread = Minecraft.getMinecraft();
        mainThread.addScheduledTask(() -> {
            if (Minecraft.getMinecraft().world != null) {
                World world = Minecraft.getMinecraft().world;
                // IPhysObjectWorld physObjectWorld = ((IHasShipManager) world).getManager();
                QueryableShipData worldData = QueryableShipData.get(world);
                for (ShipData shipData : message.indexedData) {
                    worldData.addOrUpdateShipPreservingPhysObj(shipData);
                }
            }
        });

        System.out.println("Receiving message with length " + message.indexedData.size());
        // TODO: does nothing
        return null;
    }
}
