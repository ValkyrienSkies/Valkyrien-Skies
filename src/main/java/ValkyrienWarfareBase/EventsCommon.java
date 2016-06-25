package ValkyrienWarfareBase;

import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventsCommon {

	@SubscribeEvent(priority=EventPriority.HIGHEST)
	public void onWorldLoad(WorldEvent.Load event){
		if(!event.getWorld().isRemote){
			ValkyrienWarfareMod.chunkManager.initWorld(event.getWorld());
		}
		ValkyrienWarfareMod.physicsManager.initWorld(event.getWorld());
	}
	
	@SubscribeEvent(priority=EventPriority.HIGHEST)
	public void onWorldUnload(WorldEvent.Unload event){
		if(!event.getWorld().isRemote){
			ValkyrienWarfareMod.chunkManager.removeWorld(event.getWorld());
		}
		ValkyrienWarfareMod.physicsManager.removeWorld(event.getWorld());
	}
	
	@SubscribeEvent(priority=EventPriority.HIGHEST)
	public void onChunkNBTLoad(ChunkDataEvent.Load event){
		NBTTagCompound data = event.getData();
		
	}
	
	@SubscribeEvent(priority=EventPriority.HIGHEST)
	public void onChunkNBTUnload(ChunkDataEvent.Save event){
		NBTTagCompound data = event.getData();
		
	}

	@SubscribeEvent(priority=EventPriority.HIGHEST)
	public void onEntityUntrack(PlayerEvent.StopTracking event){
		if(!event.getEntityPlayer().worldObj.isRemote){
			Entity ent = event.getTarget();
			if(ent instanceof PhysicsWrapperEntity){
				((PhysicsWrapperEntity)ent).wrapping.onPlayerUntracking(event.getEntityPlayer());
			}
		}
	}

}
