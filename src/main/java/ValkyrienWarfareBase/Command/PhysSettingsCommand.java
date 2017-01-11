package ValkyrienWarfareBase.Command;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import ValkyrienWarfareBase.PhysicsSettings;
import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.API.Vector;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class PhysSettingsCommand extends CommandBase {

	public static final ArrayList<String> completionOptions = new ArrayList<String>();
	
	static {
		completionOptions.add("gravityVector");
		completionOptions.add("doSplitting");
		completionOptions.add("maxShipSize");
		completionOptions.add("physicsIterations");
		completionOptions.add("physicsSpeed");
		completionOptions.add("doGravity");
		completionOptions.add("doPhysicsBlocks");
		completionOptions.add("doBalloons");
		completionOptions.add("doAirshipRotation");
		completionOptions.add("doAirshipMovement");
		completionOptions.add("save");
	}

	@Override
	public String getCommandName() {
		return "physSettings";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/physSettings <setting name> [value]";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 3;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		String key = args[0];
		if (key.equals("doSplitting")) {
			if (args.length == 1) {
				sender.addChatMessage(new TextComponentString("doSplitting=" + ValkyrienWarfareMod.doSplitting + " (Default: false)"));
				return;
			} else if (args.length == 2) {
				boolean value = Boolean.parseBoolean(args[1]);
				sender.addChatMessage(new TextComponentString("Set physics splitting to " + value));
				ValkyrienWarfareMod.doSplitting = value;
				return;
			}
		} else if (key.equals("maxShipSize")) {
			if (args.length == 1) {
				sender.addChatMessage(new TextComponentString("maxShipSize=" + ValkyrienWarfareMod.maxShipSize + " (Default: 15000)"));
				return;
			} else if (args.length == 2) {
				int value = Integer.parseInt(args[1]);
				sender.addChatMessage(new TextComponentString("Set maximum ship size to " + value));
				ValkyrienWarfareMod.maxShipSize = value;
				return;
			}
		} else if (key.equals("gravityVector")) {
			if (args.length == 1) {
				sender.addChatMessage(new TextComponentString("gravityVector=" + ValkyrienWarfareMod.gravity.toRoundedString() + " (Default: <0,-9.8,0>)"));
				return;
			} else if (args.length == 2) {
				Vector newVector = new Vector(0, -9.8, 0);
				try {
					if (args[1] != null) {
						String[] numbers = args[1].replace("<", "").replace(">", "").replace(" ", "").split(",");
						newVector.X = Double.parseDouble(numbers[1]);
						newVector.Y = Double.parseDouble(numbers[2]);
						newVector.Z = Double.parseDouble(numbers[3]);
					} else {
						sender.addChatMessage(new TextComponentString("Usage: /physSettings gravityVector <x> <y> <z>"));
						return;
					}
				} catch (Exception e) {}
				ValkyrienWarfareMod.gravity = newVector;
				sender.addChatMessage(new TextComponentString("Physics Gravity set to " + newVector.toRoundedString() + " (Default: <0,-9.8,0>)"));
				return;
			}
		} else if (key.equals("physicsIterations")) {
			if (args.length == 1) {
				sender.addChatMessage(new TextComponentString("physicsIterations=" + ValkyrienWarfareMod.physIter + " (Default: 10)"));
				return;
			} else if (args.length == 2) {
				int sentNum = ValkyrienWarfareMod.physIter;
				try {
					sentNum = Integer.parseInt(args[1]);
				} catch (Exception e) {
					notifyCommandListener(sender, this, "Invalid Input", new Object[] {});
					return;
				}
				if (sentNum >= 0 && sentNum <= 1000) {
					ValkyrienWarfareMod.physIter = sentNum;
					sender.addChatMessage(new TextComponentString("physicsIterations=" + ValkyrienWarfareMod.physIter + " (Default: 10)"));
					return;
				}
			}
		} else if (key.equals("physicsSpeed")) {
			if (args.length == 1) {
				sender.addChatMessage(new TextComponentString("physicsSpeed=" + ValkyrienWarfareMod.maxShipSize + " (Default: 0.5)"));
				return;
			} else if (args.length == 2) {
				double sentNum = ValkyrienWarfareMod.physSpeed;
				try {
					sentNum = Double.parseDouble(args[1]);
				} catch (Exception e) {
					notifyCommandListener(sender, this, "Invalid Input", new Object[] {});
					return;
				}
				if (sentNum >= 0 && sentNum <= 1000) {
					ValkyrienWarfareMod.physSpeed = sentNum;
					sender.addChatMessage(new TextComponentString("physicsSpeed=" + ValkyrienWarfareMod.maxShipSize + " (Default: 0.5)"));
					return;
				} else {
					sender.addChatMessage(new TextComponentString("Value too big! Use a value between 0 and 1000."));
					return;
				}
			}
		} else if (key.equals("doGravity")) {
			if (args.length == 1) {
				sender.addChatMessage(new TextComponentString("doGravity=" + PhysicsSettings.doGravity + " (Default: true)"));
				return;
			} else if (args.length == 2) {
				boolean value = Boolean.parseBoolean(args[1]);
				sender.addChatMessage(new TextComponentString("Set doGravity to " + (PhysicsSettings.doGravity ? "enabled" : "disabled")));
				PhysicsSettings.doGravity = value;
				return;
			}
		} else if (key.equals("doPhysicsBlocks")) {
			if (args.length == 1) {
				sender.addChatMessage(new TextComponentString("doPhysicsBlocks=" + PhysicsSettings.doPhysicsBlocks + " (Default: true)"));
				return;
			} else if (args.length == 2) {
				boolean value = Boolean.parseBoolean(args[1]);
				sender.addChatMessage(new TextComponentString("Set doPhysicsBlocks to " + (PhysicsSettings.doPhysicsBlocks ? "enabled" : "disabled")));
				PhysicsSettings.doPhysicsBlocks = value;
				return;
			}
		} else if (key.equals("doBalloons")) {
			if (args.length == 1) {
				sender.addChatMessage(new TextComponentString("doBalloons=" + PhysicsSettings.doBalloons + " (Default: true)"));
				return;
			} else if (args.length == 2) {
				boolean value = Boolean.parseBoolean(args[1]);
				sender.addChatMessage(new TextComponentString("Set doBalloons to " + (PhysicsSettings.doBalloons ? "enabled" : "disabled")));
				PhysicsSettings.doBalloons = value;
				return;
			}
		} else if (key.equals("doAirshipRotation")) {
			if (args.length == 1) {
				sender.addChatMessage(new TextComponentString("doAirshipRotation=" + PhysicsSettings.doAirshipRotation + " (Default: true)"));
				return;
			} else if (args.length == 2) {
				boolean value = Boolean.parseBoolean(args[1]);
				sender.addChatMessage(new TextComponentString("Set doAirshipRotation to " + (PhysicsSettings.doAirshipRotation ? "enabled" : "disabled")));
				PhysicsSettings.doAirshipRotation = value;
				return;
			}
		} else if (key.equals("doAirshipMovement")) {
			if (args.length == 1) {
				sender.addChatMessage(new TextComponentString("doAirshipMovement=" + PhysicsSettings.doAirshipMovement + " (Default: true)"));
				return;
			} else if (args.length == 2) {
				boolean value = Boolean.parseBoolean(args[1]);
				sender.addChatMessage(new TextComponentString("Set doAirshipMovement to " + (PhysicsSettings.doGravity ? "enabled" : "disabled")));
				PhysicsSettings.doAirshipMovement = value;
				return;
			}
		} else if (key.equals("save")) {
			ValkyrienWarfareMod.instance.saveConfig();
			sender.addChatMessage(new TextComponentString("Saved phisics settings"));
			return;
		}

		sender.addChatMessage(new TextComponentString(this.getCommandUsage(sender)));
	}

	@Override
	public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
		if (args.length == 1)	{
			ArrayList<String> possibleArgs = (ArrayList<String>) completionOptions.clone();
			
			for (Iterator<String> iterator = possibleArgs.iterator(); iterator.hasNext();) { //Don't like this, but i have to because concurrentmodificationexception			    
			    if (!iterator.next().startsWith(args[0])) {
			        iterator.remove();
			    }
			}
			
			return possibleArgs;
		}
		
		return null;
	}
}
