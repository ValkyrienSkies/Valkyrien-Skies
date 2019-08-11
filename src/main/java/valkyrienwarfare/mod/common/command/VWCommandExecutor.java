package valkyrienwarfare.mod.common.command;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import org.apache.commons.io.output.NullOutputStream;
import picocli.CommandLine;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.PrintWriter;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class VWCommandExecutor extends CommandBase {

    private static final String COMMAND_NAME = "vw";

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        VWCommand command = new VWCommand();
        VWCommandFactory factory = new VWCommandFactory(sender);
        CommandLine commandLine = new CommandLine(command, factory);

        ChatWriter chatOut = new ChatWriter(sender);
        commandLine.setOut(chatOut);
        commandLine.setErr(chatOut);

        args = VWCommandUtil.toProperArgs(args);

        commandLine.execute(args);
    }

    class ChatWriter extends PrintWriter {

        ICommandSender sender;

        ChatWriter(ICommandSender sender) {
            super(new NullOutputStream());
            this.sender = sender;
        }

        @Override
        public void print(String string) {
            sender.sendMessage(new TextComponentString(string));
        }
    }

}
