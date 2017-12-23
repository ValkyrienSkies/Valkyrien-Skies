/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2017 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package valkyrienwarfare.chunkmanagement;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/**
 * This stores the chunk claims for a PhysicsObject; not the chunks themselves
 *
 * @author thebest108
 */
public class ChunkSet {

	public World world;
	public int centerX, centerZ;
	public int radius;
	public int minX, maxX, minZ, maxZ;
	public boolean[][] chunkOccupiedInLocal;

	public ChunkSet(int x, int z, int size) {
		centerX = x;
		centerZ = z;
		radius = size;
		minX = centerX - radius;
		maxX = centerX + radius;
		minZ = centerZ - radius;
		maxZ = centerZ + radius;
		chunkOccupiedInLocal = new boolean[(radius * 2) + 1][(radius * 2) + 1];
	}

	public ChunkSet(NBTTagCompound readFrom) {
		this(readFrom.getInteger("centerX"), readFrom.getInteger("centerZ"), readFrom.getInteger("radius"));
	}

	public void writeToNBT(NBTTagCompound toSave) {
		toSave.setInteger("centerX", centerX);
		toSave.setInteger("centerZ", centerZ);
		toSave.setInteger("radius", radius);
	}

	public boolean isChunkEnclosedInMaxSet(int chunkX, int chunkZ) {
		boolean inX = (chunkX >= centerX - 12) && (chunkX <= centerX + 12);
		boolean inZ = (chunkZ >= centerZ - 12) && (chunkZ <= centerZ + 12);
		return inX && inZ;
	}

	public boolean isChunkEnclosedInSet(int chunkX, int chunkZ) {
		boolean inX = (chunkX >= minX) && (chunkX <= maxX);
		boolean inZ = (chunkZ >= minZ) && (chunkZ <= maxZ);
		return inX && inZ;
	}

	@Override
	public String toString() {
		return centerX + ":" + centerZ + ":" + radius;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ChunkSet) {
			ChunkSet other = (ChunkSet) o;
			return other.centerX == centerX && other.centerZ == centerZ && other.radius == radius;
		}
		return false;
	}

}
