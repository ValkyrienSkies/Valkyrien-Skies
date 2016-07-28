package ValkyrienWarfareBase.Command;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

public class PhysicsSpeedCommand extends CommandBase{

	@Override
	public String getCommandName(){
		return "setPhysSpeed";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "Sets seconds simulated per tick";
	}

	@Override
	public int getRequiredPermissionLevel(){
        return 3;
    }

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		try{
			World commandWorld = sender.getEntityWorld();
			
			double sentNum = ValkyrienWarfareMod.physicsManager.getManagerForWorld(commandWorld).physSpeed;
			String s = null;
            s = getChatComponentFromNthArg(sender, args, 0).getUnformattedText();
	        try{
            	if(s!=null){
	            	sentNum = Double.parseDouble(s);
	            }
	        }catch(Exception e){
	        	notifyCommandListener(sender, this, "Invalid Input", new Object[] {args[0]});
        		return;
	        }
            if(sentNum>=-20&&sentNum<=100){
            	ValkyrienWarfareMod.physicsManager.getManagerForWorld(commandWorld).physSpeed = sentNum;
            	notifyCommandListener(sender, this, "Physics Speed set to "+sentNum+" :Default (.055); Do not set this to a negative value unless you want to ruin your world", new Object[] {args[0]});
            }
		}catch(Exception e){
			System.out.println(e);
		}
		
	}
}