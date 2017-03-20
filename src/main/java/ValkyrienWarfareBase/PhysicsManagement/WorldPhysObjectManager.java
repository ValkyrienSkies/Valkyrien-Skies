package ValkyrienWarfareBase.PhysicsManagement;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

/**
 * This class essentially handles all the issues with ticking and handling Physics Objects in the given world
 *
 * @author thebest108
 *
 */
public class WorldPhysObjectManager {

	// private static double ShipRangeCheck = 120D;
	public World worldObj;
	public ArrayList<PhysicsWrapperEntity> physicsEntities = new ArrayList<PhysicsWrapperEntity>();
	public ArrayList<PhysicsWrapperEntity> physicsEntitiesToUnload = new ArrayList<PhysicsWrapperEntity>();
	public ArrayList<Callable<Void>> physCollisonCallables = new ArrayList<Callable<Void>>();
//	private static Field droppedChunksField;

	public WorldPhysObjectManager(World toManage) {
		worldObj = toManage;
	}

	/**
	 * Returns the list of PhysicsEntities that aren't too far away from players to justify being ticked
	 * @return
	 */
	public ArrayList<PhysicsWrapperEntity> getTickablePhysicsEntities(){
		ArrayList<PhysicsWrapperEntity> list = (ArrayList<PhysicsWrapperEntity>) physicsEntities.clone();

		ArrayList<PhysicsWrapperEntity> frozenShips = new ArrayList<PhysicsWrapperEntity>();


		if(worldObj instanceof WorldServer){
			WorldServer worldServer = (WorldServer)worldObj;
	        for (PhysicsWrapperEntity wrapper:list){
	        	if(!wrapper.isDead){
		        	if(wrapper.wrapping.surroundingWorldChunksCache != null){
		        		int chunkCacheX = MathHelper.floor_double(wrapper.posX/16D)-wrapper.wrapping.surroundingWorldChunksCache.chunkX;
		        		int chunkCacheZ = MathHelper.floor_double(wrapper.posZ/16D)-wrapper.wrapping.surroundingWorldChunksCache.chunkZ;

		        		chunkCacheX = Math.max(0, Math.min(chunkCacheX,wrapper.wrapping.surroundingWorldChunksCache.chunkArray.length-1));
		        		chunkCacheZ = Math.max(0, Math.min(chunkCacheZ,wrapper.wrapping.surroundingWorldChunksCache.chunkArray[0].length-1));

		        		Chunk chunk = wrapper.wrapping.surroundingWorldChunksCache.chunkArray[chunkCacheX][chunkCacheZ];

//		        		Chunk chunk = wrapper.wrapping.surroundingWorldChunksCache.chunkArray[(wrapper.wrapping.surroundingWorldChunksCache.chunkArray.length)/2][(wrapper.wrapping.surroundingWorldChunksCache.chunkArray[0].length)/2];
			            if (chunk != null && !worldServer.thePlayerManager.contains(chunk.xPosition, chunk.zPosition))
			            {
			            	frozenShips.add(wrapper);
			            	//Then I should freeze any ships in this chunk
			            }
		        	}
	        	}else{
	        		frozenShips.add(wrapper);
	        	}
	        }
		}

		/*if(droppedChunksField == null){
			try{
				if(ValkyrienWarfarePlugin.isObfuscatedEnvironment){
					droppedChunksField = ChunkProviderServer.class.getDeclaredField("field_73248_b");
				}else{
					droppedChunksField = ChunkProviderServer.class.getDeclaredField("droppedChunksSet");
				}
				droppedChunksField.setAccessible(true);
			}catch(Exception e){}
		}
		ChunkProviderServer serverProvider = (ChunkProviderServer) worldObj.getChunkProvider();

		try{
			Set<Long> droppedChunks = (Set<Long>) droppedChunksField.get(serverProvider);

			for(PhysicsWrapperEntity entity:list){
				int chunkX = entity.chunkCoordX;
				int chunkZ = entity.chunkCoordZ;
				if(droppedChunks.contains(ChunkPos.chunkXZ2Int(chunkX, chunkZ))){
					frozenShips.add(entity);
				}
			}
		}catch(Exception e){}*/

		list.removeAll(frozenShips);

		return list;
	}

	public void onLoad(PhysicsWrapperEntity loaded) {
		if (!loaded.wrapping.fromSplit) {
			physicsEntities.add(loaded);
			physCollisonCallables.add(loaded.wrapping.collisionCallable);
		} else {
			// reset check to prevent strange errors
			loaded.wrapping.fromSplit = false;
		}
	}

	public void onUnload(PhysicsWrapperEntity loaded) {
		physicsEntities.remove(loaded);
		physCollisonCallables.remove(loaded.wrapping.collisionCallable);
		loaded.wrapping.onThisUnload();
	}

	public PhysicsWrapperEntity getManagingObjectForChunk(Chunk chunk) {
		for (PhysicsWrapperEntity wrapper : physicsEntities) {
			if (wrapper.wrapping.ownsChunk(chunk.xPosition, chunk.zPosition)) {
				return wrapper;
			}
		}
		return null;
	}

	public List<PhysicsWrapperEntity> getNearbyPhysObjects(AxisAlignedBB toCheck) {
		ArrayList<PhysicsWrapperEntity> ships = new ArrayList<PhysicsWrapperEntity>();

		AxisAlignedBB expandedCheck = toCheck.expand(6, 6, 6);

		for (PhysicsWrapperEntity wrapper : physicsEntities) {
			if (wrapper.wrapping.collisionBB.intersectsWith(expandedCheck)) {
				ships.add(wrapper);
			}
		}

		return ships;
	}

	public boolean isEntityFixed(Entity entity) {
		if (getShipFixedOnto(entity) != null) {
			return true;
		}
		return false;
	}

	public PhysicsWrapperEntity getShipFixedOnto(Entity entity) {
		for (PhysicsWrapperEntity wrapper : physicsEntities) {
			if (wrapper.wrapping.isEntityFixed(entity)) {
				if (wrapper.riddenByEntities.contains(entity)){
					return wrapper;
				}
				//If one of the entities riding has this entity too, then be sure to check for it
				for(Entity e:wrapper.riddenByEntities){
					if(!e.isDead && e.riddenByEntities.contains(entity)){
						return wrapper;
					}
				}
			}
		}
		return null;
	}

}
