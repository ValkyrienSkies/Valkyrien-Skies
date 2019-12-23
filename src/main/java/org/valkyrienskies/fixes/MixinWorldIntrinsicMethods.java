package org.valkyrienskies.fixes;

import java.util.Iterator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;

public interface MixinWorldIntrinsicMethods {

    @SuppressWarnings("unused")
    Iterator<Chunk> getPersistentChunkIterable(Iterator<Chunk> chunkIterator);

    @SuppressWarnings("unused")
    Biome getBiomeForCoordsBody(BlockPos pos);
}
