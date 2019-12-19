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
    @SuppressWarnings("Convert2Lambda")
    // Why do you not use a lambda? Because lambdas are compiled and this causes NoClassDefFound
    // errors. DON'T USE A LAMBDA
    public IMessage onMessage(ShipIndexDataMessage message, MessageContext ctx) {
        IThreadListener mainThread = Minecraft.getMinecraft();
        mainThread.addScheduledTask(new Runnable() {
            @Override
            public void run() {
                if (Minecraft.getMinecraft().world != null) {
                    World world = Minecraft.getMinecraft().world;
                    // IPhysObjectWorld physObjectWorld = ((IHasShipManager) world).getManager();
                    QueryableShipData worldData = QueryableShipData.get(world);
                    for (ShipData shipData : message.indexedData) {
                        worldData.addOrUpdateShipPreservingPhysObj(shipData);
                    }
                }
            }
        });

        // TODO: does nothing
        return null;
    }
}
