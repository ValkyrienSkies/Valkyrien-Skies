package valkyrienwarfare.fixes;

import net.minecraft.world.chunk.Chunk;

import java.util.Iterator;

public interface WorldChunkloadingCrashFix {

	public Iterator<Chunk> getPersistentChunkIterable(Iterator<Chunk> chunkIterator);

}
