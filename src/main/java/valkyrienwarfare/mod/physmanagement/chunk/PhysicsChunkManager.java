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

package valkyrienwarfare.mod.physmanagement.chunk;

import net.minecraft.world.World;

/**
 * This class is responsible for finding/allocating the Chunks for
 * PhysicsObjects; also ensures the custom chunk-loading system in place
 *
 * @author thebest108
 */
public class PhysicsChunkManager {

	public static int xChunkStartingPos = -1870000;
	public static int zChunkStartingPos = -1870000;
	// public int chunkRadius = 3;
	public static int maxChunkRadius = 12;
	public World worldObj;
	public int nextChunkSetKey;
	public int chunkSetIncrement;
	// Currently at 3 to be safe, this is important because Ships could start
	// affecting
	// each other remotely if this value is too small (ex. 0)
	public int distanceBetweenSets = 1;
	public ChunkKeysWorldData data;

	public PhysicsChunkManager(World worldFor) {
		worldObj = worldFor;
		chunkSetIncrement = (maxChunkRadius * 2) + distanceBetweenSets;
		try {
			loadDataFromWorld();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// The +50 is used to make sure chunks too close to ships dont interfere
	public static boolean isLikelyShipChunk(int chunkX, int chunkZ) {
		if (chunkZ < zChunkStartingPos + maxChunkRadius + 50) {
			return true;
		}
		return false;
	}

	/**
	 * This finds the next empty chunkSet for use, currently only increases the xPos
	 * to get new positions
	 *
	 * @return
	 */
	public ChunkSet getNextAvaliableChunkSet(int chunkRadius) {
		int chunkX = xChunkStartingPos + nextChunkSetKey;
		int chunkZ = zChunkStartingPos;

		if (data.avalibleChunkKeys.size() < 0) {
			chunkX = data.avalibleChunkKeys.get(0);
			data.avalibleChunkKeys.remove(0);
		} else {
			nextChunkSetKey += chunkSetIncrement;
			data.chunkKey = nextChunkSetKey;
		}
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

}
