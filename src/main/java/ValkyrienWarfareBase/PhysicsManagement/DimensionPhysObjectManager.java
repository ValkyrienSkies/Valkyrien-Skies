package ValkyrienWarfareBase.PhysicsManagement;

import java.util.HashMap;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.Render.CameraHijacker;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class DimensionPhysObjectManager {

public HashMap<World,WorldPhysObjectManager> managerPerWorld;
	
	private WorldPhysObjectManager cachedManager;

	public DimensionPhysObjectManager(){
		managerPerWorld = new HashMap<World,WorldPhysObjectManager>();
	}
	
	//Put the ship in the manager queues
	public void onShipLoad(PhysicsWrapperEntity justLoaded){
		getManagerForWorld(justLoaded.worldObj).onLoad(justLoaded);
	}
	
	//Remove the ship from the damn queues
	public void onShipUnload(PhysicsWrapperEntity justUnloaded){
		getManagerForWorld(justUnloaded.worldObj).onUnload(justUnloaded);
	}
	
	public void initWorld(World toInit){
		if(!managerPerWorld.containsKey(toInit)){
			managerPerWorld.put(toInit, new WorldPhysObjectManager(toInit));
		}
	}
	
	public WorldPhysObjectManager getManagerForWorld(World world){
		if(cachedManager==null||cachedManager.worldObj!=world){
			cachedManager = managerPerWorld.get(world);
		}
		return cachedManager;
	}
	
	public void removeWorld(World world){
		if(managerPerWorld.containsKey(world)){
			getManagerForWorld(world).physicsEntities.clear();
		}
		managerPerWorld.remove(world);
//		System.out.println("cleared Mounting Entity");
		CameraHijacker.mountedEntity = null;
	}
	
	/**
	 * Returns the PhysicsWrapperEntity that claims this chunk if there is one;
	 * returns null if there is no loaded entity managing it
	 * @param chunk
	 * @return
	 */
	public PhysicsWrapperEntity getObjectManagingChunk(Chunk chunk){
		if(ValkyrienWarfareMod.chunkManager.isChunkInShipRange(chunk.worldObj, chunk.xPosition, chunk.zPosition)){
			return getManagerForWorld(chunk.worldObj).getManagingObjectForChunk(chunk);
		}
		return null;
	}
	
	public PhysicsWrapperEntity getObjectManagingPos(World world,BlockPos pos){
		Chunk chunk = world.getChunkFromBlockCoords(pos);
		return getObjectManagingChunk(chunk);
	}
	
	public boolean isEntityFixed(Entity entity){
		return getManagerForWorld(entity.worldObj).isEntityFixed(entity);
	}
	
}
