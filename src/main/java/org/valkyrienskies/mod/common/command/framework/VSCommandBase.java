package org.valkyrienskies.mod.common.command.framework;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import lombok.SneakyThrows;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import org.apache.commons.io.output.NullOutputStream;
import org.valkyrienskies.mod.common.command.converters.WorldConverter;
import picocli.AutoComplete;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class VSCommandBase<K> extends CommandBase {

    private Class<K> cmdClass;
    private List<String> aliases;
    private String name;

    VSCommandBase(Class<K> cmdClass) {
        if (cmdClass.getAnnotation(Command.class) == null) {
            throw new IllegalArgumentException("Clazz must have the PicoCLI @Command annotation!");
        }

        this.cmdClass = cmdClass;
        this.name = this.cmdClass.getAnnotation(Command.class).name();
        this.aliases = Arrays.asList(this.cmdClass.getAnnotation(Command.class).aliases());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getAliases() {
        return aliases;
    }

    @SneakyThrows
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender,
        final String[] splitArgs, @Nullable BlockPos targetPos) {

        CommandSpec spec = CommandSpec.forAnnotatedObject(cmdClass.newInstance());
        String[] args = VSCommandUtil.toProperArgs(splitArgs);
        List<CharSequence> candidates = new ArrayList<>();

        if (args.length == 0) {
            AutoComplete.complete(spec, args, 0, 0, 500, candidates);
        } else {
            AutoComplete.complete(spec, args, args.length - 1,
                args[args.length - 1].length(), 500, candidates);
        }

        return candidates.stream()
            .distinct()
            .map(CharSequence::toString)
            .map(s -> args.length > 0 ? args[args.length - 1] + s : s)
            .collect(Collectors.toList());
    }

    @Override
    public String getUsage(ICommandSender sender) {
        VSCommandFactory factory = new VSCommandFactory(sender);
        CommandLine commandLine = new CommandLine(factory.create(cmdClass), factory);

        return commandLine.getUsageMessage();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        VSCommandFactory factory = new VSCommandFactory(sender);

        CommandLine commandLine = new CommandLine(factory.create(cmdClass), factory);
        commandLine.registerConverter(World.class, new WorldConverter());

        ChatWriter chatOut = new ChatWriter(sender);
        commandLine.setOut(chatOut);
        commandLine.setErr(chatOut);

        args = VSCommandUtil.toProperArgs(args);
        commandLine.execute(args);
    }

    static class ChatWriter extends PrintWriter {

        ICommandSender sender;

        ChatWriter(ICommandSender sender) {
            super(new NullOutputStream());
            this.sender = sender;
        }

        @Override
        public void print(Object object) {
            this.print(object.toString());
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
