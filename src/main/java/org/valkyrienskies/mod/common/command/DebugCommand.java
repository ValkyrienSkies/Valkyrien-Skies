package org.valkyrienskies.mod.common.command;

import java.util.stream.Collectors;
import javax.inject.Inject;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.command.DebugCommand.GetClientPhysicsObjects;
import org.valkyrienskies.mod.common.physmanagement.shipdata.QueryableShipData;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Model;
import picocli.CommandLine.Spec;


@Command(name = "vsdebug", aliases = {"vsd", "valkyrienskiesdebug"},
    synopsisSubcommandLabel = "COMMAND", mixinStandardHelpOptions = true,
    usageHelpWidth = 55,
    subcommands = {
        HelpCommand.class, GetClientPhysicsObjects.class
    })
public class DebugCommand implements Runnable {

    @Spec
    private Model.CommandSpec spec;

    @Inject
    private ICommandSender sender;

    @Override
    public void run() {
        String usageMessage = spec.commandLine().getUsageMessage()
            .replace("\r", "");

        sender.sendMessage(new TextComponentString(usageMessage));
    }

    @Command(name = "list-client-ships")
    static class GetClientPhysicsObjects implements Runnable {

        @Inject
        private ICommandSender sender;

        @Override
        public void run() {
            World world = Minecraft.getMinecraft().world;
            QueryableShipData data = ValkyrienUtils.getQueryableData(world);

            if (data.getShips().size() == 0) {
                // There are no ships
                sender.sendMessage(new TextComponentTranslation(
                    "commands.vs.list-ships.noships"));
                return;
            }

            String listOfShips = data.getShips()
                .stream()
                .map(shipData -> {
                    if (shipData.getShipTransform() == null) {
                        // Unknown Location (this should be an error? TODO: look into this)
                        return String.format("%s, Unknown Location", shipData.getName());
                    } else {
                        // Known Location
                        return String.format("%s [%.1f, %.1f, %.1f]", shipData.getName(),
                            shipData.getShipTransform().getPosX(),
                            shipData.getShipTransform().getPosY(),
                            shipData.getShipTransform().getPosZ());
                    }
                })
                .collect(Collectors.joining(",\n"));

            sender.sendMessage(new TextComponentTranslation(
                "commands.vs.list-ships.ships", listOfShips));
        }
    }

}
