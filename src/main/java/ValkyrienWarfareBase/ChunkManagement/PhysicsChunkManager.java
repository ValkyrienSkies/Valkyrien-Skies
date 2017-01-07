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
	public int xChunkStartingPos = -31250;
	public int zChunkStartingPos = -31250;
	// public int chunkRadius = 3;
	public int maxChunkRadius = 12;
	// Currently at 3 to be safe, this is important because Ships could start affecting
	// each other remotely if this value is too small (ex. 0)
	public int distanceBetweenSets = 12;
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
		nextChunkSetKey += chunkSetIncrement;
		data.chunkKey = nextChunkSetKey;
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

	// Warning: This may end up becoming an issue in future, possibly move this to a variable in the Chunk class
	public boolean isChunkInShipRange(int x, int z, boolean isClient) {
		boolean xInRange = x >= (xChunkStartingPos - maxChunkRadius * 2);
		boolean zInRange = z >= (zChunkStartingPos - maxChunkRadius * 2);
		return xInRange && zInRange;
	}

}
