package ValkyrienWarfareBase.PhysicsManagement;

import java.util.HashMap;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareControl.Piloting.ClientPilotingManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class DimensionPhysObjectManager {

	private HashMap<World, WorldPhysObjectManager> managerPerWorld;

	private WorldPhysObjectManager cachedManager;

	public DimensionPhysObjectManager() {
		managerPerWorld = new HashMap<World, WorldPhysObjectManager>();
	}

	// Put the ship in the manager queues
	public void onShipLoad(PhysicsWrapperEntity justLoaded) {
		getManagerForWorld(justLoaded.worldObj).onLoad(justLoaded);
	}

	// Remove the ship from the damn queues
	public void onShipUnload(PhysicsWrapperEntity justUnloaded) {
		getManagerForWorld(justUnloaded.worldObj).onUnload(justUnloaded);
	}

	public void initWorld(World toInit) {
		if (!managerPerWorld.containsKey(toInit)) {
			managerPerWorld.put(toInit, new WorldPhysObjectManager(toInit));
		}
	}

	public WorldPhysObjectManager getManagerForWorld(World world) {
		if (cachedManager == null || cachedManager.worldObj != world) {
			cachedManager = managerPerWorld.get(world);
		}
		if (cachedManager == null) {
			System.err.println("getManagerForWorld just requested for a World without one!!! Wtf, how does this even Happen Man!?");
		}
		return cachedManager;
	}

	public void removeWorld(World world) {
		if (managerPerWorld.containsKey(world)) {
			getManagerForWorld(world).physicsEntities.clear();
		}
		managerPerWorld.remove(world);
		// System.out.println("cleared Mounting Entity");
		//This is critical!!! Failure to clear these on world unload will force Java to keep the ENTIRE WORLD LOADED. HUGE MEMORY LEAK!!! Don't change
		ClientPilotingManager.setMountedWrapperEntity(null);
		ClientPilotingManager.setPilotedWrapperEntity(null);
	}

	/**
	 * Returns the PhysicsWrapperEntity that claims this chunk if there is one; returns null if there is no loaded entity managing it
	 * 
	 * @param chunk
	 * @return
	 */
	public PhysicsWrapperEntity getObjectManagingChunk(Chunk chunk) {
		if (chunk == null) {
			return null;
		}
		if (ValkyrienWarfareMod.chunkManager.isChunkInShipRange(chunk.worldObj, chunk.xPosition, chunk.zPosition)) {
			WorldPhysObjectManager physManager = getManagerForWorld(chunk.worldObj);
			if (physManager == null) {
				return null;
			}
			return physManager.getManagingObjectForChunk(chunk);
		}
		return null;
	}

	public PhysicsWrapperEntity getObjectManagingPos(World world, BlockPos pos) {
		Chunk chunk = world.getChunkFromBlockCoords(pos);
		return getObjectManagingChunk(chunk);
	}

	public boolean isEntityFixed(Entity entity) {
		return getManagerForWorld(entity.worldObj).isEntityFixed(entity);
	}

	public PhysicsWrapperEntity getShipFixedOnto(Entity entity) {
		return getManagerForWorld(entity.worldObj).getShipFixedOnto(entity);
	}

}
