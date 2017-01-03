package ValkyrienWarfareBase;

import ValkyrienWarfareBase.Command.PhysSettingsCommand;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;

public class ExtraRegistry {

	public static void registerCommands(MinecraftServer server){
		ServerCommandManager manager = (ServerCommandManager)server.getCommandManager();
		manager.registerCommand(new PhysSettingsCommand());
	}
}
