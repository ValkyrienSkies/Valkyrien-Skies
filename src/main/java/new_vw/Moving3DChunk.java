package new_vw;

import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class Moving3DChunk {

	public Chunk realChunkLol;
	public int minY;
	public int maxY;
	//Don't let the entity touch these, MC does weird stuff
	public float x, y, z;
	public float pitch, yaw, roll;
	
	public Moving3DChunk(Moving3DChunkEntity entityChunk) {
		World world = entityChunk.world;
		realChunkLol = world.getChunkFromChunkCoords(entityChunk.vw_chunkX, entityChunk.vw_chunkZ);
		minY = entityChunk.vw_minChunkY;
		maxY = entityChunk.vw_maxChunkY;
	}
}
