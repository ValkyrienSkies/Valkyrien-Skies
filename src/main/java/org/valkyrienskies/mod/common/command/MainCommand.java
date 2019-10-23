package org.valkyrienskies.mod.common.command;

import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.command.MainCommand.TeleportTo;
import org.valkyrienskies.mod.common.command.autocompleters.ShipNameAutocompleter;
import org.valkyrienskies.mod.common.command.autocompleters.WorldAutocompleter;
import org.valkyrienskies.mod.common.coordinates.ShipTransform;
import org.valkyrienskies.mod.common.multithreaded.VSThread;
import org.valkyrienskies.mod.common.physics.management.physo.ShipData;
import org.valkyrienskies.mod.common.physmanagement.shipdata.QueryableShipData;
import org.valkyrienskies.mod.common.ship_handling.IHasShipManager;
import org.valkyrienskies.mod.common.ship_handling.WorldServerShipManager;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Model;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(name = "valkyrienskies", aliases = "vs",
    synopsisSubcommandLabel = "COMMAND", mixinStandardHelpOptions = true,
    usageHelpWidth = 55,
    subcommands = {
        HelpCommand.class,
        MainCommand.ListShips.class,
        MainCommand.DisableShip.class,
        MainCommand.GC.class,
        MainCommand.TPS.class,
        TeleportTo.class})
public class MainCommand implements Runnable {

    @Spec
    private Model.CommandSpec spec;

    @Inject
    private ICommandSender sender;

    @Override
    public void run() {
        String usageMessage = spec.commandLine().getUsageMessage().replace("\r", "");

        sender.sendMessage(new TextComponentString(usageMessage));
    }

    @Command(name = "teleport-to", aliases = "tpto")
    static class TeleportTo implements Runnable {

        @Inject
        ICommandSender sender;

        @Parameters(index = "0", completionCandidates = ShipNameAutocompleter.class)
        String shipName;

        public void run() {
            if (!(sender instanceof EntityPlayer)) {
                sender.sendMessage(new TextComponentString("You must execute this command as "
                    + "a player!"));
            }

            World world = sender.getEntityWorld();
            QueryableShipData data = QueryableShipData.get(world);
            Optional<ShipData> oTargetShipData = data.getShipFromName(shipName);

            if (!oTargetShipData.isPresent()) {
                sender.sendMessage(new TextComponentString(
                    "That ship, " + shipName + " could not be found"));
                return;
            }

            ShipTransform pos = oTargetShipData.get().getShipTransform();
            ((EntityPlayer) sender).setPositionAndUpdate(pos.getPosX(), pos.getPosY(), pos.getPosZ());
        }

    }

    @Command(name = "gc")
    static class GC implements Runnable {

        @Inject
        ICommandSender sender;

        public void run() {
            System.gc();
            sender.sendMessage(new TextComponentString("Successfully garbage collected"));
        }

    }

    @Command(name = "tps")
    static class TPS implements Runnable {

        @Inject
        ICommandSender sender;

        @Option(names = {"--world", "-w"}, completionCandidates = WorldAutocompleter.class)
        World world;

        @Override
        public void run() {
            if (world == null) {
                world = sender.getEntityWorld();
            }

            VSThread worldPhysicsThread = ((WorldServerShipManager) ((IHasShipManager) world)
                .getManager()).getPhysicsThread();

            if (worldPhysicsThread != null) {
                long averagePhysTickTimeNano = worldPhysicsThread.getAveragePhysicsTickTimeNano();
                double ticksPerSecond = 1000000000D / ((double) averagePhysTickTimeNano);
                double ticksPerSecondTwoDecimals = Math.floor(ticksPerSecond * 100) / 100;
                sender.sendMessage(new TextComponentString(
                    "Player world: " + ticksPerSecondTwoDecimals + " physics ticks per second"));
            }
        }
    }

    @Command(name = "ship-physics")
    static class DisableShip implements Runnable {

        @Inject
        ICommandSender sender;

        @Spec
        CommandSpec spec;

        @Parameters(index = "0", completionCandidates = ShipNameAutocompleter.class)
        String shipName;

        @Parameters(index = "1", arity = "0..1")
        boolean enabled;

        @Override
        public void run() {
            World world = sender.getEntityWorld();
            QueryableShipData data = QueryableShipData.get(world);
            Optional<ShipData> oTargetShipData = data.getShipFromName(shipName);

            if (!oTargetShipData.isPresent()) {
                sender.sendMessage(new TextComponentString(
                    "That ship, " + shipName + " could not be found"));
                return;
            }

            ShipData targetShipData = oTargetShipData.get();

            /*
            Optional<Entity> oEntity = world.getLoadedEntityList().stream()
                .filter(e -> e.getPersistentID().equals(targetShipData.getUuid()))
                .findFirst();

            if (!oEntity.isPresent()) {
                throw new RuntimeException("QueryableShipData is incorrect?");
            }

            try {
                PhysicsWrapperEntity wrapperEntity = (PhysicsWrapperEntity) oEntity.get();
                BlockPos infuserPos = wrapperEntity.getPhysicsObject().getPhysicsInfuserPos();
                TileEntityPhysicsInfuser infuser = Objects.requireNonNull(
                    (TileEntityPhysicsInfuser) world.getTileEntity(infuserPos));

                if (spec.commandLine().getParseResult().hasMatchedPositional(1)) {
                    infuser.isPhysicsEnabled(enabled);
                    sender.sendMessage(new TextComponentString(
                        "Successfully set the physics of ship " + shipName + " to " +
                            (infuser.isPhysicsEnabled() ? "enabled" : "disabled")
                    ));
                } else {
                    sender.sendMessage(new TextComponentString(
                        "The physics of the ship " + shipName + " is " +
                            (infuser.isPhysicsEnabled() ? "enabled" : "disabled")
                    ));
                }

            } catch (ClassCastException e) {
                throw new RuntimeException("Ship entity is not PhysicsWrapperEntity or "
                    + "Physics infuser is not a physics infuser?", e);
            }

             */
        }
    }

    @Command(name = "list-ships", aliases = "ls")
    static class ListShips implements Runnable {

        @Inject
        ICommandSender sender;

        @Option(names = {"-v", "--verbose"})
        boolean verbose;

        @Override
        public void run() {
            World world = sender.getEntityWorld();
            QueryableShipData data = ValkyrienUtils.getQueryableData(world);

            if (data.getShips().size() == 0) {
                // There are no ships
                sender.sendMessage(new TextComponentTranslation(
                    "commands.valkyrienskies.list-ships.noships"));
                return;
            }

            String listOfShips;

            if (verbose) {
                listOfShips = data.getShips()
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
            } else {
                listOfShips = data.getShips()
                    .stream()
                    .map(ShipData::getName)
                    .collect(Collectors.joining(",\n"));
            }

            sender.sendMessage(new TextComponentTranslation(
                "commands.valkyrienskies.list-ships.ships", listOfShips));
        }

    }

}
