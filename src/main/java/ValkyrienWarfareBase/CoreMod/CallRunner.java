package ValkyrienWarfareBase.CoreMod;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.ChunkManagement.PhysicsChunkManager;
import ValkyrienWarfareBase.Collision.EntityCollisionInjector;
import net.minecraft.entity.Entity;
import net.minecraft.world.gen.ChunkProviderServer;

public class CallRunner {

	public static PhysicsChunkManager cachedChunkManager;
	
	public static void onEntityMove(Entity entity,double dx,double dy,double dz){
		if(!EntityCollisionInjector.alterEntityMovement(entity, dx, dy, dz)){
			entity.moveEntity(dx, dy, dz);
		}
	}
	
	public static void onDropChunk(ChunkProviderServer provider,int x,int z){
		if(cachedChunkManager==null){
			cachedChunkManager = ValkyrienWarfareMod.chunkManager.getManagerForWorld(provider.worldObj);
		}else{
			if(cachedChunkManager.worldObj!=provider.worldObj){
				cachedChunkManager = ValkyrienWarfareMod.chunkManager.getManagerForWorld(provider.worldObj);
			}
		}
		if(!cachedChunkManager.isChunkInShipRange(x, z)){
			provider.dropChunk(x, z);
		}
	}

}
