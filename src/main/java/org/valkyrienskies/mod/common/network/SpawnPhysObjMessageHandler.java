package org.valkyrienskies.mod.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;
import org.valkyrienskies.mod.common.physics.management.physo.ShipData;
import org.valkyrienskies.mod.common.physmanagement.shipdata.QueryableShipData;

public class SpawnPhysObjMessageHandler implements IMessageHandler<SpawnPhysObjMessage, IMessage> {

    @Override
    @SuppressWarnings("Convert2Lambda")
    // Why do you not use a lambda? Because lambdas are compiled and this causes NoClassDefFound
    // errors. DON'T USE A LAMBDA
    public IMessage onMessage(SpawnPhysObjMessage message, MessageContext ctx) {
        IThreadListener mainThread = Minecraft.getMinecraft();
        mainThread.addScheduledTask(new Runnable() {
            @Override
            public void run() {
                if (Minecraft.getMinecraft().world != null) {
                    // Spawn a PhysicsObject from the ShipData.
                    World world = Minecraft.getMinecraft().world;
                    QueryableShipData queryableShipData = QueryableShipData.get(world);
                    ShipData toSpawn = message.shipToSpawnData;
                    if (toSpawn.getPhyso() != null) {
                        throw new IllegalStateException("Wtf you can't spawn a ship twice!");
                    }
                    // Create a new PhysicsObject based on the ShipData.
                    queryableShipData.addOrUpdateShipPreservingPhysObj(toSpawn);
                    PhysicsObject physicsObject = new PhysicsObject(world, toSpawn, false);
                    toSpawn.setPhyso(physicsObject);
                }
            }
        });

        return null;
    }
}
