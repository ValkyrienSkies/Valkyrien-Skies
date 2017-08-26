package ValkyrienWarfareBase.PhysicsManagement;

import ValkyrienWarfareBase.ChunkManagement.PhysicsChunkManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;

public class DimensionPhysObjectManager {
	
	private HashMap<World, WorldPhysObjectManager> managerPerWorld;
	
	private WorldPhysObjectManager cachedManager;
	
	public DimensionPhysObjectManager() {
		managerPerWorld = new HashMap<World, WorldPhysObjectManager>();
	}
	
	/**
	 * Kinda like a preorder, order one now!
	 *
	 * @param toPreload
	 */
	public void onShipPreload(PhysicsWrapperEntity toPreload) {
		getManagerForWorld(toPreload.world).preloadPhysicsWrapperEntityMappings(toPreload);
	}
	
	// Put the ship in the manager queues
	public void onShipLoad(PhysicsWrapperEntity justLoaded) {
		getManagerForWorld(justLoaded.world).onLoad(justLoaded);
	}
	
	// Remove the ship from the damn queues
	public void onShipUnload(PhysicsWrapperEntity justUnloaded) {
		getManagerForWorld(justUnloaded.world).onUnload(justUnloaded);
	}
	
	public void initWorld(World toInit) {
		if (!managerPerWorld.containsKey(toInit)) {
			managerPerWorld.put(toInit, new WorldPhysObjectManager(toInit));
		}
	}
	
	public WorldPhysObjectManager getManagerForWorld(World world) {
		if (world == null) {
			//I'm not quite sure what to do here
		}
		if (cachedManager == null || cachedManager.worldObj != world) {
			cachedManager = managerPerWorld.get(world);
		}
		if (cachedManager == null) {
			System.err.println("getManagerForWorld just requested for a World without one!!! Assuming that this is a new world, so making a new WorldPhysObjectManager for it.");
			cachedManager = new WorldPhysObjectManager(world);
			//Make sure to add the cachedManager to the world managers
			managerPerWorld.put(world, cachedManager);
		}
		return cachedManager;
	}
	
	public void removeWorld(World world) {
		if (managerPerWorld.containsKey(world)) {
			getManagerForWorld(world).physicsEntities.clear();
		}
		managerPerWorld.remove(world);
	}
	
	/**
	 * Returns the PhysicsWrapperEntity that claims this chunk if there is one; returns null if there is no loaded entity managing it
	 *
	 * @param chunk
	 * @return
	 */
	
	//TODO: Fix this
	@Deprecated
	public PhysicsWrapperEntity getObjectManagingPos(World world, BlockPos pos) {
		if (world == null || pos == null) {
			return null;
		}
		if (world.getChunkProvider() == null) {
//			System.out.println("Retard Devs coded a World with no Chunks in it!");
			return null;
		}
		
		if (!PhysicsChunkManager.isLikelyShipChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
			return null;
		}
		//NoClassFound Entity$1.class FIX
//		if(!world.isRemote){
//			if(world.getChunkProvider() instanceof ChunkProviderServer){
//				ChunkProviderServer providerServer =  (ChunkProviderServer) world.getChunkProvider();
//				//The chunk at the given pos isn't loaded? Don't bother with the next step, you'll create an infinite loop!
//				if(!providerServer.chunkExists(pos.getX() >> 4, pos.getZ() >> 4)){
//					return null;
//				}
//			}
//		}
//		Chunk chunk = world.getChunkFromBlockCoords(pos);
//		return getObjectManagingChunk(chunk);
		WorldPhysObjectManager physManager = getManagerForWorld(world);
		if (physManager == null) {
			return null;
		}
		return physManager.getManagingObjectForChunkPosition(pos.getX() >> 4, pos.getZ() >> 4);
	}
	
	public boolean isEntityFixed(Entity entity) {
		return getManagerForWorld(entity.world).isEntityFixed(entity);
	}
	
	public PhysicsWrapperEntity getShipFixedOnto(Entity entity) {
		return getManagerForWorld(entity.world).getShipFixedOnto(entity, false);
	}
	
}
