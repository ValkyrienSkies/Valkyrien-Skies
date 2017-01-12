package ValkyrienWarfareBase.Command;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;

public class AirshipSettingsCommand extends CommandBase {

	@Override
	public String getCommandName() {
		return "airshipSettings";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/airshipSettings <setting name> [value]";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (!(sender instanceof EntityPlayer)) {
			sender.addChatMessage(new TextComponentString("You need to be a player to do that!"));
			return;
		}

		EntityPlayer p = (EntityPlayer) sender;
		BlockPos pos = p.rayTrace(4.5, 1).getBlockPos();
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(p.getEntityWorld(), pos);

		if (wrapper == null) {
			sender.addChatMessage(new TextComponentString("You need to be looking at an airship to do that!"));
			return;
		}
		if (p.entityUniqueID.toString().equals(wrapper.wrapping.creator)) {
			if (args[0].equals("transfer")) {
				if (args[1] != null && !args[1].isEmpty()) {
					EntityPlayer target = server.getPlayerList().getPlayerByUsername(args[1]);
					if (target == null) {
						p.addChatMessage(new TextComponentString("That player is not online!"));
						return;
					}
					switch (wrapper.wrapping.changeOwner(target)) {
					case ERROR_IMPOSSIBLE_STATUS:
						p.addChatMessage(new TextComponentString("An error occured, please report to mod devs"));
						break;
					case ERROR_NEWOWNER_NOT_ENOUGH:
						p.addChatMessage(new TextComponentString("That player doesn't have enough free airship slots!"));
						break;
					case SUCCESS:
						p.addChatMessage(new TextComponentString("Success! " + target.getName() + " is the new owner of this airship!"));
						break;
					}
					return;
				}
			} else if (args[0].equals("allowPlayer")) {
				if (args[1] != null && !args[1].isEmpty()) {
					EntityPlayer target = server.getPlayerList().getPlayerByUsername(args[1]);
					if (target == null) {
						p.addChatMessage(new TextComponentString("That player is not online!"));
						return;
					}
					wrapper.wrapping.allowedUsers.add(target.entityUniqueID.toString());
					p.addChatMessage(new TextComponentString("Success! " + target.getName() + " can now interact with this airship!"));
					return;
				}
			}
		} else {
			p.addChatMessage(new TextComponentString("You need to be the owner of an airship to change airship settings!"));
		}
	}
}
