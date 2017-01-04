package ValkyrienWarfareBase.Relocation;

import java.util.concurrent.Callable;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.Chunk;

/**
 * Gets around all the lag from Chunk checks
 * 
 * @author thebest108
 *
 */
public class VWChunkCache {

	public Chunk[][] cachedChunks;
	public boolean[][] isChunkLoaded;
	public boolean allLoaded = true;
	public World worldFor;
	int minChunkX, minChunkZ, maxChunkX, maxChunkZ;

	public VWChunkCache(World world, int mnX, int mnZ, int mxX, int mxZ) {
		worldFor = world;
		minChunkX = mnX >> 4;
		minChunkZ = mnZ >> 4;
		maxChunkX = mxX >> 4;
		maxChunkZ = mxZ >> 4;
		cachedChunks = new Chunk[maxChunkX - minChunkX + 1][maxChunkZ - minChunkZ + 1];
		isChunkLoaded = new boolean[maxChunkX - minChunkX + 1][maxChunkZ - minChunkZ + 1];
		for (int x = minChunkX; x <= maxChunkX; x++) {
			for (int z = minChunkZ; z <= maxChunkZ; z++) {
				cachedChunks[x - minChunkX][z - minChunkZ] = world.getChunkFromChunkCoords(x, z);
				isChunkLoaded[x - minChunkX][z - minChunkZ] = !cachedChunks[x - minChunkX][z - minChunkZ].isEmpty();
				if (!isChunkLoaded[x - minChunkX][z - minChunkZ]) {
					allLoaded = false;
				}
			}
		}
	}

	public VWChunkCache(World world, Chunk[][] toCache) {
		minChunkX = toCache[0][0].xPosition;
		minChunkZ = toCache[0][0].zPosition;
		maxChunkX = toCache[toCache.length - 1][toCache[0].length - 1].xPosition;
		maxChunkZ = toCache[toCache.length - 1][toCache[0].length - 1].zPosition;
		cachedChunks = new Chunk[maxChunkX - minChunkX + 1][maxChunkZ - minChunkZ + 1];
		isChunkLoaded = new boolean[maxChunkX - minChunkX + 1][maxChunkZ - minChunkZ + 1];
		cachedChunks = toCache.clone();
	}

	@Nullable
	public TileEntity getTileEntity(BlockPos pos) {
		int i = (pos.getX() >> 4) - this.minChunkX;
		int j = (pos.getZ() >> 4) - this.minChunkZ;
		if (i < 0 || i >= cachedChunks.length || j < 0 || j >= cachedChunks[i].length)
			return null;
		if (cachedChunks[i][j] == null)
			return null;
		return this.cachedChunks[i][j].getTileEntity(pos, Chunk.EnumCreateEntityType.IMMEDIATE);
	}

	public boolean containsPos(BlockPos pos) {
		int i = (pos.getX() >> 4) - this.minChunkX;
		int j = (pos.getZ() >> 4) - this.minChunkZ;
		if (i < 0 || i >= cachedChunks.length || j < 0 || j >= cachedChunks[i].length)
			return false;
		return true;
	}

	public Chunk getChunkAt(int x, int z) {
		return cachedChunks[x - minChunkX][z - minChunkZ];
	}

	public IBlockState getBlockState(BlockPos pos) {
		Chunk chunkForPos = cachedChunks[(pos.getX() >> 4) - minChunkX][(pos.getZ() >> 4) - minChunkZ];
		return chunkForPos.getBlockState(pos);
	}

	public IBlockState getBlockState(int x, int y, int z) {
		Chunk chunkForPos = cachedChunks[(x >> 4) - minChunkX][(z >> 4) - minChunkZ];
		return chunkForPos.getBlockState(x, y, z);
	}

	public void setBlockState(BlockPos pos, IBlockState state) {
		Chunk chunkForPos = cachedChunks[(pos.getX() >> 4) - minChunkX][(pos.getZ() >> 4) - minChunkZ];
		chunkForPos.setBlockState(pos, state);
	}

	public boolean isBlockLoaded(BlockPos pos) {
		return isChunkLoaded[(pos.getX() >> 4) - minChunkX][(pos.getZ() >> 4) - minChunkZ];
	}

	public boolean canSeeSky(BlockPos pos) {
		Chunk chunkForPos = cachedChunks[(pos.getX() >> 4) - minChunkX][(pos.getZ() >> 4) - minChunkZ];
		return chunkForPos.canSeeSky(pos);
	}

	public int getLightFor(EnumSkyBlock type, BlockPos pos) {
		Chunk chunkForPos = cachedChunks[(pos.getX() >> 4) - minChunkX][(pos.getZ() >> 4) - minChunkZ];
		return chunkForPos.getLightFor(type, pos);
	}

	public void setLightFor(EnumSkyBlock p_177431_1_, BlockPos pos, int value) {
		Chunk chunkForPos = cachedChunks[(pos.getX() >> 4) - minChunkX][(pos.getZ() >> 4) - minChunkZ];
		chunkForPos.setLightFor(p_177431_1_, pos, value);
	}

}