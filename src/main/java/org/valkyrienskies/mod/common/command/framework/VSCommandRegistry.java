package org.valkyrienskies.mod.common.command.framework;

import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;
import org.valkyrienskies.mod.common.command.DebugCommand;
import org.valkyrienskies.mod.common.command.MainCommand;
import org.valkyrienskies.mod.common.command.PhysSettingsCommand;
import org.valkyrienskies.mod.common.command.config.VSConfigCommandBase;
import org.valkyrienskies.mod.common.config.VSConfig;

public class VSCommandRegistry {

    public static void registerCommands(MinecraftServer server) {
        ServerCommandManager manager = (ServerCommandManager) server.getCommandManager();
        manager.registerCommand(
            new VSConfigCommandBase("vsconfig", VSConfig.class, "vsc"));
        manager.registerCommand(new VSCommandBase<>(DebugCommand.class));
        manager.registerCommand(new VSCommandBase<>(MainCommand.class));
        manager.registerCommand(new PhysSettingsCommand());
    }

}
