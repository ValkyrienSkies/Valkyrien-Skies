package ValkyrienWarfareBase.Command;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

public class PhysSplittingToggleCommand extends CommandBase{

	@Override
	public String getCommandName(){
		return "doPhysSplitting";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/doPhysSplitting true:false";
	}

	@Override
	public int getRequiredPermissionLevel(){
        return 3;
    }

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		try{
			World commandWorld = sender.getEntityWorld();
			
			boolean value = ValkyrienWarfareMod.doSplitting;
			String s = null;
            s = getChatComponentFromNthArg(sender, args, 0).getUnformattedText();
	        try{
            	if(s!=null){
            		value = Boolean.parseBoolean(s);
	            }
	        }catch(Exception e){
	        	notifyCommandListener(sender, this, "Invalid Input", new Object[] {args[0]});
        		return;
	        }
	        ValkyrienWarfareMod.doSplitting = value;
	        if(value){
	        	notifyCommandListener(sender, this, "Physics Splitting Enabled", new Object[] {args[0]});
	        }else{
	        	notifyCommandListener(sender, this, "Physics Splitting Disabled", new Object[] {args[0]});
	        }
		}catch(Exception e){
			System.out.println(e);
		}
		
	}
}