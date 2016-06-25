package ValkyrienWarfareBase.ChunkManagement;

import java.util.HashMap;

import ValkyrienWarfareBase.PhysicsManagement.WorldPhysObjectManager;
import net.minecraft.world.World;

public class DimensionPhysicsChunkManager {

	public HashMap<World,PhysicsChunkManager> managerPerWorld;
	private PhysicsChunkManager cachedManager;
	
	public DimensionPhysicsChunkManager(){
		managerPerWorld = new HashMap<World,PhysicsChunkManager>();
	}
	
	public void initWorld(World toInit){
		if(!managerPerWorld.containsKey(toInit)){
			managerPerWorld.put(toInit, new PhysicsChunkManager(toInit));
		}
	}
	
	public PhysicsChunkManager getManagerForWorld(World world){
		if(cachedManager!=null){
			if(cachedManager.worldObj!=world){
				cachedManager = managerPerWorld.get(world);
			}
		}else{
			cachedManager = managerPerWorld.get(world);
		}
		return cachedManager;
	}
	
	public void removeWorld(World world){
		managerPerWorld.remove(world);
	}
}
