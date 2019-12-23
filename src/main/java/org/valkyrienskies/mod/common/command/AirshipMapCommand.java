package org.valkyrienskies.mod.common.command;

import java.util.Optional;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import org.valkyrienskies.mod.common.physmanagement.shipdata.ShipData;
import org.valkyrienskies.mod.common.physmanagement.shipdata.ShipPositionData;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AirshipMapCommand extends CommandBase {

    @Override
    public String getName() {
        return "airshipmapping";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/airshipmapping tpto <Ship Name>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args)
        throws CommandException {
        String term = args[0];

        if (term.equals("tpto")) {
            StringBuilder shipName = new StringBuilder(args.length < 2 ? "" : args[1]);
            if (args.length > 2) {
                for (int i = 2; i < args.length; i++) {
                    shipName.append(" ").append(args[i]);
                }
            }

            Entity player = sender.getCommandSenderEntity();
            if (!(player instanceof EntityPlayer)) {
                return;
            }

            Optional<ShipData> shipDataOptional = ValkyrienUtils.getQueryableData(player.world)
                .getShipFromName(shipName.toString());

            if (shipDataOptional.isPresent()) {
                ShipPositionData positionData = shipDataOptional.get().getPositionData();
                double posX = positionData.getPosX();
                double posY = positionData.getPosY();
                double posZ = positionData.getPosZ();

                // Time to teleport!
                if (player instanceof EntityPlayerMP) {
                    EntityPlayerMP playerMP = (EntityPlayerMP) player;

                    ((EntityPlayerMP) player).connection.setPlayerLocation(posX, posY, posZ, 0, 0);
                }
            } else {
                sender.sendMessage(new TextComponentString("That's not a valid ship!"));
            }
        }

        if (term.equals("help")) {
            sender.sendMessage(new TextComponentString("tpto"));
        }
    }

}
