package org.valkyrienskies.mod.common.util.multithreaded;

import java.util.concurrent.Executor;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.server.FMLServerHandler;
import org.valkyrienskies.mod.common.ships.ship_world.IHasShipManager;
import org.valkyrienskies.mod.common.ships.ship_world.WorldServerShipManager;

public class VSExecutors {

    public static final Executor SERVER =
        runnable -> FMLServerHandler.instance().getServer().addScheduledTask(runnable);
    public static final Executor CLIENT = FMLClientHandler.instance().getClient()::addScheduledTask;

    /**
     * Returns an executor to execute tasks on the physics thread of the selected world
     */
    public static Executor physics(WorldServer world) {
        return ((WorldServerShipManager) ((IHasShipManager) world).getManager())
            .getPhysicsThread()::addScheduledTask;
    }

    /**
     * Returns an executor to execute tasks on the thread of the selected world
     */
    public static Executor forWorld(WorldServer world) {
        return world::addScheduledTask;
    }

}
