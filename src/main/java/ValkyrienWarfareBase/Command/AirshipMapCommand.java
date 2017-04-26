package ValkyrienWarfareBase.Command;

import java.util.EnumSet;
import java.util.Set;

import ValkyrienWarfareBase.Interaction.ShipNameUUIDData;
import ValkyrienWarfareBase.Interaction.ShipUUIDToPosData;
import ValkyrienWarfareBase.Interaction.ShipUUIDToPosData.ShipPositionData;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class AirshipMapCommand extends CommandBase {

	@Override
	public String getCommandName() {
		return "airshipMappings";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/airshipMappings tpto <Ship Name>";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		String term = args[0];
		String shipName = args[1];
		if(args.length > 2){
			for(int i = 2; i < args.length; i++){
				shipName += " " + args[i];
			}
		}
		Entity player = sender.getCommandSenderEntity();
		World world = player.worldObj;
		
		ShipNameUUIDData data = ShipNameUUIDData.get(world);
		
		if(data.ShipNameToLongMap.containsKey(shipName)){
			long shipUUIDMostSig = data.ShipNameToLongMap.get(shipName);
			
			ShipUUIDToPosData posData = ShipUUIDToPosData.get(world);
			
			ShipPositionData positionData = posData.getShipPositionData(shipUUIDMostSig);
			
			double posX = positionData.shipPosition.X;
			double posY = positionData.shipPosition.Y;
			double posZ = positionData.shipPosition.Z;
			
			//Time to teleport!
			
			if (player instanceof EntityPlayerMP){
	            EntityPlayerMP playerMP = (EntityPlayerMP)player;
	            
	            ((EntityPlayerMP) player).connection.setPlayerLocation(posX, posY, posZ, 0, 0);
	        }
		}
	}

}
