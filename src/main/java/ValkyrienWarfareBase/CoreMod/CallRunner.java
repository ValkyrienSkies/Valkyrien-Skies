package ValkyrienWarfareBase.CoreMod;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.ChunkManagement.PhysicsChunkManager;
import ValkyrienWarfareBase.Collision.EntityCollisionInjector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkProviderServer;

public class CallRunner {
	
	public static void onEntityMove(Entity entity,double dx,double dy,double dz){
		if(!EntityCollisionInjector.alterEntityMovement(entity, dx, dy, dz)){
			entity.moveEntity(dx, dy, dz);
		}
	}
	
	public static void onEntityRemoved(World world,Entity removed){
		if(removed instanceof PhysicsWrapperEntity){
			ValkyrienWarfareMod.physicsManager.onShipUnload((PhysicsWrapperEntity) removed);
		}
		world.onEntityRemoved(removed);
	}
	
	public static void onEntityAdded(World world,Entity added){
		world.onEntityAdded(added);
	}
	
	public static void onDropChunk(ChunkProviderServer provider,int x,int z){
		if(!ValkyrienWarfareMod.chunkManager.isChunkInShipRange(provider.worldObj,x, z)){
			provider.dropChunk(x, z);
		}
	}

}
