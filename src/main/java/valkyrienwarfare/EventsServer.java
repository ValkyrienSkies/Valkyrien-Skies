package valkyrienwarfare;

public class EventsServer {

	/*@SubscribeEvent
	public void livingUpdate(LivingUpdateEvent e)	{
		if (e.getEntity() instanceof EntityPlayer)	{
			EntityPlayer p = (EntityPlayer) e.getEntity();
			
			ValkyrienWarfareMod.VWLogger.log(Level.INFO, "EntityPlayer LivingUpdateEvent");
			
			if (p.lastTickPosX != p.posX || p.lastTickPosZ != p.posZ)	{ //Player has moved
				ValkyrienWarfareMod.VWLogger.log(Level.INFO, "EntityPlayer LivingUpdateEvent Move");
				if (math.abs(p.posX) > 27000000 || math.abs(p.posZ) > 27000000)	{ //Player is outside of world border, tp them back
					p.attemptTeleport(p.lastTickPosX, p.lastTickPosY, p.lastTickPosZ);
					p.addChatMessage(new TextComponentString("You can't go beyond 27000000 blocks because airships are stored there!"));
				}
			}
		}
	}*/
}
