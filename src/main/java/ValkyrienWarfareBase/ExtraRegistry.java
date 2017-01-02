package ValkyrienWarfareBase;

import ValkyrienWarfareBase.Command.PhysConstructionLimitCommand;
import ValkyrienWarfareBase.Command.PhysSplittingToggleCommand;
import ValkyrienWarfareBase.Command.PhysicsGravityCommand;
import ValkyrienWarfareBase.Command.PhysicsIterCommand;
import ValkyrienWarfareBase.Command.PhysicsSpeedCommand;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;

public class ExtraRegistry {

	public static void registerCommands(MinecraftServer server){
		ServerCommandManager manager = (ServerCommandManager)server.getCommandManager();
		manager.registerCommand(new PhysicsSpeedCommand());
		manager.registerCommand(new PhysicsIterCommand());
		manager.registerCommand(new PhysicsGravityCommand());
		manager.registerCommand(new PhysConstructionLimitCommand());
		manager.registerCommand(new PhysSplittingToggleCommand());
	}
}
