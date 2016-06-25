package ValkyrienWarfareBase.PhysicsManagement;

import java.util.HashMap;

import ValkyrienWarfareBase.ChunkManagement.PhysicsChunkManager;
import net.minecraft.world.World;

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
		if(managerPerWorld.containsKey(world)){
			getManagerForWorld(world).physicsEntities.clear();
		}
		managerPerWorld.remove(world);
	}
	
}
