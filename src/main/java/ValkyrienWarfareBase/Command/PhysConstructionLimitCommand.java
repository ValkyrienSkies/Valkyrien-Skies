package ValkyrienWarfareBase.Command;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class PhysConstructionLimitCommand extends CommandBase{

	@Override
	public String getCommandName(){
		return "setPhysConstructionLimit";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "Sets the maximum blocks a Ship will consider for spawning";
	}

	@Override
	public int getRequiredPermissionLevel(){
        return 3;
    }

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		try{
			int sentNum = ValkyrienWarfareMod.maxShipSize;
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
            if(sentNum>=0){
            	ValkyrienWarfareMod.maxShipSize = sentNum;
            	notifyCommandListener(sender, this, "Physics Construction Limit set to "+sentNum+" :Default (15000)", new Object[] {args[0]});
            }
		}catch(Exception e){
			System.out.println(e);
		}
		
	}
}