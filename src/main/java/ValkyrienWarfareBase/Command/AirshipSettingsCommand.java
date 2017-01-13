package ValkyrienWarfareBase.Command;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

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
import net.minecraft.util.text.TextFormatting;

public class AirshipSettingsCommand extends CommandBase {
	
public static final ArrayList<String> completionOptions = new ArrayList<String>();
	
	static {
		completionOptions.add("transfer");
		completionOptions.add("allowPlayer");
	}

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

		if (args.length == 0) {
			sender.addChatMessage(new TextComponentString(TextFormatting.RED + "Usage: " + getCommandUsage(sender)));
			return;
		}

		EntityPlayer p = (EntityPlayer) sender;
		BlockPos pos = p.rayTrace(p.isCreative() ? 5.0 : 4.5, 1).getBlockPos();
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(p.getEntityWorld(), pos);

		if (wrapper == null) {
			sender.addChatMessage(new TextComponentString("You need to be looking at an airship to do that!"));
			return;
		}
		if (p.entityUniqueID.toString().equals(wrapper.wrapping.creator)) {
			if (args[0].equals("transfer")) {
				if (args.length == 1) {
					return;
				}
				if (!args[1].isEmpty()) {
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
				if (args.length == 1) {
					StringBuilder result = new StringBuilder("<");
					Iterator<String> iter = wrapper.wrapping.allowedUsers.iterator();
					while (iter.hasNext()) {
						result.append(iter.next() + (iter.hasNext() ? ", " : ">"));
					}
					p.addChatMessage(new TextComponentString(result.toString()));
					return;
				}
				if (!args[1].isEmpty()) {
					EntityPlayer target = server.getPlayerList().getPlayerByUsername(args[1]);
					if (target == null) {
						p.addChatMessage(new TextComponentString("That player is not online!"));
						return;
					}
					if (target.entityUniqueID.toString().equals(wrapper.wrapping.creator)) {
						p.addChatMessage(new TextComponentString("You can't add yourself to your own airship!"));
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
	
	@Override
	public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
		if (args.length == 1)	{
			ArrayList<String> possibleArgs = (ArrayList<String>) completionOptions.clone();
			
			for (Iterator<String> iterator = possibleArgs.iterator(); iterator.hasNext();) { //Don't like this, but I have to because concurrentmodificationexception			    
			    if (!iterator.next().startsWith(args[0])) {
			        iterator.remove();
			    }
			}
			
			return possibleArgs;
		} else if (args.length == 2)	{
			if (args[0].startsWith("do"))	{
				if (args[1].startsWith("t"))	{
					return Lists.newArrayList("true");
				} else if (args[1].startsWith("f"))	{
					return Lists.newArrayList("false");
				} else {
					return Lists.newArrayList("true", "false");
				}
			}
		}
		
		return null;
	}
}
