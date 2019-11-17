# Valkyrien Skies Commands

Read more: https://picocli.info/

## How to: Create a command `/example`:

1. Make a new class `ExampleCommand`
    ```java
    @Command(name = "example", aliases = "ex",
        synopsisSubcommandLabel = "COMMAND")
    public class ExampleCommand implements Runnable {
        @Inject
        ICommandSender sender;
    
        @Override
        public void run() {
            sender.sendMessage(new TextComponentString("Hello!"));
        }
    }
    ```
2. Add to the `VSModCommandRegistry`
    ```
    public static void registerCommands(MinecraftServer server) {
        ...
        manager.registerCommand(new VSCommandBase<>(ExampleCommand.class));
    }
    ```

## How to: Create a command `/example <message> [-t TARGET]`

```java
@Command(name = "example", aliases = "ex",
    synopsisSubcommandLabel = "COMMAND")
public class ExampleCommand implements Runnable {
    @Inject
    ICommandSender sender;
    
    @Parameters
    String message;

    @Option(names = "t")
    String target;

    @Override
    public void run() {
        if (target != null) {
            sender.getEntityWorld().getPlayerEntityByName(target)
                .sendMessage(new TextComponentString(message));
        } else {
            sender.sendMessage(new TextComponentString(message));
        }
    }
}
```

Expected behaviour:

- `/example hello there -t Notch` should send "hello there" to a player named Notch
- `/example hello myself` should send "hello myself" to the player sending the command.

