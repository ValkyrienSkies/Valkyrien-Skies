package valkyrienwarfare;

import valkyrienwarfare.command.AirshipMapCommand;
import valkyrienwarfare.command.AirshipSettingsCommand;
import valkyrienwarfare.command.PhysSettingsCommand;
import valkyrienwarfare.command.ValkyrienWarfareHelpCommand;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;

public class ModCommands {
	
	// There's some Strange bug with registering commands in the Mod File (The client loading server classes, and then freaking out). Best to just do them all in
	// a separate class
	public static void registerCommands(MinecraftServer server) {
		ServerCommandManager manager = (ServerCommandManager) server.getCommandManager();
		manager.registerCommand(new PhysSettingsCommand());
		manager.registerCommand(new AirshipSettingsCommand());
		manager.registerCommand(new AirshipMapCommand());
		manager.registerCommand(new ValkyrienWarfareHelpCommand());
	}
}
