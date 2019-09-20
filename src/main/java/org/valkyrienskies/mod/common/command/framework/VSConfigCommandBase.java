package org.valkyrienskies.mod.common.command.framework;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class VSConfigCommandBase<K> extends CommandBase {

    private String name;

    public VSConfigCommandBase(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return null; // TODO implement
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {

    }
}
