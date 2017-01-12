package ValkyrienWarfareBase;

import ValkyrienWarfareBase.Command.AirshipSettingsCommand;
import ValkyrienWarfareBase.Command.PhysSettingsCommand;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;

public class ExtraRegistry {

	// There's some Strange bug with registering commands in the Mod File (The client loading server classes, and then freaking out). Best to just do them all in
	// a seperate class
	public static void registerCommands(MinecraftServer server) {
		ServerCommandManager manager = (ServerCommandManager) server.getCommandManager();
		manager.registerCommand(new PhysSettingsCommand());
		manager.registerCommand(new AirshipSettingsCommand());
	}
}
