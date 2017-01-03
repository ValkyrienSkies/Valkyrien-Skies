package ValkyrienWarfareBase.Command;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.API.Vector;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

public class PhysicsGravityCommand extends CommandBase{

	@Override
	public String getCommandName(){
		return "setPhysGravity";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "Sets the simulated Gravity Vector";
	}

	@Override
	public int getRequiredPermissionLevel(){
        return 3;
    }

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		try{
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
	            	newVector.X = Double.parseDouble(numbers[0]);
	            	newVector.Y = Double.parseDouble(numbers[1]);
	            	newVector.Z = Double.parseDouble(numbers[2]);
	            }
            }catch(Exception e){
            	notifyCommandListener(sender, this, "Invalid Input", new Object[] {args[0]});
        		return;
            }
            ValkyrienWarfareMod.gravity = newVector;
            notifyCommandListener(sender, this, "Physics Gravity set to "+newVector.toRoundedString()+" :Default: <0,-9.8,0>", new Object[] {args[0]});
		}catch(Exception e){
			System.out.println(e);
		}
		
	}
}