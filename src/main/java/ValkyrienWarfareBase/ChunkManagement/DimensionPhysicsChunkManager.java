package ValkyrienWarfareBase.ChunkManagement;

import java.util.HashMap;

import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class DimensionPhysicsChunkManager {

	public HashMap<World, PhysicsChunkManager> managerPerWorld;
	private PhysicsChunkManager cachedManager;

	public DimensionPhysicsChunkManager() {
		managerPerWorld = new HashMap<World, PhysicsChunkManager>();
	}

	public void initWorld(World toInit) {
		if (!managerPerWorld.containsKey(toInit)) {
			managerPerWorld.put(toInit, new PhysicsChunkManager(toInit));
		}
	}

	public PhysicsChunkManager getManagerForWorld(World world) {
		if(world == null){
			return null;
		}
		if (cachedManager == null || cachedManager.worldObj != world) {
			cachedManager = managerPerWorld.get(world);
			if (cachedManager == null) {
				initWorld(world);
				cachedManager = managerPerWorld.get(world);
			}
		}
		return cachedManager;
	}

	public void removeWorld(World world) {
		managerPerWorld.remove(world);
	}
}
