package ValkyrienWarfareBase.ChunkManagement;

import java.util.HashMap;

import net.minecraft.world.World;

public class DimensionPhysicsChunkManager {

	public HashMap<World,PhysicsChunkManager> managerPerWorld;
	
	public DimensionPhysicsChunkManager(){
		managerPerWorld = new HashMap<World,PhysicsChunkManager>();
	}
	
	public void initWorld(World toInit){
		if(!managerPerWorld.containsKey(toInit)){
			managerPerWorld.put(toInit, new PhysicsChunkManager(toInit));
		}
	}
	
	public PhysicsChunkManager getManagerForWorld(World world){
		return managerPerWorld.get(world);
	}
	
	public void removeWorld(World world){
		managerPerWorld.remove(world);
	}
}
