package valkyrienwarfare.mod.common.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import valkyrienwarfare.mod.common.physmanagement.interaction.QueryableShipData;
import valkyrienwarfare.mod.common.physmanagement.interaction.ShipData;

import java.util.stream.Collectors;

@Command(name = "valkyrienwarfare", aliases = "ls",
        subcommands = {
                HelpCommand.class,
                VWCommand.ListShips.class})

public class VWCommand {

    @Command(name = "list-ships", aliases = "ls")
    static class ListShips implements Runnable {

        ICommandSender sender;

        ListShips(ICommandSender sender) {
            this.sender = sender;
        }

        public void run() {
            World world = sender.getEntityWorld();
            QueryableShipData data = QueryableShipData.get(world);

            if (data.getShips().size() == 0) {
                // There are no ships
                sender.sendMessage(new TextComponentTranslation(
                        "commands.valkyrienwarfare.list-ships.noships"));
                return;
            }

            String listOfShips = data.getShips()
                    .stream()
                    .map(ShipData::getName)
                    .collect(Collectors.joining(",\n"));

            sender.sendMessage(new TextComponentTranslation(
                    "commands.valkyrienwarfare.list-ships.ships", listOfShips));
        }

    }

}
