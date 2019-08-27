package org.valkyrienskies.mod.common.command.framework;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import org.apache.commons.io.output.NullOutputStream;
import org.valkyrienskies.mod.common.command.converters.WorldConverter;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class VWCommandBase<K> extends CommandBase {

    private Class<K> cmdClass;

    VWCommandBase(Class<K> cmdClass) {
        if (cmdClass.getAnnotation(Command.class) == null) {
            throw new IllegalArgumentException("Clazz must have the PicoCLI @Command annotation!");
        }

        this.cmdClass = cmdClass;
    }

    @Override
    public String getName() {
        return this.cmdClass.getAnnotation(Command.class).name();
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList(this.cmdClass.getAnnotation(Command.class).aliases());
    }

    @Override
    public String getUsage(ICommandSender sender) {
        VWCommandFactory factory = new VWCommandFactory(sender);
        CommandLine commandLine = new CommandLine(factory.create(cmdClass), factory);

        return commandLine.getUsageMessage();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        VWCommandFactory factory = new VWCommandFactory(sender);

        CommandLine commandLine = new CommandLine(factory.create(cmdClass), factory);
        commandLine.registerConverter(World.class, new WorldConverter());


        ChatWriter chatOut = new ChatWriter(sender);
        commandLine.setOut(chatOut);
        commandLine.setErr(chatOut);

        args = VWCommandUtil.toProperArgs(args);

        commandLine.execute(args);
    }

    static class ChatWriter extends PrintWriter {

        ICommandSender sender;

        ChatWriter(ICommandSender sender) {
            super(new NullOutputStream());
            this.sender = sender;
        }

        @Override
        public void print(String string) {
            string = string.replace("\r", "");
            sender.sendMessage(new TextComponentString(string));
        }

        @Override
		public void println(String string) {
        	this.print(string);
		}

    }

}
