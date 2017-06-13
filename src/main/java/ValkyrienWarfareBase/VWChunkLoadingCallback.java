package ValkyrienWarfareBase;

import java.util.List;

import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

public class VWChunkLoadingCallback implements LoadingCallback {

	@Override
	public void ticketsLoaded(List<Ticket> tickets, World world) {
		//Delete the old tickets, we don't need them
		for(Ticket ticket : tickets){
			ForgeChunkManager.releaseTicket(ticket);
		}
	}

}
