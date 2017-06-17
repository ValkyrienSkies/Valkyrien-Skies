package ValkyrienWarfareBase.Command;

import java.util.ArrayList;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class ValkyrienWarfareHelpCommand extends CommandBase {

	public static final ArrayList<String> commands = new ArrayList<String>();

	static{
		commands.add("/physSettings");
		commands.add("/airshipSettings");
		commands.add("/airshipMappings");
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "/VW";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		// TODO Auto-generated method stub
		return "/VW       See entire list of commands for Valkyrien Warfare";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		sender.sendMessage(new TextComponentString("All ValkyrienWarfare Commands"));

		for(String command : commands){
			sender.sendMessage(new TextComponentString(command));
		}

		sender.sendMessage(new TextComponentString("To see avaliable subcommands, type /Command help"));
	}

}
