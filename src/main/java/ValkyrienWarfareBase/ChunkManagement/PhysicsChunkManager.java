package ValkyrienWarfareBase.ChunkManagement;

import net.minecraft.world.World;

/**
 * This class is responsible for finding/allocating the Chunks for PhysicsObjects
 * @author thebest108
 *
 */
public class PhysicsChunkManager {

	public World dimension;
	public int nextChunkSetKey;
	public int chunkSetIncrement;
	public int xChunkStartingPos = -31250;
	public int zChunkStartingPos = -31250;
	public int chunkRadius = 8;
	public ChunkKeysWorldData data;
	
	public PhysicsChunkManager(World worldFor){
		dimension = worldFor;
		chunkSetIncrement = (chunkRadius*2)+1;
		loadDataFromWorld();
	}
	
	/**
	 * This finds the next empty chunkSet for use, currently only increases
	 * the xPos to get new positions
	 * @return
	 */
	public ChunkSet getNextAvaliableChunkSet(){
		int chunkX = xChunkStartingPos+nextChunkSetKey;
		int chunkZ = zChunkStartingPos;
		nextChunkSetKey += chunkSetIncrement;
		data.chunkKey = nextChunkSetKey;
		data.markDirty();
		return new ChunkSet(chunkX,chunkZ,chunkRadius);
	}
	
	/**
	 * This retrieves the ChunkSetKey data for the specific world
	 */
	public void loadDataFromWorld(){
		data = ChunkKeysWorldData.get(dimension);
		nextChunkSetKey = data.chunkKey;
	}

}
