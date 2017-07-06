package ValkyrienWarfareBase.Fixes;

import java.util.Iterator;

import net.minecraft.world.chunk.Chunk;

public interface WorldChunkloadingCrashFix {

    public Iterator<Chunk> getPersistentChunkIterable(Iterator<Chunk> chunkIterator);

}
