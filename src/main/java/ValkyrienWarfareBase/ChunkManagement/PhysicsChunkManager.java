package ValkyrienWarfareBase.ChunkManagement;

import net.minecraft.world.World;

/**
 * This class is responsible for finding/allocating the Chunks for PhysicsObjects; also ensures the custom chunk-loading system in place
 *
 * @author thebest108
 *
 */
public class PhysicsChunkManager {

	public World worldObj;
	public int nextChunkSetKey;
	public int chunkSetIncrement;
	public static int xChunkStartingPos = -1870000;
	public static int zChunkStartingPos = -1870000;
	// public int chunkRadius = 3;
	public static int maxChunkRadius = 12;
	// Currently at 3 to be safe, this is important because Ships could start affecting
	// each other remotely if this value is too small (ex. 0)
	public int distanceBetweenSets = 1;
	public ChunkKeysWorldData data;

	public PhysicsChunkManager(World worldFor) {
		worldObj = worldFor;
		chunkSetIncrement = (maxChunkRadius * 2) + distanceBetweenSets;
		loadDataFromWorld();
	}

	/**
	 * This finds the next empty chunkSet for use, currently only increases the xPos to get new positions
	 *
	 * @return
	 */
	public ChunkSet getNextAvaliableChunkSet(int chunkRadius) {

		int chunkX = xChunkStartingPos + nextChunkSetKey;
		int chunkZ = zChunkStartingPos;

		if (data.avalibleChunkKeys.size() < 0) {
			chunkX = data.avalibleChunkKeys.remove(0);
		} else {
			nextChunkSetKey += chunkSetIncrement;
			data.chunkKey = nextChunkSetKey;
		}
		data.markDirty();
		return new ChunkSet(chunkX, chunkZ, chunkRadius);
	}

	/**
	 * This retrieves the ChunkSetKey data for the specific world
	 */
	public void loadDataFromWorld() {
		data = ChunkKeysWorldData.get(worldObj);
		nextChunkSetKey = data.chunkKey;
	}

	//The +50 is used to make sure chunks too close to ships dont interfere
	public static boolean isLikelyShipChunk(int chunkX, int chunkZ){
		if(chunkZ < zChunkStartingPos + maxChunkRadius + 50){
			return true;
		}
		return false;
	}

}
