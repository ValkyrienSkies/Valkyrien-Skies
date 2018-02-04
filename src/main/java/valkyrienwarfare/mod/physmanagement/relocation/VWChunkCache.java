/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2018 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package valkyrienwarfare.mod.physmanagement.relocation;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

/**
 * Gets around all the lag from Chunk checks
 *
 * @author thebest108
 */
public class VWChunkCache {

	private final Chunk[][] cachedChunks;
	private final boolean[][] isChunkLoaded;
	private boolean allLoaded = true;
	private final World worldFor;
	private final int minChunkX, minChunkZ, maxChunkX, maxChunkZ;

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
		worldFor = world;
		minChunkX = toCache[0][0].x;
		minChunkZ = toCache[0][0].z;
		maxChunkX = toCache[toCache.length - 1][toCache[0].length - 1].x;
		maxChunkZ = toCache[toCache.length - 1][toCache[0].length - 1].z;
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

	private boolean hasChunkAt(int chunkX, int chunkZ) {
	    int relX = chunkX - minChunkX;
	    int relZ = chunkZ - minChunkZ;
	    return relX >= 0 && relX < cachedChunks.length && relZ >= 0 && relZ < cachedChunks[0].length;
	}
	
	public Chunk getChunkAt(int x, int z) {
		return cachedChunks[x - minChunkX][z - minChunkZ];
	}

	public IBlockState getBlockState(BlockPos pos) {
		Chunk chunkForPos = cachedChunks[(pos.getX() >> 4) - minChunkX][(pos.getZ() >> 4) - minChunkZ];
		return chunkForPos.getBlockState(pos);
	}

	public IBlockState getBlockState(int x, int y, int z) {
	    if (!hasChunkAt(x >> 4, z >> 4)) {
	        return Blocks.AIR.getDefaultState();
	    }
		Chunk chunkForPos = cachedChunks[(x >> 4) - minChunkX][(z >> 4) - minChunkZ];
		return chunkForPos.getBlockState(x, y, z);
	}

}