package ValkyrienWarfareBase.Command;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

public class PhysicsIterCommand extends CommandBase{

	@Override
	public String getCommandName(){
		return "setPhysIter";
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
            	notifyCommandListener(sender, this, "Physics Iters set to "+sentNum+" :Default (10)", new Object[] {args[0]});
            }
		}catch(Exception e){
			System.out.println(e);
		}
		
	}
}