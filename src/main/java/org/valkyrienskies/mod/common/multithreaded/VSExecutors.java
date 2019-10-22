package org.valkyrienskies.mod.common.multithreaded;

import java.util.concurrent.Executor;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.server.FMLServerHandler;

public class VSExecutors {

    public static final Executor SERVER =
        runnable -> FMLServerHandler.instance().getServer().addScheduledTask(runnable);
    public static final Executor CLIENT = FMLClientHandler.instance().getClient()::addScheduledTask;

    public static Executor forWorld(WorldServer world) {
        return world::addScheduledTask;
    }

}
