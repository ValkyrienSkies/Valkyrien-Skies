package ValkyrienWarfareBase.Command;

import java.util.List;

import javax.annotation.Nullable;

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

	@Override
	public String getCommandName() {
		return "physSettings";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/physSettings <setting name> [value]";
	}
	
	@Override
	public int getRequiredPermissionLevel(){
        return 3;
    }

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		String key = args[0];
		if (key.equals("doSplitting"))	{
			if (args.length == 1)	{
				sender.addChatMessage(new TextComponentString("doSplitting=" + ValkyrienWarfareMod.doSplitting + " (Default: false)"));
				return;
			} else if (args.length == 2)	{
				boolean value = Boolean.parseBoolean(args[1]);
				sender.addChatMessage(new TextComponentString("Set physics splitting to " + value));
				ValkyrienWarfareMod.doSplitting = value;
			}
		} else if (key.equals("maxShipSize"))	{
			if (args.length == 1)	{
				sender.addChatMessage(new TextComponentString("maxShipSize=" + ValkyrienWarfareMod.maxShipSize + " (Default: 15000)"));
				return;
			} else if (args.length == 2)	{
				int value = Integer.parseInt(args[1]);
				sender.addChatMessage(new TextComponentString("Set maximum ship size to " + value));
				ValkyrienWarfareMod.maxShipSize = value;
			}
		} else if (key.equals("gravityVector"))	{
			if (args.length == 1)	{
				sender.addChatMessage(new TextComponentString("gravityVector=" + ValkyrienWarfareMod.gravity.toRoundedString() + " (Default: <0,-9.8,0>)"));
				return;
			} else if (args.length == 2)	{
				World commandWorld = sender.getEntityWorld();
				String s = null;
	            s = getChatComponentFromNthArg(sender, args, 0).getUnformattedText();
	            Vector newVector = new Vector();
	            try{
		            if(s!=null){
		            	s = s.replace("<","");
		            	s = s.replace(">","");
		            	s = s.replace(" ","");
		            	String[] numbers = s.split(",");
		            	newVector.X = Double.parseDouble(numbers[1]);
		            	newVector.Y = Double.parseDouble(numbers[2]);
		            	newVector.Z = Double.parseDouble(numbers[3]);
		            }
	            }catch(Exception e){
	            	sender.addChatMessage(new TextComponentString("Usage: /physSettings gravityVector <x> <y> <z>"));
	        		return;
	            }
	            ValkyrienWarfareMod.gravity = newVector;
	            sender.addChatMessage(new TextComponentString("Physics Gravity for world" + commandWorld.getProviderName() + " set to " + newVector.toRoundedString() + " (Default: <0,-9.8,0>)"));
			}
		} else if (key.equals("pysicsIterations"))	{
			if (args.length == 1)	{
				sender.addChatMessage(new TextComponentString("pysicsIterations=" + ValkyrienWarfareMod.physIter + " (Default: 10)"));
				return;
			} else if (args.length == 2)	{
				World commandWorld = sender.getEntityWorld();
				
				int sentNum = ValkyrienWarfareMod.physIter;
				String s = null;
	            s = getChatComponentFromNthArg(sender, args, 0).getUnformattedText();
	            try{
	            	if(s!=null){
	            		sentNum = Integer.parseInt(s);
	            	}
	            }catch(Exception e){
	            	notifyCommandListener(sender, this, "Invalid Input", new Object[] {args[0]});
	        		return;
	            }
	            if(sentNum>=0&&sentNum<=1000){
	            	ValkyrienWarfareMod.physIter = sentNum;
					sender.addChatMessage(new TextComponentString("pysicsIterations=" + ValkyrienWarfareMod.physIter + " (Default: 10)"));
	            }
			}
		} else if (key.equals("physicsSpeed"))	{
			if (args.length == 1)	{
				sender.addChatMessage(new TextComponentString("physicsSpeed=" + ValkyrienWarfareMod.maxShipSize + " (Default: 0.5)"));
				return;
			} else if (args.length == 2)	{
				World commandWorld = sender.getEntityWorld();
				
				int sentNum = ValkyrienWarfareMod.physIter;
				String s = null;
	            s = getChatComponentFromNthArg(sender, args, 0).getUnformattedText();
	            try{
	            	if(s!=null){
	            		sentNum = Integer.parseInt(s);
	            	}
	            }catch(Exception e){
	            	notifyCommandListener(sender, this, "Invalid Input", new Object[] {args[0]});
	        		return;
	            }
	            if(sentNum>=0&&sentNum<=1000){
	            	ValkyrienWarfareMod.physIter = sentNum;
					sender.addChatMessage(new TextComponentString("physicsSpeed=" + ValkyrienWarfareMod.maxShipSize + " (Default: 0.5)"));
	            }
			}
		}
		
		sender.addChatMessage(new TextComponentString(this.getCommandUsage(sender)));
	}
	
	@Override
	public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
		return null;
    }
}
