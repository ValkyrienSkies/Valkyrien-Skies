package org.valkyrienskies.mod.common.physics.management.util;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.gen.ChunkProviderServer;
import org.valkyrienskies.mod.common.physics.management.PhysicsObject;

public class SurroundingChunkCacheController {

	private ChunkCache cachedChunks;
	private PhysicsObject physicsObject;

	public SurroundingChunkCacheController(PhysicsObject physicsObject) {
		this.physicsObject = physicsObject;
	}

	public void updateChunkCache() {
		AxisAlignedBB cacheBB = physicsObject.getShipBoundingBox();

		// Check if all those surrounding chunks are loaded
		BlockPos min = new BlockPos(cacheBB.minX, Math.max(cacheBB.minY, 0), cacheBB.minZ);
		BlockPos max = new BlockPos(cacheBB.maxX, Math.min(cacheBB.maxY, 255), cacheBB.maxZ);

		if (!physicsObject.getWorld().isRemote) {
			ChunkProviderServer serverChunkProvider = (ChunkProviderServer) physicsObject.getWorld().getChunkProvider();

			int chunkMinX = min.getX() >> 4;
			int chunkMaxX = max.getX() >> 4;
			int chunkMinZ = min.getZ() >> 4;
			int chunkMaxZ = max.getZ() >> 4;

			boolean areSurroundingChunksLoaded = true;

			outer:
			for (int chunkX = chunkMinX; chunkX <= chunkMaxX; chunkX++) {
				for (int chunkZ = chunkMinZ; chunkZ <= chunkMaxZ; chunkZ++) {
					areSurroundingChunksLoaded = serverChunkProvider.chunkExists(chunkX, chunkZ);
					if (!areSurroundingChunksLoaded) break outer;
				}
			}

			if (areSurroundingChunksLoaded) {
				cachedChunks = new ChunkCache(physicsObject.getWorld(), min, max, 0);
			} else {
				physicsObject.resetConsecutiveProperTicks();
			}
		} else {
			cachedChunks = new ChunkCache(physicsObject.getWorld(), min, max, 0);
		}
	}

	public ChunkCache cachedChunks() {
		return cachedChunks;
	}
}
