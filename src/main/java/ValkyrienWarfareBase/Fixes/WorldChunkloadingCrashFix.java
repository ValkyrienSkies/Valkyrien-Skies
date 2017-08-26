package ValkyrienWarfareBase.Fixes;

import net.minecraft.world.chunk.Chunk;

import java.util.Iterator;

public interface WorldChunkloadingCrashFix {

	public Iterator<Chunk> getPersistentChunkIterable(Iterator<Chunk> chunkIterator);

}
